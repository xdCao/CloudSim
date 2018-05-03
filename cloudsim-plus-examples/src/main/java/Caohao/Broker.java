package Caohao;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;

/**
 * created by xdCao on 2018/5/3
 */

public class Broker extends DatacenterBrokerSimple {


    /**
     * Creates a new DatacenterBroker object.
     *
     * @param simulation name to be associated with this entity
     * @post $none
     */
    public Broker(CloudSim simulation) {
        super(simulation);
    }

//    @Override
//    protected Vm selectVmForWaitingCloudlet(Cloudlet cloudlet) {
//        if (cloudlet.isBindToVm()) {
//            return cloudlet.getVm();
//        }
//
//        /*If user didn't bind this cloudlet to a specific Vm
//        or if the bind VM was not created, try the next Vm on the list of created*/
//        return getVmFromCreatedList(getNextVmIndex());
//    }
//
//    private int getNextVmIndex() {
//        if (getVmExecList().isEmpty()) {
//            return -1;
//        }
//
//        final int vmIndex = getVmExecList().indexOf(getLastSelectedVm());
//        return (vmIndex + 1) % getVmExecList().size();
//    }

}
