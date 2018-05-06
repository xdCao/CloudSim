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
import static java.util.stream.Collectors.toSet;

/**
 * created by xdCao on 2018/5/2
 */

public class EnergyMigrationPolicy extends VmAllocationPolicyMigrationAbstract{



    static class MyVmSelection extends PowerVmSelectionPolicy{

        // todo 这里是选择要迁移的虚拟机的算法
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
                .min(comparator);
            return (optional.isPresent() ? optional.get() : Vm.NULL);
        }

        private double calDistributeChange(QosVm vm, Host host) {

            double cur = calDistribution(host);
            double next=CalHelper.calDistributionNext(host,vm);

            return cur-next;


        }


        protected List<QosVm> getMigratableQosVms(Host host) {
            return host.<QosVm>getVmList().stream()
                .filter(vm -> !vm.isInMigration())
                .filter(vm -> vm.getCloudletScheduler().getCloudletList().size()>0)
                .collect(Collectors.toList());
        }
    }


    private static MyVmSelection vmSelection=new MyVmSelection();

//    private static PowerVmSelectionPolicy vmSelection=new PowerVmSelectionPolicyRandomSelection();


    public EnergyMigrationPolicy() {
        super(vmSelection);
    }

    public EnergyMigrationPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
        super(vmSelectionPolicy);
    }

    // todo 这里是找目的主机的算法
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
        //@todo See https://github.com/manoelcampos/cloudsim-plus/issues/94
        final Set<Host> overloadedHosts = getOverloadedHosts();
        printOverUtilizedHosts(overloadedHosts);
//        if (overloadedHosts.size()>0)
//            saveAllocation();
        final Map<Vm, Host> migrationMap = getMigrationMapFromOverloadedHosts(overloadedHosts);
        updateMigrationMapFromUnderloadedHosts(overloadedHosts, migrationMap);
//        if (overloadedHosts.size()>0)
////            restoreAllocation();
        return migrationMap;


    }

    @Override
    protected Set<Host> getOverloadedHosts() {
        return this.getHostList().stream()
            .filter(this::isHostOverloaded)
            .filter(h -> h.getVmsMigratingOut().isEmpty())
            .collect(toSet());
    }

    //todo
    /*找出要进行迁移的VM、PM对*/
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
            findHostForVm(vm, overloadedHosts).ifPresent(host -> addVmToMigrationMap(migrationMap, vm, host, "\t%s will be migrated to %s"));
        }
        Log.printLine();

        return migrationMap;

    }

    /*从过载主机中找出要进行迁移的vmlist*/
    protected List<QosVm> getQosVmsToMigrateFromOverloadedHosts(final Set<Host> overloadedHosts) {
        final List<QosVm> vmsToMigrate = new LinkedList<>();
        for (final Host host : overloadedHosts) {
            vmsToMigrate.addAll(getQosVmsToMigrateFromOverloadedHost(host));
        }
        return vmsToMigrate;
    }

    /*从某一台主机中获取要迁移的虚拟机列表*/
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
//        boolean isHostOverUsedAfterAllocation = true;
//        if (host.createTemporaryVm(vm)) {
//            isHostOverUsedAfterAllocation = isHostOverloaded(host);
//            host.destroyTemporaryVm(vm);
//        }
//        return !isHostOverUsedAfterAllocation;

        //todo 这里

        return true;
    }

    @Override
    protected Optional<Host> findHostForVmInternal(final Vm vm, final Stream<Host> hostStream){
//        final Comparator<Host> hostPowerConsumptionComparator =
//            Comparator.comparingDouble(h -> getPowerAfterAllocationDifference(h, vm));

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


    private boolean isHostUnderloadedAfterAllocation(Host host, Vm vm) {


        return getUtilizationOfCpuMips(host)<0.125;

//        if (host.createTemporaryVm(vm)) {
//            boolean isHostUnderLoadedAfterAlloca = isHostUnderloaded(host);
//            host.destroyTemporaryVm(vm);
//            return isHostUnderLoadedAfterAlloca;
//        }else {
//            return false;
//        }

    }


    /*---------------------------------------------------------------------------------------------------------------------*/

    //设置过载门限，不光是qos本身的要求，其分布也是过载的触发条件
    //todo 这部分暂时先按迁移之后的方差影响来
    @Override
    public double getOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().min(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
    }

    public static double getMyOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().min(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
    }


    //空闲门限
    @Override
    public boolean isHostUnderloaded(Host host) {
        Optional<Double> aDouble = host.getVmList().stream().filter(vm -> !vm.isInMigration()).max(Comparator.comparingDouble(Vm::getCurrentRequestedTotalMips)).map(vm -> vm.getCurrentRequestedTotalMips());
        if (aDouble.isPresent()){
            return (CalHelper.getHostTotalRequestedMips(host)-aDouble.get())/host.getTotalMipsCapacity()<0.125;
        }else {
            return false;
        }
//        return getHostCpuUtilizationPercentage(host)<0.125;
    }

    //过载门限
    @Override
    public boolean isHostOverloaded(Host host) {
        double upperThreshold = getOverUtilizationThreshold(host);
        addHistoryEntryIfAbsent(host,upperThreshold);

        double var = calDistribution(host);

        return CalHelper.getHostCpuUtilizationPercentage(host) > upperThreshold||var>0;

    }





}
