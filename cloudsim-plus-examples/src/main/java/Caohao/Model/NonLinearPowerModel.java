package Caohao.Model;

import org.cloudbus.cloudsim.power.models.PowerModelAbstract;
import org.cloudbus.cloudsim.power.models.PowerModelSimple;

import java.util.function.UnaryOperator;

/**
 * created by xdCao on 2018/4/24
 */

public class NonLinearPowerModel extends PowerModelAbstract {

    public NonLinearPowerModel() {
    }

    @Override
    protected double getPowerInternal(double utilization) throws IllegalArgumentException {
        if (utilization==0.00){
            return 0;
        }
        if (utilization<=0.125){
            return 500+17.6*utilization*100;
        }else {
            return 720+1.82857*(utilization-0.125)*100;
        }
    }


    @Override
    public double getMaxPower() {
        return 879.9999;
    }
}
