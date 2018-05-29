package Caohao.MigrationPolicy;

import Caohao.CalHelper;
import Caohao.Constants;
import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Caohao.CalHelper.calDistribution;
import static Caohao.CalHelper.calDistributionAddNext;

public class FirstFitMigration extends VmAllocationPolicyMigrationAbstract {


    static class VmSelection extends PowerVmSelectionPolicy{

        @Override
        public Vm getVmToMigrate(Host host) {
            final List<QosVm> migratableVms = getMigratableQosVms(host);
            if (migratableVms.isEmpty()) {
                return Vm.NULL;
            }

            final Predicate<Vm> inMigration = Vm::isInMigration;
            final Comparator<QosVm> comparator =
                Comparator.comparingDouble(vm -> vm.getCurrentRequestedTotalMips());
            final Optional<QosVm> optional = migratableVms.stream()
                .filter(inMigration.negate())
                .max(comparator);
            return (optional.isPresent() ? optional.get() : Vm.NULL);
        }


        List<QosVm> getMigratableQosVms(Host host) {
            return host.<QosVm>getVmList().stream()
                .filter(vm -> !vm.isInMigration())
                .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
                .collect(Collectors.toList());
        }
    }

    public FirstFitMigration() {
        super(new VmSelection());
    }

    @Override
    public Optional<Host> findHostForVm(final Vm vm) {
        List<Host> hostList = getHostList();
        Optional<Host> max = hostList.stream()
            .filter(e -> e.isSuitableForVm(vm))
            .filter(e->isNotHostOverloadedAfterAllocationWithOutQos(e,vm))
            .max(Comparator.comparingDouble(e->allocateCompare(e,vm)));
        return max;
    }

    private double allocateCompare(Host host, Vm vm) {

        return CalHelper.getHostCpuUtilizationPercentage(host);
//
//        double var = calDistributionAddNext(host, (QosVm) vm) / calDistribution(host);
//        double percent=CalHelper.getHostCpuUtilizationPercentageNext(host,vm);
//        return percent/var;

    }

    /*获取迁移后的新映射关系*/
    @Override
    public Map<Vm, Host> getOptimizedAllocationMap(List<? extends Vm> vmList) {
        final Set<Host> overloadedHosts = getOverloadedHosts();
        printOverUtilizedHosts(overloadedHosts);
        final Map<Vm, Host> migrationMap = getMigrationMapFromOverloadedHosts(overloadedHosts);
        updateMigrationMapFromUnderloadedHosts(overloadedHosts, migrationMap);
        return migrationMap;
    }

    //触发空闲迁移
    @Override
    public boolean isHostUnderloaded(Host host) {
        Optional<Double> aDouble = host.getVmList().stream().filter(vm -> !vm.isInMigration())
            .max(Comparator.comparingDouble(Vm::getCurrentRequestedTotalMips))
            .map(vm -> vm.getCurrentRequestedTotalMips());
        return aDouble.filter(aDouble1 ->
            (CalHelper.getHostTotalRequestedMips(host) - aDouble1) / host.getTotalMipsCapacity() < 0.125)
            .isPresent();
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

    //从低负载主机中获取要迁移的虚拟机
    @Override
    protected List<? extends Vm> getVmsToMigrateFromUnderUtilizedHost(Host host) {
        return host.getVmList().stream()
            .filter(vm -> !vm.isInMigration())
            .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /*-----------------------------------------------underload-----------------------------------------------*/

    @Override
    protected Map<Vm, Host> getNewVmPlacementFromUnderloadedHost(List<? extends Vm> vmsToMigrate, Set<? extends Host> excludedHosts) {
        /*这里才是能够进行能耗优化的虚拟机迁移过程*/
        final Map<Vm, Host> migrationMap = new HashMap<>();

        /*这里对要迁移的vm排序*/
        // todo 这里对能耗
        VmList.sortByCpuUtilization(vmsToMigrate, getDatacenter().getSimulation().clock());
        for (final Vm vm : vmsToMigrate) {
            //try to find a target Host to place a VM from an underloaded Host that is not underloaded too
            final Optional<Host> optional = findHostForUnderloadedVm(vm, excludedHosts, host -> isHostNotUnderloadedAfterAllocation(host,vm));// todo 这里细节
            if (!optional.isPresent()) {
//                Log.printFormattedLine("\tA new Host, which isn't also underloaded or won't be overloaded, couldn't be found to migrate %s.", vm);
//                Log.printFormattedLine("\tMigration of VMs from the underloaded %s cancelled.", vm.getHost());
//                return new HashMap<>();
                continue;
            }
            addVmToMigrationMap(migrationMap, vm, optional.get(), "\t%s will be allocated to %s");
        }

        return migrationMap;
    }

    public Optional<Host> findHostForUnderloadedVm(final Vm vm, final Set<? extends Host> excludedHosts, final Predicate<Host> predicate) {
        final Stream<Host> stream = this.getHostList().stream()
            .filter(h -> !excludedHosts.contains(h))
            .filter(h -> h.isSuitableForVm(vm))
            .filter(h -> isNotHostOverloadedAfterAllocation(h, vm))
            .filter(h -> isPowerSaved(h,vm))
            .filter(predicate);

        return findHostForUnderloadedVmInternal(vm, stream);
    }


    @Override
    protected boolean isNotHostOverloadedAfterAllocation(final Host host, final Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold);

    }

    private boolean isHostNotUnderloadedAfterAllocation(Host host, Vm vm) {
        return CalHelper.getHostCpuUtilizationPercentageNext(host,vm)>0.125;
    }

    private boolean isPowerSaved(Host h,Vm vm) {
        double powerSaveAfterAllocationDifference = getPowerSaveAfterAllocationDifference(h, vm);
        if (powerSaveAfterAllocationDifference>0)
            return true;
        else
            return false;
    }



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


    protected Optional<Host> findHostForUnderloadedVmInternal(final Vm vm, final Stream<Host> hostStream){
        final Comparator<Host> hostPowerConsumptionComparator =
            Comparator.comparingDouble(h -> getHostDistributionAndPower(h, vm));

        return additionalHostFilters(vm, hostStream).max(hostPowerConsumptionComparator);
    }


    //这个是低负载迁移的算法核心
    private double getHostDistributionAndPower(Host h, Vm vm) {
        double powerSave = getPowerSaveAfterAllocationDifference(h, vm);
        return powerSave;
    }





}
