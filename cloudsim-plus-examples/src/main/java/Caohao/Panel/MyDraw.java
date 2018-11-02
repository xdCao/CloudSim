package Caohao.Panel;

import Caohao.Model.EnergyTimeStamp;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * created by xdCao on 2018/5/7
 */

public class MyDraw extends JFrame {

    private MyPanel mp = null;

    private List<EnergyTimeStamp> list=null;

    public MyDraw(java.util.List<EnergyTimeStamp> list){

        list=list;

        mp = new MyPanel();

        this.add(mp);
        this.setSize(400,300);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    class MyPanel extends JPanel{

        @Override
        public void paint(Graphics g)
        {
            super.paint(g);

            for (int i = 1; i < list.size(); i++) {

                EnergyTimeStamp last=list.get(i-1);
                EnergyTimeStamp cur=list.get(i);


            }

        }
    }

}
