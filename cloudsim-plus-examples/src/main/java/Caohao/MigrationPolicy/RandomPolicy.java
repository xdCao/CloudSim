package Caohao.MigrationPolicy;

import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;

public class RandomPolicy extends VmAllocationPolicyMigrationAbstract {


    public RandomPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
        super(vmSelectionPolicy);
    }

    @Override
    public double getOverUtilizationThreshold(Host host) {
        return 0;
    }


}
