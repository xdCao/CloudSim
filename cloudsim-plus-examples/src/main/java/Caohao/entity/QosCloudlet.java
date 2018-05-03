package Caohao.entity;

import org.cloudbus.cloudsim.cloudlets.CloudletSimple;

/**
 * created by xdCao on 2018/5/2
 */

public class QosCloudlet extends CloudletSimple{

    private double qos;

    public QosCloudlet(long cloudletLength, int pesNumber,double qos) {
        super(cloudletLength, pesNumber);
        this.qos=qos;
    }

    public double getQos() {
        return qos;
    }

    public void setQos(double qos) {
        this.qos = qos;
    }


}
