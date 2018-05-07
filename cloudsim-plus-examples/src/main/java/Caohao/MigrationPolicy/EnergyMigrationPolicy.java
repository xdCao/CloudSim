package Caohao.MigrationPolicy;

import Caohao.CalHelper;
import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Caohao.CalHelper.calAvaQos;
import static Caohao.CalHelper.calDistribution;
import static Caohao.CalHelper.calDistributionNext;
import static Caohao.Constants.VAR_THRESHOLD;
import static java.util.stream.Collectors.toSet;

/**
 * created by xdCao on 2018/5/2
 */

public class EnergyMigrationPolicy extends VmAllocationPolicyMigrationAbstract{



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
            double next=CalHelper.calDistributionNext(host,vm);
            return cur-next;
        }


        List<QosVm> getMigratableQosVms(Host host) {
            return host.<QosVm>getVmList().stream()
                .filter(vm -> !vm.isInMigration())
                .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
                .collect(Collectors.toList());
        }
    }


    private static MyVmSelection vmSelection=new MyVmSelection();

    public EnergyMigrationPolicy() {
        super(vmSelection);
    }

    public EnergyMigrationPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
        super(vmSelectionPolicy);
    }

    // todo 虚拟机初始分配策略，不包括迁移，目前是找空闲CPU最多的
    @Override
    public Optional<Host> findHostForVm(Vm vm) {
        List<Host> hostList = getHostList();
        Optional<Host> max = hostList.stream()
            .filter(e -> e.isSuitableForVm(vm))
            .filter(e->isNotHostOverloadedAfterAllocationWithOutQos(e,vm))
            .min(Comparator.comparingDouble(Host::getAvailableMips));
        return max;
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
            Comparator.comparingDouble(vm -> vm.getQos());
        vmsToMigrate.sort(comparator);

        for (final Vm vm : vmsToMigrate) {
            /*这里是找迁移目的主机的过程*/
            Log.print("vm"+vm.getId()+" ");
//            findHostForVm(vm, overloadedHosts,host -> checkForVmFromOverLoaded(host,vm))
            findHostForVm(vm, overloadedHosts,host -> checkForVmFromOverLoaded(host,vm))
                .ifPresent(host -> addVmToMigrationMap(migrationMap, vm, host, "\t%s will be migrated to %s"));

        }
        Log.printLine();

        return migrationMap;

    }

    private boolean checkForVmFromOverLoaded(Host host, Vm vm) {
        return true;
    }

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
            vmsToMigrate.add((QosVm) vm);
            /*循环释放虚拟机直到主机不再过载*/
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
            .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    protected Map<Vm, Host> getNewVmPlacementFromUnderloadedHost(List<? extends Vm> vmsToMigrate, Set<? extends Host> excludedHosts) {
        /*这里才是能够进行能耗优化的虚拟机迁移过程*/
        final Map<Vm, Host> migrationMap = new HashMap<>();
        VmList.sortByCpuUtilization(vmsToMigrate, getDatacenter().getSimulation().clock());
        for (final Vm vm : vmsToMigrate) {
            //try to find a target Host to place a VM from an underloaded Host that is not underloaded too
            final Optional<Host> optional = findHostForVm(vm, excludedHosts, host -> !isHostUnderloadedAfterAllocation(host,vm));// todo 这里细节
            if (!optional.isPresent()) {
                Log.printFormattedLine("\tA new Host, which isn't also underloaded or won't be overloaded, couldn't be found to migrate %s.", vm);
                Log.printFormattedLine("\tMigration of VMs from the underloaded %s cancelled.", vm.getHost());
                return new HashMap<>();
            }
            addVmToMigrationMap(migrationMap, vm, optional.get(), "\t%s will be allocated to %s");
        }

        return migrationMap;
    }

    public Optional<Host> findHostForVm(final Vm vm, final Set<? extends Host> excludedHosts, final Predicate<Host> predicate) {
        final Stream<Host> stream = this.getHostList().stream()
            .filter(h -> !excludedHosts.contains(h))
            .filter(h -> h.isSuitableForVm(vm))
            .filter(h -> isNotHostOverloadedAfterAllocation(h, vm))
            .filter(predicate);

        return findHostForVmInternal(vm, stream);
    }

    @Override
    protected boolean isNotHostOverloadedAfterAllocation(final Host host, final Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        double var = calDistributionNext(host,(QosVm) vm);
        /*不违反QOS且方差在一定范围*/
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold)&&(var<=VAR_THRESHOLD);
    }

    @Override
    protected Optional<Host> findHostForVmInternal(final Vm vm, final Stream<Host> hostStream){
        final Comparator<Host> hostPowerConsumptionComparator =
            Comparator.comparingDouble(h -> getHostDistributionAndPower(h, vm));

        return additionalHostFilters(vm, hostStream).min(hostPowerConsumptionComparator);
    }


    //这个是低负载迁移的算法核心
    private double getHostDistributionAndPower(Host h, Vm vm) {
        double powerDif = getPowerAfterAllocationDifference(h, vm);
        double var=Math.abs(((QosVm)vm).getQos()-calAvaQos(h));
//        return powerDif+var;
        return var;
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
        return CalHelper.getHostCpuUtilizationPercentage(host)>upperThreshold||var>VAR_THRESHOLD;

    }

    /*不考虑QOS的情况下判断分配新的虚拟机后是否过载*/
    private boolean isNotHostOverloadedAfterAllocationWithOutQos(Host host, Vm vm) {
        double upperThreshold = getOverUtilizationThreshold(host);
        double nextUpperHold=upperThreshold<((QosVm)vm).getQos()?upperThreshold:((QosVm)vm).getQos();
        return (CalHelper.getHostCpuUtilizationPercentageNext(host,vm)<=nextUpperHold);
    }

    private boolean isHostUnderloadedAfterAllocation(Host host, Vm vm) {
        return getUtilizationOfCpuMips(host)<0.125;
    }





}
