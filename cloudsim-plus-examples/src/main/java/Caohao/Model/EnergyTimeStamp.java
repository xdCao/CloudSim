package Caohao.Model;

/**
 * created by xdCao on 2018/5/7
 */

public class EnergyTimeStamp {

    private double time;

    private double energy;

    public EnergyTimeStamp(double time, double energy) {
        this.time = time;
        this.energy = energy;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }
}
