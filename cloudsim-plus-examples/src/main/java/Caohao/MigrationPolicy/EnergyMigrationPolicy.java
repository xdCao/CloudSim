package Caohao.MigrationPolicy;

import Caohao.Broker;
import Caohao.CalHelper;
import Caohao.Constants;
import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Caohao.CalHelper.calDistribution;
import static Caohao.CalHelper.calDistributionAddNext;
import static Caohao.CalHelper.getHostCpuUtilizationPercentageNext;
import static Caohao.Constants.VAR_THRESHOLD;
import static java.util.stream.Collectors.toSet;

/**
 * created by xdCao on 2018/5/2
 */

public class EnergyMigrationPolicy extends VmAllocationPolicyMigrationAbstract{

    private static MyVmSelection vmSelection=new MyVmSelection();

    public EnergyMigrationPolicy() {
        super(vmSelection);
    }

    public EnergyMigrationPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
        super(vmSelectionPolicy);
    }


    /*-----------------------------------------overload----------------------------------------------*/

    /*找出因为过载要进行迁移的VM、PM对*/
    @Override
    protected Map<Vm, Host> getMigrationMapFromOverloadedHosts(Set<Host> overloadedHosts) {

        final List<QosVm> vmsToMigrate = getQosVmsToMigrateFromOverloadedHosts(overloadedHosts);
        final Map<Vm, Host> migrationMap = new HashMap<>();
        if(overloadedHosts.isEmpty()) {
            return migrationMap;
        }

        Log.printLine("\tReallocation of VMs from overloaded hosts: ");
//        VmList.sortByCpuUtilization(vmsToMigrate, getDatacenter().getSimulation().clock());
        /*这里是对要迁移的虚拟机进行排序，应该按带来的能耗收益来排,但是对于过载的虚拟机而言，不存在能耗收益，所以这一部分的排序应该按qos*/
        Comparator<QosVm> comparator =
            Comparator.comparingDouble(vm -> sortVmToMigrate(vm));
        vmsToMigrate.sort(comparator);

        for (final QosVm vm : vmsToMigrate) {
            /*这里是找迁移目的主机的过程*/
            if (vm.getId()==2){
                Log.print("");
            }
            Log.print("vm"+vm.getId()+" ");

            Optional<Host> hostOptional= findHostForOverloadedVm(vm, overloadedHosts, host -> checkForVmFromOverLoaded(host, vm));
            if (hostOptional.isPresent()){
                addVmToMigrationMap(migrationMap, vm, hostOptional.get(), "\t%s will be migrated to %s");
            }else {
                vm.getHost().createTemporaryVm(vm);
            }



        }
        Log.printLine();

        return migrationMap;

    }

    private double sortVmToMigrate(QosVm vm) {
        double varDec = CalHelper.calDistributionRemoveNext(vm.getHost(), vm)/CalHelper.calDistribution(vm.getHost());
        return vm.getQos()*varDec;
    }

    public Optional<Host> findHostForOverloadedVm(final Vm vm, final Set<? extends Host> excludedHosts, final Predicate<Host> predicate) {
        final Stream<Host> stream = this.getHostList().stream()
            .filter(h -> !excludedHosts.contains(h))
            .filter(h -> h.isSuitableForVm(vm))
            .filter(h -> isNotHostOverloadedAfterAllocation(h, vm))
            .filter(predicate);

        return findHostForOverloadedVmInternal(vm, stream);
    }

    @Override
    protected boolean isNotHostOverloadedAfterAllocation(final Host host, final Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        double var = CalHelper.calDistributionAddNext(host,(QosVm) vm);
        /*不违反QOS且方差比原来小*/
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold)&&(var<=CalHelper.calDistribution(host));

    }

    private boolean checkForVmFromOverLoaded(Host host, QosVm vm) {
        return CalHelper.calDistributionRemoveNext(vm.getHost(),vm)-CalHelper.calDistribution(vm.getHost())<0;
    }

    protected Optional<Host> findHostForOverloadedVmInternal(final Vm vm, final Stream<Host> hostStream){
        final Comparator<Host> hostPowerConsumptionComparator =
            Comparator.comparingDouble(h -> compareHostDistribution(h, vm));

        return additionalHostFilters(vm, hostStream).min(hostPowerConsumptionComparator);
    }

    private double compareHostDistribution(Host h, Vm vm) {
        double var = calDistributionAddNext(h, (QosVm) vm) / calDistribution(h);
        double percent=CalHelper.getHostCpuUtilizationPercentageNext(h,vm);
        return var/percent;
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


    protected Optional<Host> findHostForUnderloadedVmInternal(final Vm vm, final Stream<Host> hostStream){
        final Comparator<Host> hostPowerConsumptionComparator =
            Comparator.comparingDouble(h -> getHostDistributionAndPower(h, vm));

        return additionalHostFilters(vm, hostStream).max(hostPowerConsumptionComparator);
    }


    //这个是低负载迁移的算法核心
    private double getHostDistributionAndPower(Host h, Vm vm) {
        double powerSave = getPowerSaveAfterAllocationDifference(h, vm);
        double var = calDistributionAddNext(h, (QosVm) vm) / calDistribution(h);
        return powerSave/var;
    }





/*-------------------------------------------------------------------------------------------------------------------------------*/
    // todo 虚拟机初始分配策略，不包括迁移，目前是找空闲CPU最少的
    @Override
    public Optional<Host> findHostForVm(Vm vm) {
        List<Host> hostList = getHostList();
        Optional<Host> max = hostList.stream()
            .filter(e -> e.isSuitableForVm(vm))
            .filter(e->isNotHostOverloadedAfterAllocationWithOutQos(e,vm))
            .max(Comparator.comparingDouble(e->allocateCompare(e,vm)));
        return max;
    }

    private double allocateCompare(Host host, Vm vm) {

        double var = calDistributionAddNext(host, (QosVm) vm) / calDistribution(host);
        double percent=CalHelper.getHostCpuUtilizationPercentageNext(host,vm);
        return percent/var;

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


    /*------------------------------------------------------------------------------------------------------------------*/


    /*从过载主机中找出要进行迁移的vmlist*/
    protected List<QosVm> getQosVmsToMigrateFromOverloadedHosts(final Set<Host> overloadedHosts) {
        final List<QosVm> vmsToMigrate = new LinkedList<>();
        for (final Host host : overloadedHosts) {
            vmsToMigrate.addAll(getQosVmsToMigrateFromOverloadedHost(host));
        }
        return vmsToMigrate;
    }

    /*从某一Host中获取要迁移的虚拟机列表*/
    protected List<QosVm> getQosVmsToMigrateFromOverloadedHost(final Host host) {
        final List<QosVm> vmsToMigrate = new LinkedList<>();
        while (true) {
            final Vm vm = getVmSelectionPolicy().getVmToMigrate(host);//这里调用了我们VMSelectPolicy的方法
            if (Vm.NULL == vm) {
                break;
            }
            if (vm.getId()==2)
                Log.print("");
            vmsToMigrate.add((QosVm) vm);
            /*循环释放虚拟机直到主机不再过载*/
            host.destroyTemporaryVm(vm);
            if (!isHostOverloaded(host)) {
                break;
            }
        }

        return vmsToMigrate;
    }


    //从低负载主机中获取要迁移的虚拟机
    @Override
    protected List<? extends Vm> getVmsToMigrateFromUnderUtilizedHost(Host host) {
        return host.getVmList().stream()
            .filter(vm -> !vm.isInMigration())
            .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
            .collect(Collectors.toCollection(LinkedList::new));
    }



    /*--------------------------------------------------------门限和触发条件-------------------------------------------------------------*/

    //设置过载门限，不光是qos本身的要求，其分布也是过载的触发条件
    //todo 这部分暂时先按迁移之后的方差影响来
    @Override
    public double getOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().min(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
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

    /*获取过载主机集合*/
    @Override
    protected Set<Host> getOverloadedHosts() {
        return this.getHostList().stream()
            .filter(this::isHostOverloaded)
            .filter(h -> h.getVmsMigratingOut().isEmpty())
            .collect(toSet());
    }

    //触发过载迁移
    @Override
    public boolean isHostOverloaded(Host host) {


        double upperThreshold = getOverUtilizationThreshold(host);
        addHistoryEntryIfAbsent(host,upperThreshold);
        double var = calDistribution(host);
        return (CalHelper.getHostCpuUtilizationPercentage(host)>upperThreshold||var>VAR_THRESHOLD);

    }

    /*不考虑QOS的情况下判断分配新的虚拟机后是否过载*/
    private boolean isNotHostOverloadedAfterAllocationWithOutQos(Host host, Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold);
    }




    protected double getPowerSaveAfterAllocationDifference(final Host des, final Vm vm){
        final double powerAfterAllocation = getPowerAfterAllocation(des, vm);

        Host source=vm.getHost();

        double addDelta = powerAfterAllocation - des.getPowerModel().getPower(CalHelper.getHostCpuUtilizationPercentage(des));
        double decDelta= source.getPowerModel().getPower(CalHelper.getHostCpuUtilizationPercentage(source))-source.getPowerModel().getPower(CalHelper.getHostCpuPercentAfterDeallocate(source,vm));

        Cloudlet cloudlet = vm.getCloudletScheduler().getCloudletList().get(0);
        double remainTime = cloudlet.getTotalLength() / (Constants.HOST_MIPS*cloudlet.getNumberOfPes()) - (cloudlet.getBroker().getSimulation().clock()-cloudlet.getExecStartTime());

        double addEne=remainTime*addDelta;//这里应该考虑迁移时间进去

        double decEne=remainTime*decDelta;
        double powerSave=decEne-addEne;
        return powerSave;
    }
    @Override
    protected double getPowerAfterAllocation(final Host host, final Vm vm) {
        try {
            return host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
        } catch (Exception e) {
            Log.printFormattedLine("[ERROR] Power consumption for Host %d could not be determined: ", host.getId(), e.getMessage());
        }

        return 0;
    }




    protected double getMaxUtilizationAfterDeallocation(final Host host, final Vm vm) {
        final double requestedTotalMips = vm.getCurrentRequestedTotalMips();
        final double hostUtilizationMips = getUtilizationOfCpuMips(host);
        final double hostPotentialMipsUse = hostUtilizationMips - requestedTotalMips;
        return hostPotentialMipsUse / host.getTotalMipsCapacity();
    }

    @Override
    protected double getUtilizationOfCpuMips(final Host host) {
        double hostUtilizationMips = 0;
        for (final Vm vm2 : host.getVmList()) {
            if (host.getVmsMigratingIn().contains(vm2)) {
                // calculate additional potential CPU usage of a migrating in VM
                hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
            }
            hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
        }
        return hostUtilizationMips;
    }


    static class MyVmSelection extends PowerVmSelectionPolicy{

        // todo 这里是选择要迁移的虚拟机的算法,目前选出对方差影响最大的
        @Override
        public Vm getVmToMigrate(Host host) {
            final List<QosVm> migratableVms = getMigratableQosVms(host);
            if (migratableVms.isEmpty()) {
                return Vm.NULL;
            }

            final Predicate<Vm> inMigration = Vm::isInMigration;
            final Comparator<QosVm> comparator =
                Comparator.comparingDouble(vm -> calDistributeChange(vm,host));
            final Optional<QosVm> optional = migratableVms.stream()
                .filter(inMigration.negate())
                .max(comparator);
            return (optional.isPresent() ? optional.get() : Vm.NULL);
        }

        /*移除vm后host的qos方差减小的值*/
        private double calDistributeChange(QosVm vm, Host host) {
            double cur = calDistribution(host);
            double next=CalHelper.calDistributionRemoveNext(host,vm);
            return (cur-next)-vm.getQos();
        }


        List<QosVm> getMigratableQosVms(Host host) {
            return host.<QosVm>getVmList().stream()
                .filter(vm -> !vm.isInMigration())
                .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
                .collect(Collectors.toList());
        }
    }


}
