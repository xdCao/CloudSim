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
import org.cloudsimplus.listeners.EventListener;

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


    @Override
    public void run() {

        simulation = new CloudSim();

        Datacenter datacenter0 = createDatacenter(allocationPolicy);
        datacenter0.setLog(false);
        broker = new DatacenterBrokerSimple(simulation);

        dynamicCreateVmsAndTasks(broker);

        simulation.start();

    }



    public MigrationWithEnergy(VmAllocationPolicy allocationPolicy){
        this.allocationPolicy=allocationPolicy;
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


    private List<Vm> createListOfScalableVms(final int numberOfVms) {
        List<Vm> newList = new ArrayList<>(numberOfVms);
        for (int i = 0; i < numberOfVms; i++) {
            Vm vm = createVm();
            createHorizontalVmScaling(vm);
            newList.add(vm);
        }

        return newList;
    }

    /**
     * Creates a {@link HorizontalVmScaling} object for a given VM.
     *
     * @param vm the VM for which the Horizontal Scaling will be created
     * @see #createListOfScalableVms(int)
     */
    private void createHorizontalVmScaling(Vm vm) {
        HorizontalVmScaling horizontalScaling = new HorizontalVmScalingSimple();
        horizontalScaling
            .setVmSupplier(this::createVm)
            .setOverloadPredicate(this::isVmOverloaded);
        vm.setHorizontalScaling(horizontalScaling);
    }



    private boolean isVmOverloaded(Vm vm) {
        return vm.getCpuPercentUsage() > 0.7;
    }




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


    private Vm createVm() {
        int id=vmList.size();
        return new VmSimple(id, 1000, 2)
            .setRam(512).setBw(1000).setSize(10000)
            .setCloudletScheduler(new CloudletSchedulerTimeShared());
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


    private void submitNewVmsAndCloudletsToBroker(CloudletVmEventInfo eventInfo, DatacenterBroker broker) {

        if (vmList.size()>=VMS){
            return;
        }

        Log.printFormattedLine("\n\t#Cloudlet %d finished. Submitting %d new VMs to the broker\n",
            eventInfo.getCloudlet().getId(),1);


        dynamicCreateVmsAndTasks(broker);

    }






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





}
