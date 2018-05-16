package Caohao.MigrationPolicy;

import Caohao.CalHelper;
import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FirstFitMigration extends VmAllocationPolicyMigrationAbstract {


    public FirstFitMigration(PowerVmSelectionPolicy vmSelectionPolicy) {
        super(vmSelectionPolicy);
    }

    @Override
    public Optional<Host> findHostForVm(final Vm vm) {
        return this.getHostList()
            .stream()
            .sorted()
            .filter(h -> h.isSuitableForVm(vm))
            .filter(h->isNotHostOverloadedAfterAllocationWithOutQos(h,vm))
            .findFirst();
    }


    public double getOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().min(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
    }


    public boolean isHostOverloaded(Host host) {
        double upperThreshold = getOverUtilizationThreshold(host);
        return CalHelper.getHostCpuUtilizationPercentage(host)>upperThreshold;
    }

    private boolean isNotHostOverloadedAfterAllocationWithOutQos(Host host, Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold);
    }



}
