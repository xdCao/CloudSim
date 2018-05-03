package Caohao.MigrationPolicy;

import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyRandomSelection;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * created by xdCao on 2018/5/2
 */

public class EnergyMigrationPolicy extends VmAllocationPolicyMigrationAbstract{



    static class MyVmSelection extends PowerVmSelectionPolicy{
        @Override
        public Vm getVmToMigrate(Host host) {
            final List<? extends Vm> migratableVms = getMigratableVms(host);
            if (migratableVms.isEmpty()) {
                return Vm.NULL;
            }

            final Predicate<Vm> inMigration = Vm::isInMigration;
            final Comparator<? super Vm> cpuUsageComparator =
                Comparator.comparingDouble(vm -> vm.getCpuPercentUsage(vm.getSimulation().clock()));
            final Optional<? extends Vm> optional = migratableVms.stream()
                .filter(inMigration.negate())
                .min(cpuUsageComparator);
            return (optional.isPresent() ? optional.get() : Vm.NULL);
        }
    }


    private static MyVmSelection vmSelection=new MyVmSelection();

//    private static PowerVmSelectionPolicy vmSelection=new PowerVmSelectionPolicyRandomSelection();


    public EnergyMigrationPolicy() {
        super(vmSelection);
    }

    public EnergyMigrationPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
        super(vmSelectionPolicy);
    }

    @Override
    public Optional<Host> findHostForVm(Vm vm) {
        List<Host> hostList = getHostList();
        Optional<Host> max = hostList.stream()
            .filter(e -> e.isSuitableForVm(vm))
            .max(Comparator.comparingDouble(Host::getAvailableMips));
        return max;
    }

    @Override
    public Map<Vm, Host> getOptimizedAllocationMap(List<? extends Vm> vmList) {
        //@todo See https://github.com/manoelcampos/cloudsim-plus/issues/94
        final Set<Host> overloadedHosts = getOverloadedHosts();
        printOverUtilizedHosts(overloadedHosts);
        saveAllocation();
        final Map<Vm, Host> migrationMap = getMigrationMapFromOverloadedHosts(overloadedHosts);
        if(migrationMap.size()==0){
            return migrationMap;
        }else {
            updateMigrationMapFromUnderloadedHosts(overloadedHosts, migrationMap);
            restoreAllocation();
            return migrationMap;
        }

    }



    /*---------------------------------------------------------------------------------------------------------------------*/

    //设置过载门限
    @Override
    public double getOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().max(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
    }

    //空闲门限
    @Override
    public boolean isHostUnderloaded(Host host) {
        return getHostCpuUtilizationPercentage(host)<0;
    }

    @Override
    public boolean isHostOverloaded(Host host) {
        double upperThreshold = getOverUtilizationThreshold(host);
        addHistoryEntryIfAbsent(host,upperThreshold);
        return getHostCpuUtilizationPercentage(host) > upperThreshold;

    }


    private double getHostCpuUtilizationPercentage(final Host host) {
        return getHostTotalRequestedMips(host) / host.getTotalMipsCapacity();
    }

    private double getHostTotalRequestedMips(final Host host) {
        return host.getVmList().stream()
            .mapToDouble(Vm::getCurrentRequestedTotalMips)
            .sum();
    }

}
