package Caohao.MigrationPolicy;

import Caohao.CalHelper;
import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import static Caohao.CalHelper.calDistribution;

public class StaticVarThreshold extends EnergyMigrationPolicy {

    @Override
    protected boolean isNotHostOverloadedAfterAllocation(Host host, Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        double var = CalHelper.calDistributionAddNext(host,(QosVm) vm);

        /*不违反QOS且方差比原来小*/
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold)&&(var<=CalHelper.calDistribution(host));
//        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold)&&(var<=getDynamicVar());// todo 新的动态门限

    }

    @Override
    public boolean isHostOverloaded(Host host) {
        double upperThreshold = getOverUtilizationThreshold(host);
        addHistoryEntryIfAbsent(host,upperThreshold);
        double var = calDistribution(host);
        return (CalHelper.getHostCpuUtilizationPercentage(host)>upperThreshold||var>0.01);

//        return (CalHelper.getHostCpuUtilizationPercentage(host)>upperThreshold||var>getDynamicVar());
    }
}
