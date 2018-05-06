package Caohao;

import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static Caohao.MigrationPolicy.EnergyMigrationPolicy.getMyOverUtilizationThreshold;

/**
 * created by xdCao on 2018/5/6
 */

public class CalHelper {

    //计算PM中的QOS方差
    public static double calDistribution(Host host) {

        List<QosVm> vmList = host.getVmList();
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
            return var;
        }

    }

    //计算PM的QOS均值
    public static double calAvaQos(Host host){
        List<QosVm> vmList = host.getVmList();
        if (vmList.size()==0){
            return 0.0;
        }else {
            double sum=0;
            for (QosVm vm:vmList){
                sum+=vm.getQos();
            }
            double ava=sum/vmList.size();
            return ava;
        }
    }

    public static double calDistributionNext(Host host, QosVm vm) {

        List<QosVm> vmList = host.getVmList();
        if (vmList.size()==0){
            return 0.0;
        }else {
            double sum=0;
            List<Double> qosList=new ArrayList<>();
            for (QosVm qosVm:vmList){
                sum+=qosVm.getQos();
                qosList.add(qosVm.getQos());
            }
            sum+=vm.getQos();
            double ava=sum/(qosList.size()+1);
            double var=0;
            for (QosVm qosVm:vmList){
                var+=Math.pow(qosVm.getQos()-ava,2);
            }
            var+=Math.pow(vm.getQos()-ava,2);
            return var;
        }

    }

    public static boolean isHostOverloaded(Host host) {
        double upperThreshold = getMyOverUtilizationThreshold(host);
        double var = calDistribution(host);
        return getHostCpuUtilizationPercentage(host) > upperThreshold||var>0;

    }


    public static boolean isHostUnderloaded(Host host) {
        Optional<Double> aDouble = host.getVmList().stream()
            .filter(vm -> !vm.isInMigration())
            .max(Comparator.comparingDouble(Vm::getCurrentRequestedTotalMips))
            .map(vm -> vm.getCurrentRequestedTotalMips());
        if (aDouble.isPresent()){
            return (getHostTotalRequestedMips(host)-aDouble.get())/host.getTotalMipsCapacity()<0.125;
        }else {
            return false;
        }
//        return getHostCpuUtilizationPercentage(host)<0.125;
    }


    public static double getHostCpuUtilizationPercentage(final Host host) {
//        return getHostTotalRequestedMips(host) / host.getTotalMipsCapacity();
        return getHostTotalAllocatedMips(host) / host.getTotalMipsCapacity();
    }

    public static double getHostTotalAllocatedMips(Host host){
        List<Vm> vmList = host.getVmList();
        double sum=0;
        for (Vm vm:vmList){
            sum+=host.getTotalAllocatedMipsForVm(vm);
        }
        return sum;
    }

    public static double getHostTotalRequestedMips(final Host host) {
        return host.getVmList().stream()
            .mapToDouble(Vm::getCurrentRequestedTotalMips)
            .sum();
    }




}
