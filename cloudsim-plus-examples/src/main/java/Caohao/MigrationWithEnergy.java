package Caohao;

import jdk.nashorn.internal.runtime.AllocationStrategy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationMedianAbsoluteDeviation;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.hosts.HostStateHistoryEntry;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.autoscaling.HorizontalVmScaling;
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.ParallelSimulationsExample;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.omg.CORBA.INTERNAL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static Caohao.Constants.*;

/**
 * created by xdCao on 2018/4/24
 */

public class MigrationWithEnergy implements Runnable{

    private Random random=new Random();

    private CloudSim simulation;

    private VmAllocationPolicy allocationPolicy;

    private List<Host> hostList;

    private final List<Vm> vmList = new ArrayList<>(VMS);

    private final List<Cloudlet> cloudletList=new ArrayList<>(VMS);

    private DatacenterBroker broker;

    MigrationWithEnergy(VmAllocationPolicy allocationPolicy){
        this.allocationPolicy=allocationPolicy;
    }

    @Override
    public void run() {

        simulation = new CloudSim();

        simulation.addOnClockTickListener(this::createNewCloudlets);

        Datacenter datacenter0 = createDatacenter(allocationPolicy);
        datacenter0.setLog(false);
        broker = new DatacenterBrokerSimple(simulation);

        broker.setVmDestructionDelayFunction(vm -> 0.0);

        initVms();

        initCloudLets();

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        simulation.start();

    }


    /*---------------------------------------------------------动态请求--------------------------------------------*/

    public void initVms(){
        Vm vm = new VmSimple(vmList.size(),VM_MIPS, VM_PES);
        vm
            .setRam(VM_RAM).setBw((long)VM_BW).setSize(VM_SIZE)
            .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        vm.getUtilizationHistory().enable();
        createHorizontalVmScaling(vm);
        vmList.add(vm);

    }

    private void createHorizontalVmScaling(Vm vm) {
        HorizontalVmScaling horizontalScaling = new HorizontalVmScalingSimple();
        horizontalScaling
            .setVmSupplier(this::createVm)
            .setOverloadPredicate(this::isVmOverloaded);
        vm.setHorizontalScaling(horizontalScaling);
    }

    private Vm createVm() {
        int id=vmList.size();
        Vm vm=new VmSimple(id, VM_MIPS,VM_PES)
            .setRam(VM_RAM).setBw((long)VM_BW).setSize(VM_SIZE)
            .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        createHorizontalVmScaling(vm);
        vmList.add(vm);
        return vm;
    }

    private boolean isVmOverloaded(Vm vm) {
        return vm.getCpuPercentUsage() > 0.7;
    }


    public void initCloudLets(){

        UtilizationModelFull um = new UtilizationModelFull();

        Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGHT+2000*random.nextInt(10),VM_PES)
                .setFileSize(CLOUDLET_FILESIZE)
                .setOutputSize(CLOUDLET_OUTPUTSIZE)
                .setUtilizationModelCpu(um)
                .setUtilizationModelRam(um)
                .setUtilizationModelBw(um);

        cloudlet.addOnFinishListener(eventInfo->destroyVm(eventInfo,broker));
        cloudletList.add(cloudlet);

    }

    private void destroyVm(CloudletVmEventInfo eventInfo, DatacenterBroker broker) {
        Log.printFormattedLine("\n\t#Cloudlet %d finished. destroy VM %d \n",
            eventInfo.getCloudlet().getId(),eventInfo.getVm().getId());
        eventInfo.getVm().getHost().destroyVm(eventInfo.getVm());
    }

    private void createNewCloudlets(EventInfo eventInfo) {
        if (cloudletList.size()>=VMS){
            return;
        }
        final long time = (long) eventInfo.getTime();
        UtilizationModelFull um = new UtilizationModelFull();
        if (time % Req_INTERVAL == 0 && time <= 50) {
            Log.printFormattedLine("\t#Creating Cloudlet at time %d.", time);
            Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGHT+2000*random.nextInt(10),VM_PES)
                .setFileSize(CLOUDLET_FILESIZE)
                .setOutputSize(CLOUDLET_OUTPUTSIZE)
                .setUtilizationModelCpu(um)
                .setUtilizationModelRam(um)
                .setUtilizationModelBw(um);
            cloudlet.addOnFinishListener(eventInfoVm->destroyVm(eventInfoVm,broker));
            cloudletList.add(cloudlet);
            broker.submitCloudlet(cloudlet);
        }
    }

