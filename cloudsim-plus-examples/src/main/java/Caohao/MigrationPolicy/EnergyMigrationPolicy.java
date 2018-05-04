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

        // todo 这里是选择要迁移的虚拟机的算法
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

        @Override
        protected List<Vm> getMigratableVms(Host host) {
            return host.<Vm>getVmList().stream()
                .filter(vm -> !vm.isInMigration())
                .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
                .collect(Collectors.toList());
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

    // todo 这里是找目的主机的算法
    @Override
    public Optional<Host> findHostForVm(Vm vm) {
        List<Host> hostList = getHostList();
        Optional<Host> max = hostList.stream()
            .filter(e -> e.isSuitableForVm(vm))
            .min(Comparator.comparingDouble(Host::getAvailableMips));
        return max;
    }

    @Override
    public Map<Vm, Host> getOptimizedAllocationMap(List<? extends Vm> vmList) {
        //@todo See https://github.com/manoelcampos/cloudsim-plus/issues/94
        final Set<Host> overloadedHosts = getOverloadedHosts();
        printOverUtilizedHosts(overloadedHosts);
//        if (overloadedHosts.size()>0)
//            saveAllocation();
        final Map<Vm, Host> migrationMap = getMigrationMapFromOverloadedHosts(overloadedHosts);
        updateMigrationMapFromUnderloadedHosts(overloadedHosts, migrationMap);
//        if (overloadedHosts.size()>0)
////            restoreAllocation();
        return migrationMap;


    }


    //todo
    /*找出要进行迁移的VM、PM对*/
    @Override
    protected Map<Vm, Host> getMigrationMapFromOverloadedHosts(Set<Host> overloadedHosts) {

        final List<Vm> vmsToMigrate = getVmsToMigrateFromOverloadedHosts(overloadedHosts);
        final Map<Vm, Host> migrationMap = new HashMap<>();
        if(overloadedHosts.isEmpty()) {
            return migrationMap;
        }

        Log.printLine("\tReallocation of VMs from overloaded hosts: ");
        VmList.sortByCpuUtilization(vmsToMigrate, getDatacenter().getSimulation().clock());
        for (final Vm vm : vmsToMigrate) {
            findHostForVm(vm, overloadedHosts).ifPresent(host -> addVmToMigrationMap(migrationMap, vm, host, "\t%s will be migrated to %s"));
        }
        Log.printLine();

        return migrationMap;

    }

    /*从过载主机中找出要进行迁移的vmlist*/
    @Override
    protected List<Vm> getVmsToMigrateFromOverloadedHosts(final Set<Host> overloadedHosts) {
        final List<Vm> vmsToMigrate = new LinkedList<>();
        for (final Host host : overloadedHosts) {
            vmsToMigrate.addAll(getVmsToMigrateFromOverloadedHost(host));
        }

        return vmsToMigrate;
    }

    /*从某一台主机中获取要迁移的虚拟机列表*/
    @Override
    protected List<Vm> getVmsToMigrateFromOverloadedHost(final Host host) {
        final List<Vm> vmsToMigrate = new LinkedList<>();
        while (true) {
            final Vm vm = getVmSelectionPolicy().getVmToMigrate(host);
            if (Vm.NULL == vm) {
                break;
            }
            vmsToMigrate.add(vm);
            /*Temporarily destroys the selected VM into the overloaded Host so that
            the loop gets VMs from such a Host until it is not overloaded anymore.*/
            host.destroyTemporaryVm(vm);
            if (!isHostOverloaded(host)) {
                break;
            }
        }

        return vmsToMigrate;
    }

    @Override
    protected List<? extends Vm> getVmsToMigrateFromUnderUtilizedHost(Host host) {
        return host.getVmList().stream()
            .filter(vm -> !vm.isInMigration())
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    protected Map<Vm, Host> getNewVmPlacementFromUnderloadedHost(List<? extends Vm> vmsToMigrate, Set<? extends Host> excludedHosts) {
        final Map<Vm, Host> migrationMap = new HashMap<>();
        VmList.sortByCpuUtilization(vmsToMigrate, getDatacenter().getSimulation().clock());
        for (final Vm vm : vmsToMigrate) {
            //try to find a target Host to place a VM from an underloaded Host that is not underloaded too
            final Optional<Host> optional = findHostForVm(vm, excludedHosts, host -> !isHostUnderloadedAfterAllocation(host,vm));
            if (!optional.isPresent()) {
                Log.printFormattedLine("\tA new Host, which isn't also underloaded or won't be overloaded, couldn't be found to migrate %s.", vm);
                Log.printFormattedLine("\tMigration of VMs from the underloaded %s cancelled.", vm.getHost());
                return new HashMap<>();
            }
            addVmToMigrationMap(migrationMap, vm, optional.get(), "\t%s will be allocated to %s");
        }

        return migrationMap;
    }

    private boolean isHostUnderloadedAfterAllocation(Host host, Vm vm) {

        if (host.createTemporaryVm(vm)) {
            boolean isHostUnderLoadedAfterAlloca = isHostUnderloaded(host);
            host.destroyTemporaryVm(vm);
            return isHostUnderLoadedAfterAlloca;
        }else {
            return false;
        }

    }


    /*---------------------------------------------------------------------------------------------------------------------*/

    //设置过载门限
    @Override
    public double getOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().min(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
    }

    //空闲门限
    @Override
    public boolean isHostUnderloaded(Host host) {
        Optional<Double> aDouble = host.getVmList().stream().filter(vm -> !vm.isInMigration()).max(Comparator.comparingDouble(Vm::getCurrentRequestedTotalMips)).map(vm -> vm.getCurrentRequestedTotalMips());
        if (aDouble.isPresent()){
            return (getHostTotalRequestedMips(host)-aDouble.get())<0.125;
        }else {
            return false;
        }
//        return getHostCpuUtilizationPercentage(host)<0.125;
    }

    //过载门限
    @Override
    public boolean isHostOverloaded(Host host) {
        double upperThreshold = getOverUtilizationThreshold(host);
        addHistoryEntryIfAbsent(host,upperThreshold);
        return getHostCpuUtilizationPercentage(host) > upperThreshold;

    }


    public static double getHostCpuUtilizationPercentage(final Host host) {
        return getHostTotalRequestedMips(host) / host.getTotalMipsCapacity();
    }

    public static double getHostTotalRequestedMips(final Host host) {
        return host.getVmList().stream()
            .mapToDouble(Vm::getCurrentRequestedTotalMips)
            .sum();
    }

}
