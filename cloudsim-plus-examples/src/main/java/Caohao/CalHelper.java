package Caohao;

import Caohao.entity.QosVm;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static Caohao.Constants.VAR_THRESHOLD;


/**
 * created by xdCao on 2018/5/6
 */

public class CalHelper {

    //计算PM中的QOS方差
    public static double calDistribution(Host host) {

        List<QosVm> vmList = host.getVmList();
        if (vmList.size()==0){
            return 0.01;
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

    public static double calDistributionRemoveNext(Host host, QosVm vm) {

        List<QosVm> vmList = host.getVmList();
        if (vmList.size()==0||vmList.size()==1){
            return 0.01;
        }else {
            double sum=0;
            List<Double> qosList=new ArrayList<>();
            for (QosVm qosVm:vmList){
                sum+=qosVm.getQos();
                qosList.add(qosVm.getQos());
            }
            sum-=vm.getQos();
            double ava=sum/(qosList.size()-1);
            double var=0;
            for (QosVm qosVm:vmList){
                var+=Math.pow(qosVm.getQos()-ava,2);
            }
            var-=Math.pow(vm.getQos()-ava,2);
            return var/(vmList.size()-1);
        }

    }

    public static double calDistributionAddNext(Host host, QosVm vm) {
        List<QosVm> vmList = host.getVmList();
        if (vmList.size()==0){
            return 0.01;
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
            return var/(vmList.size()+1);
        }
    }

    public static boolean isHostOverloaded(Host host) {
//
//        if (isHostUnderloaded(host))
//            return false;

        double upperThreshold = getMyOverUtilizationThreshold(host);
        double var = calDistribution(host);
//        return getHostCpuUtilizationPercentage(host) > upperThreshold||var>VAR_THRESHOLD;
        return getHostCpuUtilizationPercentage(host) > upperThreshold;
    }

    public static double getMyOverUtilizationThreshold(Host host) {
        List<QosVm> vmList = host.getVmList();
        Optional<Double> aDouble = vmList.stream().min(Comparator.comparingDouble(QosVm::getQos)).map(QosVm::getQos);
        return aDouble.orElse(1.0);
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

    public static double getHostCpuPercentAfterDeallocate(Host host,Vm vm){

        double percent = (getHostTotalAllocatedMips(host) - vm.getCurrentRequestedTotalMips()) / host.getTotalMipsCapacity();

        return percent<0?0:percent;

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


    public static double getHostCpuUtilizationPercentageNext(Host host,Vm vm) {
        return (getHostTotalAllocatedMips(host)+vm.getCurrentRequestedTotalMips()) / host.getTotalMipsCapacity();
    }


}