/*---------------------------------------------初始化底层网络----------------------------------------------------*/

    private Datacenter createDatacenter(VmAllocationPolicy allocationPolicy) {
        this.hostList = new ArrayList<>();
        for(int i = 0; i < HOSTS; i++){
            final int pes = HOST_INITIAL_PES ;
            Host host = createHost(pes, HOST_MIPS);
            hostList.add(host);
        }
        Log.printLine();

        Datacenter dc = new DatacenterSimple(simulation, hostList, allocationPolicy);
        dc.setSchedulingInterval(SCHEDULE_INTERVAL).setLog(false);
        return dc;
    }

    public Host createHost(int numberOfPes, long mipsByPe) {
        List<Pe> peList = createPeList(numberOfPes, mipsByPe);
        Host host =
            new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
        host
            .setRamProvisioner(new ResourceProvisionerSimple())
            .setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(new VmSchedulerTimeShared(0));
        host.enableStateHistory();


        host.setPowerModel(new NonLinearPowerModel());


        return host;
    }

    public List<Pe> createPeList(int numberOfPEs, long mips) {
        List<Pe> list = new ArrayList<>(numberOfPEs);
        for(int i = 0; i < numberOfPEs; i++) {
            list.add(new PeSimple(mips, new PeProvisionerSimple()));
        }
        return list;
    }


/*-----------------------------------------------打印----------------------------------------------------------*/



    public void print() {
        final List<Cloudlet> finishedList = broker.getCloudletFinishedList();
        finishedList.sort(
            Comparator.comparingInt((Cloudlet c) -> c.getVm().getHost().getId())
                .thenComparingInt(c -> c.getVm().getId()));
        new CloudletsTableBuilder(cloudletList).build();

        System.out.println("\n    WHEN A HOST CPU ALLOCATED MIPS IS LOWER THAN THE REQUESTED, IT'S DUE TO VM MIGRATION OVERHEAD)\n");

        for (Host host:hostList){
            PrintHelper.printHistory(host);
        }

        PrintHelper.printEnergy(hostList);

        Log.printConcatLine( "finished!");
    }





/*--------------------------------------------------------------------------------deprecated----------------------*/

    private void submitNewVmsAndCloudletsToBroker(CloudletVmEventInfo eventInfo, DatacenterBroker broker) {
        if (vmList.size()>=VMS){
            return;
        }
        Log.printFormattedLine("\n\t#Cloudlet %d finished. Submitting %d new VMs to the broker\n",
            eventInfo.getCloudlet().getId(),1);
        dynamicCreateVmsAndTasks(broker);

    }


    private void dynamicCreateVmsAndTasks(DatacenterBroker broker){

//        Vm vm=createVm(vmList.size(),broker,1+random.nextInt(VM_PES));
        Vm vm=createVm(vmList.size(),broker,VM_PES);
        vmList.add(vm);
        broker.submitVm(vm);


        UtilizationModelFull um = new UtilizationModelFull();
        Cloudlet cloudlet=createCloudlet(cloudletList.size(),vm,broker,um);
        cloudlet.addOnFinishListener(eventInfo->submitNewVmsAndCloudletsToBroker(eventInfo,broker));
        cloudletList.add(cloudlet);
        broker.submitCloudlet(cloudlet);




    }
    public Vm createVm(int id,DatacenterBroker broker, int pes) {
//        Vm vm = new VmSimple(id,VM_MIPS, 1+random.nextInt(pes));
        Vm vm = new VmSimple(id,VM_MIPS, pes);
        vm
            .setRam(VM_RAM).setBw((long)VM_BW).setSize(VM_SIZE)
            .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        vm.getUtilizationHistory().enable();
        return vm;
    }


    public Cloudlet createCloudlet(int cloudletId, Vm vm,DatacenterBroker broker, UtilizationModel cpuUtilizationModel) {

        UtilizationModel utilizationModelFull = new UtilizationModelFull();
        final Cloudlet cloudlet =
//            new CloudletSimple(CLOUDLET_LENGHT+random.nextInt(CLOUDLET_LENGHT),vm.getNumberOfPes())
            new CloudletSimple(CLOUDLET_LENGHT,vm.getNumberOfPes())
                .setFileSize(CLOUDLET_FILESIZE)
                .setOutputSize(CLOUDLET_OUTPUTSIZE)
                .setUtilizationModelCpu(cpuUtilizationModel)
                .setUtilizationModelRam(utilizationModelFull)
                .setUtilizationModelBw(utilizationModelFull);
        broker.bindCloudletToVm(cloudlet, vm);
        return cloudlet;
    }


}
