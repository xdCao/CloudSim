package Caohao.MigrationPolicy;

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
            .max(Comparator.comparingDouble(Host::getAvailableMips));
        return max;

    }

    @Override
    public Map<Vm, Host> getOptimizedAllocationMap(List<? extends Vm> vmList) {
        return Collections.emptyMap();
    }


}
