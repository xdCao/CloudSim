package Caohao;

import Caohao.MigrationPolicy.EnergyMigrationPolicy;
import Caohao.Model.EnergyTimeStamp;
import Caohao.Model.NonLinearPowerModel;
import Caohao.Model.PowerTimeStamp;
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
import org.cloudsimplus.listeners.EventListener;

import java.io.*;
import java.util.*;

import static Caohao.CalHelper.*;
import static Caohao.Constants.*;


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

    private DatacenterBrokerSimple broker;

    private Datacenter dc;

    private double time;

    private int index=0;

    private int taskIndex=0;

    public File file;

    public List<PowerTimeStamp> timePowerMap=new ArrayList<>();

    public File share;

    public int clock=1;


    FileWriter fw = null;
    BufferedWriter bw = null;
    PrintWriter pw = null;


    FileWriter fw1 = null;
    BufferedWriter bw1 = null;
    PrintWriter pw1 = null;


    MigrationWithEnergy(VmAllocationPolicy allocationPolicy,String fileName){
        this.allocationPolicy=allocationPolicy;
        share=new File(fileName);
    }


    @Override
    public void run() {

        file=new File("log.txt");

        try {
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);

            fw1 = new FileWriter(share, false);
            bw1 = new BufferedWriter(fw1);
            pw1 = new PrintWriter(bw1);
        } catch (IOException e) {
            System.exit(0);
        }

        simulation = new CloudSim();

        simulation.addOnClockTickListener(new EventListener<EventInfo>() {
            @Override
            public void update(EventInfo info) {
                pw.printf("time: %6.2f   totalLoad: %6.4f   \n",info.getTime(),getLoad());
                for (Host host:hostList){
                    pw.println();
                    pw.printf("Host%3d: %6.2f  var:%6.6f",host.getId(), getHostCpuUtilizationPercentage(host),calDistribution(host));
                    if (isHostOverloaded(host)){
                        pw.printf("                              overloaded\n");
                    }
                    if(isHostUnderloaded(host)){
                        pw.printf("                                             underloaded\n");
                    }

                    pw.printf("\n");


                    List<QosVm> vmList = host.getVmList();
                    for (QosVm vm:vmList){
                        pw.printf("vm%3d: %6.2f  %2d  isInMigration: "+vm.isInMigration()+"\n",vm.getId(),vm.getQos(),vm.getNumberOfPes());
                    }
                }
                pw.println();
                pw.println();

                timePowerMap.add(new PowerTimeStamp(info.getTime(),getCurrentTotalPower()));

                if ((int)info.getTime()==clock){
                    if (vmList.size()<Constants.VMS){
                        dynamicCreateVmsAndTasks(broker);
                    }
                    clock++;
                }


            }
        });

        dc = createDatacenter(allocationPolicy);
        dc.setLog(false);


//        broker = new DatacenterBrokerSimple(simulation);
        broker=new DatacenterBrokerSimple(simulation);
        broker.setLog(false);


        dynamicCreateVmsAndTasks(broker);

//        simulation.terminateAt(TIME);

        time = simulation.start();

        List<EnergyTimeStamp> timeEnergyMap = DrawHelper.getTimeEnergyMap(timePowerMap);
        for (int i = 0; i < timeEnergyMap.size(); i++) {
            pw1.printf("%15.2f %15.2f",timeEnergyMap.get(i).getTime(),timeEnergyMap.get(i).getEnergy());
            pw1.println();
        }

