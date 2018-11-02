package Caohao.MigrationPolicy;

import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static Caohao.Constants.HOSTS;
import static Caohao.Constants.HOST_INITIAL_PES;
import static Caohao.Constants.HOST_MIPS;

public class UniformedDynamicVar extends EnergyMigrationPolicy{


    private double minLoad=1;


    @Override
    public double getDynamicVar() {

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

        if (curLoad==0){
            curLoad=0.00001;
        }

        if (curLoad<minLoad){
            minLoad=curLoad;
        }

//        System.out.println("------------------------------------------------------------minLoad"+minLoad+"--curLoad"+curLoad+"---threshold"+minLoad/curLoad);

//        return (minLoad/curLoad);

        return getTotalVar(vmList)*(minLoad/curLoad);

    }

    @Override
    public Optional<Host> findHostForVm(Vm vm) {
        List<Host> hostList = getHostList();
        Optional<Host> max = hostList.stream()
            .filter(e -> e.isSuitableForVm(vm))
            .filter(e->isNotHostOverloadedAfterAllocation(e,vm))
//            .filter(e->isNotHostOverloadedAfterAllocation(e,vm))
            .max(Comparator.comparingDouble(e->allocateCompare(e,vm)));
        return max;
    }



    public double getTotalVar(List<QosVm> vmList){
        if (vmList.size()==0){
            return 0.0;
        }else {
            double sum=0;
            List<Double> qosList=new ArrayList<>();
            for (QosVm vm:vmList){
                sum+=vm.getQos();
                qosList.add(vm.getQos());
            }
            double ava=sum/qosList.size();
            double var=0;
            for (QosVm vm:vmList){
                var+=Math.pow(vm.getQos()-ava,2);
            }
            return var/vmList.size();
        }
    }



}
