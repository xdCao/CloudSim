package Caohao.MigrationPolicy;

import Caohao.CalHelper;
import Caohao.Constants;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

public class ImprovedGreedy extends PABFD {


    @Override
    protected double getPowerSaveAfterAllocationDifference(final Host des, final Vm vm){

        double migCostPower=getPowerAfterAllocationDifference(des, vm);
        double migTime = (double) vm.getRam().getCapacity() / (double) vm.getHost().getBw().getAvailableResource();
        double migCost = migCostPower * migTime;



        final double powerAfterAllocation = getPowerAfterAllocation(des, vm);

        Host source=vm.getHost();

        double addDelta = powerAfterAllocation - des.getPowerModel().getPower(CalHelper.getHostCpuUtilizationPercentage(des));
        double decDelta= source.getPowerModel().getPower(CalHelper.getHostCpuUtilizationPercentage(source))-source.getPowerModel().getPower(CalHelper.getHostCpuPercentAfterDeallocate(source,vm));

        Cloudlet cloudlet = vm.getCloudletScheduler().getCloudletList().get(0);
        double remainTime = cloudlet.getTotalLength() / (Constants.HOST_MIPS*cloudlet.getNumberOfPes()) - (cloudlet.getBroker().getSimulation().clock()-cloudlet.getExecStartTime());

        double addEne=(remainTime-migTime)*addDelta;//这里应该考虑迁移时间进去

        double decEne=(remainTime-migTime)*decDelta;
        double powerSave=decEne-addEne-migCost;
        return powerSave;

    }

}