//        pw.println();pw.println();
//
//        for (int i = 0; i < timeEnergyMap.size(); i++) {
//            pw.printf("%15.2f,",timeEnergyMap.get(i).getTime());
//        }
//        pw.println();
//        for (int i = 0; i < timeEnergyMap.size(); i++) {
//            pw.printf("%15.2f,",timeEnergyMap.get(i).getEnergy());
//        }


        try {
            pw.close();
            bw.close();
            fw.close();

            pw1.close();
            bw1.close();
            fw1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public double getLoad() {

        List<Host> hostList = getHostList();

        List<QosVm> vmList=new ArrayList<>();

        for (Host host:hostList){
            vmList.addAll(host.getVmList());
        }

        double vmCap=0;
        double pmCap=HOSTS*HOST_INITIAL_PES*HOST_MIPS;

        for (QosVm vm:vmList){

            vmCap+=vm.getCurrentRequestedTotalMips();

        }

        double curLoad=vmCap/pmCap;
        return curLoad;
    }

    private double getCurrentTotalPower() {

        double total=0;

        for (Host host:hostList){
            total+=host.getPowerModel().getPower(CalHelper.getHostCpuUtilizationPercentage(host));
        }

        return total;
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

    private void submitNewVmsAndCloudletsToBroker(CloudletVmEventInfo eventInfo, DatacenterBrokerSimple broker) {

        eventInfo.getVm().getHost().destroyVm(eventInfo.getVm());

//        if (vmList.size()<VMS){
//            Log.printFormattedLine("\n\t#Cloudlet %d finished. Submitting %d new VMs to the broker\n",
//                eventInfo.getCloudlet().getId(),1);
////            for (int i = 0; i < 10; i++) {
////                if (vmList.size()<VMS){
////                    dynamicCreateVmsAndTasks(broker);
////                }
////            }
//            dynamicCreateVmsAndTasks(broker);
////            dynamicCreateVmsAndTasks(broker);
////            dynamicCreateVmsAndTasks(broker);
////            dynamicCreateVmsAndTasks(broker);
//        }


    }


    private void dynamicCreateVmsAndTasks(DatacenterBrokerSimple broker){

            // todo 这里取模
            QosVm vm=createVm(vmList.size(),broker,Constants.vmPes[index%100]);
//        Vm vm=createVm(vmList.size(),broker,VM_PES);
            index++;
            vmList.add(vm);
            double delay=Constants.delay[index%100];
            broker.submitVm(vm);

            UtilizationModelFull um = new UtilizationModelFull();
            Cloudlet cloudlet=createCloudlet(cloudletList.size(),vm,broker,um);
//            cloudlet.setSubmissionDelay(delay);
        cloudlet.addOnFinishListener(eventInfo->submitNewVmsAndCloudletsToBroker(eventInfo,broker));
            cloudletList.add(cloudlet);
            broker.submitCloudlet(cloudlet);
            broker.bindCloudletToVm(cloudlet, vm);//这行代码的顺序非常关键

    }



    public QosVm createVm(int id,DatacenterBroker broker, int pes) {
        // todo 这里取模
        QosVm vm = new QosVm(id,VM_MIPS, pes,Constants.taskQos[index%100]-QOS_CHANGE);
        vm
            .setRam(VM_RAM).setBw((long)VM_BW).setSize(VM_SIZE)
            .setCloudletScheduler(new CloudletSchedulerSpaceShared());
        vm.getUtilizationHistory().enable();
        return vm;
    }


    public Cloudlet createCloudlet(int cloudletId, Vm vm,DatacenterBroker broker, UtilizationModel cpuUtilizationModel) {

        UtilizationModel utilizationModelFull = new UtilizationModelFull();
        final Cloudlet cloudlet =
            // todo 这里取模
            new QosCloudlet(Constants.taskLength[taskIndex%100]*Constants.taskLengthChange,(int)vm.getNumberOfPes(),Constants.taskQos[taskIndex%100])
                .setFileSize(CLOUDLET_FILESIZE)
                .setOutputSize(CLOUDLET_OUTPUTSIZE)
                .setUtilizationModelCpu(cpuUtilizationModel)
                .setUtilizationModelRam(utilizationModelFull)
                .setUtilizationModelBw(utilizationModelFull);

//        cloudlet.setSubmissionDelay(Constants.delay[taskIndex%100]);

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

    public List<PowerTimeStamp> getTimePowerMap() {
        return timePowerMap;
    }
}
