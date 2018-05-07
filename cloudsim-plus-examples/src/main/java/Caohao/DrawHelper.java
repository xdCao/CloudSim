package Caohao;

import Caohao.Model.EnergyTimeStamp;
import Caohao.Model.PowerTimeStamp;
import sun.font.GraphicComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * created by xdCao on 2018/5/7
 */

public class DrawHelper{

    public static List<EnergyTimeStamp> getTimeEnergyMap(List<PowerTimeStamp> powerList){

        List<EnergyTimeStamp> energyTimeStampList=new ArrayList<>();

        double lastTime=0;

        double energy=0;

        for (int i = 0; i < powerList.size(); i++) {
            PowerTimeStamp powerTimeStamp=powerList.get(i);
            energy+=powerTimeStamp.getPower()*(powerTimeStamp.getTime()-lastTime);
            energyTimeStampList.add(new EnergyTimeStamp(powerTimeStamp.getTime(),energy));
            lastTime=powerTimeStamp.getTime();
        }

        return energyTimeStampList;
    }

}
