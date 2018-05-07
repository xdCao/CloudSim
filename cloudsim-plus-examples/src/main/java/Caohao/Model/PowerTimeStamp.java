package Caohao.Model;

/**
 * created by xdCao on 2018/5/7
 */

public class PowerTimeStamp {

    private double time;

    private double power;

    public PowerTimeStamp(double time, double power) {
        this.time = time;
        this.power = power;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }
}
