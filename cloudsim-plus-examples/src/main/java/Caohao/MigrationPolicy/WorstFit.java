package Caohao.MigrationPolicy;

import Caohao.CalHelper;
import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.*;

/**
 * created by xdCao on 2018/5/3
 */

public class WorstFit extends VmAllocationPolicyAbstract {


    @Override
    public Optional<Host> findHostForVm(Vm vm) {

        List<Host> hostList = getHostList();
        Optional<Host> max = hostList.stream()
            .filter(e -> e.isSuitableForVm(vm))
            .filter(h->isNotHostOverloadedAfterAllocationWithOutQos(h,vm))
            .max(Comparator.comparingDouble(Host::getAvailableMips));
        return max;

    }

    @Override
    public Map<Vm, Host> getOptimizedAllocationMap(List<? extends Vm> vmList) {
        return Collections.emptyMap();
    }

    private boolean isNotHostOverloadedAfterAllocationWithOutQos(Host host, Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold);
    }


    public double getOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().min(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
    }

}
