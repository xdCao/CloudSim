package Caohao.entity;

import org.cloudbus.cloudsim.vms.VmSimple;

/**
 * created by xdCao on 2018/5/2
 */

public class QosVm extends VmSimple {

    private double qos;

    public QosVm(int id, long mipsCapacity, long numberOfPes,double qos) {
        super(id, mipsCapacity, numberOfPes);
        this.qos=qos;
    }

    public double getQos() {
        return qos;
    }

    public void setQos(double qos) {
        this.qos = qos;
    }
}
