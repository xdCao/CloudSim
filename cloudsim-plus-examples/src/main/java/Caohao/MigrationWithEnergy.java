package Caohao;

import Caohao.MigrationPolicy.EnergyMigrationPolicy;
import Caohao.Model.NonLinearPowerModel;
import Caohao.entity.QosCloudlet;
import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
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
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.vms.VmStateHistoryEntry;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.listeners.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static Caohao.CalHelper.getHostCpuUtilizationPercentage;
import static Caohao.CalHelper.isHostOverloaded;
import static Caohao.CalHelper.isHostUnderloaded;
import static Caohao.Constants.*;
import static Caohao.MigrationPolicy.EnergyMigrationPolicy.getMyOverUtilizationThreshold;

/**
 * created by xdCao on 2018/4/24
 */

public class MigrationWithEnergy implements Runnable{

    private Random random=new Random();

    private CloudSim simulation;

    private VmAllocationPolicy allocationPolicy;

    private List<Host> hostList;

    private final List<QosVm> vmList = new ArrayList<>(VMS);

    private final List<Cloudlet> cloudletList=new ArrayList<>(VMS);

    private DatacenterBroker broker;

    private Datacenter dc;

    private double time;

    private int index=0;

    private int taskIndex=0;

    public File file;

    FileWriter fw = null;
    BufferedWriter bw = null;
    PrintWriter pw = null;


    MigrationWithEnergy(VmAllocationPolicy allocationPolicy){
        this.allocationPolicy=allocationPolicy;
    }


    @Override
    public void run() {

        file=new File("log.txt");



        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
        } catch (IOException e) {
            System.exit(0);
        }

        simulation = new CloudSim();

        simulation.addOnClockTickListener(new EventListener<EventInfo>() {
            @Override
            public void update(EventInfo info) {
                pw.printf("time: %6.2f\n",info.getTime());
                for (Host host:hostList){
                    pw.println();
                    pw.printf("Host%3d: %6.2f",host.getId(), getHostCpuUtilizationPercentage(host));
                    if (isHostOverloaded(host)){
                        pw.printf("                                             overloaded\n");
                    }else if(isHostUnderloaded(host)){
                        pw.printf("                                             underloaded\n");
                    }else {
                        pw.printf("\n");
                    }

                    List<QosVm> vmList = host.getVmList();
                    for (QosVm vm:vmList){
                        pw.printf("vm%3d: %6.2f  %2d  isInMigration: "+vm.isInMigration()+"\n",vm.getId(),vm.getQos(),vm.getNumberOfPes());
                    }
                }
                pw.println();
                pw.println();
            }
        });


        Datacenter datacenter0 = createDatacenter(allocationPolicy);
        datacenter0.setLog(true);

//        broker = new DatacenterBrokerSimple(simulation);
        broker=new DatacenterBrokerSimple(simulation);


        dynamicCreateVmsAndTasks(broker);

//        simulation.terminateAt(74);

        time = simulation.start();

        try {
            pw.close();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
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

        dc = new DatacenterSimple(simulation, hostList, allocationPolicy);
        dc.setSchedulingInterval(SCHEDULE_INTERVAL).setLog(true);
        return dc;
    }

    public Host createHost(int numberOfPes, long mipsByPe) {
        List<Pe> peList = createPeList(numberOfPes, mipsByPe);
        Host host =
            new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
        host
            .setRamProvisioner(new ResourceProvisionerSimple())
            .setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(new VmSchedulerTimeShared(0.99999999));
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

//        printCloudletTime();

        System.out.println("\n    WHEN A HOST CPU ALLOCATED MIPS IS LOWER THAN THE REQUESTED, IT'S DUE TO VM MIGRATION OVERHEAD)\n");

//        for (Host host:hostList){
//            PrintHelper.printHistory(host);
//        }

        PrintHelper.printEnergy(hostList);

        Log.printConcatLine( "finished!");
    }



    public void printCloudletTime() {
        System.out.println("---------------------cloudlet cpuTime--------------------");
        for (Cloudlet cloudlet:cloudletList){
            System.out.printf("%2d|%6.2f|%6.2f|%6d\n",cloudlet.getId(),cloudlet.getActualCpuTime(),cloudlet.getWallClockTime(dc),cloudlet.getLength());
        }
    }

    /*--------------------------------------------------------------------------------动态请求----------------------*/

    private void submitNewVmsAndCloudletsToBroker(CloudletVmEventInfo eventInfo, DatacenterBroker broker) {

        eventInfo.getVm().getHost().destroyVm(eventInfo.getVm());

        if (vmList.size()<VMS){
            Log.printFormattedLine("\n\t#Cloudlet %d finished. Submitting %d new VMs to the broker\n",
                eventInfo.getCloudlet().getId(),1);
            dynamicCreateVmsAndTasks(broker);
        }


    }


    private void dynamicCreateVmsAndTasks(DatacenterBroker broker){

        QosVm vm=createVm(vmList.size(),broker,Constants.vmPes[index]);
//        Vm vm=createVm(vmList.size(),broker,VM_PES);
        index++;
        vmList.add(vm);
        broker.submitVm(vm);

        UtilizationModelFull um = new UtilizationModelFull();
        Cloudlet cloudlet=createCloudlet(cloudletList.size(),vm,broker,um);
        cloudlet.addOnFinishListener(eventInfo->submitNewVmsAndCloudletsToBroker(eventInfo,broker));
        cloudletList.add(cloudlet);
        broker.submitCloudlet(cloudlet);
        broker.bindCloudletToVm(cloudlet, vm);//这行代码的顺序非常关键


    }
    public QosVm createVm(int id,DatacenterBroker broker, int pes) {
        QosVm vm = new QosVm(id,VM_MIPS, pes,Constants.taskQos[index]);
        vm
            .setRam(VM_RAM).setBw((long)VM_BW).setSize(VM_SIZE)
            .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        vm.getUtilizationHistory().enable();
        return vm;
    }


    public Cloudlet createCloudlet(int cloudletId, Vm vm,DatacenterBroker broker, UtilizationModel cpuUtilizationModel) {

        UtilizationModel utilizationModelFull = new UtilizationModelFull();
        final Cloudlet cloudlet =
            new QosCloudlet(Constants.taskLength[taskIndex],(int)vm.getNumberOfPes(),Constants.taskQos[taskIndex])
                .setFileSize(CLOUDLET_FILESIZE)
                .setOutputSize(CLOUDLET_OUTPUTSIZE)
                .setUtilizationModelCpu(cpuUtilizationModel)
                .setUtilizationModelRam(utilizationModelFull)
                .setUtilizationModelBw(utilizationModelFull);
        taskIndex++;
        return cloudlet;
    }

    /*-----------------------------------------------------getter-----------------------------------------------------*/

    public List<Host> getHostList() {
        return hostList;
    }


    public double getTime() {
        return time;
    }

}
