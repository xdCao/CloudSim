package Caohao;

import Caohao.MigrationPolicy.*;

import java.io.File;
import java.io.IOException;

import static Caohao.workload.WorkloadProducer.initVmWorkLoad;

/**
 * created by xdCao on 2018/4/26
 */

public class Main {

    public static int num=1;


    public static void main(String[] args) throws IOException {


        File dir=new File("/Users/caohao/"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+"/");
        dir.mkdir();


            for (int j = 0; j < 100; j++) {

                initVmWorkLoad();

                //一共画三张图，横坐标分别为
                //1.请求的虚拟机数量
                //2.系统中物理机的规模
                //3.系统的运行时间

                uniform();

                improvedGreedy();
//
                FirstFit();
////
                FirstFitMig();

                num++;

            }

//        for (int i = 0; i < 100; i++) {
//
//            initVmWorkLoad();
//
//            //一共画三张图，横坐标分别为
//            //1.请求的虚拟机数量
//            //2.系统中物理机的规模
//            //3.系统的运行时间
//
//            uniform();
//
//            improvedGreedy();
////
//            FirstFit();
//////
//            FirstFitMig();
//
//            num++;
//
//        }

//        initVmWorkLoad();
//
//        //一共画三张图，横坐标分别为
//        //1.请求的虚拟机数量
//        //2.系统中物理机的规模
//        //3.系统的运行时间
//
//        uniform();
//
//        improvedGreedy();
////
//        FirstFit();
//////
//        FirstFitMig();
//
//        staticVar();

//        worstFit();

//        MySim();




    }



    private static void improvedGreedy() throws IOException {


        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new ImprovedGreedy(),"/Users/caohao/"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+"/greedy"+num+"-"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+".txt","/Users/caohao/greedyPM.txt","/Users/caohao/greedyVM.txt");
//        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new EnergyMigrationPolicy(),"E://greedy.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }

    public static void uniform() throws IOException {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new UniformedDynamicVar(),"/Users/caohao/"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+"/uniform"+num+"-"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+".txt","/Users/caohao/uniformPM.txt","/Users/caohao/uniformVM.txt");
//        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new UniformedDynamicVar(),"E://uniform.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }

    public static void FirstFit() throws IOException {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new FirstFit(),"/Users/caohao/"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+"/firstFit"+num+"-"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+".txt","/Users/caohao/firstFitPM.txt","/Users/caohao/firstFitVM.txt");
//        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new FirstFit(),"E://firstFit.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }


    public static void FirstFitMig() throws IOException {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new PABFD(),"/Users/caohao/"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+"/firstFitMig"+num+"-"+"vm"+Constants.VMS+"pm"+Constants.HOSTS+"lambda"+Constants.lamda+".txt","/Users/caohao/firstFitMigPM.txt","/Users/caohao/firstFitMigVM.txt");
//        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new PABFD(),"E://firstFitMig.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }









    /*--------------------------------------------------------------------------------------------------*/

    public static void MySim() throws IOException {


        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new EnergyMigrationPolicy(),"/Users/caohao/share.txt","/Users/caohao/sharePM.txt","/Users/caohao/shareVM.txt");
//        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new EnergyMigrationPolicy(),"E://share.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }




    public static void staticVar() throws IOException {
        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new StaticVarThreshold(),"/Users/caohao/staticVar.txt","/Users/caohao/staticVarPM.txt","/Users/caohao/staticVarVM.txt");
//        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new StaticVarThreshold(),"E://staticVar.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }




    public static void worstFit() throws IOException {
//
        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new WorstFit(),"/Users/caohao/worstFit.txt","/Users/caohao/worstFitPM.txt","/Users/caohao/worstFitVM.txt");
//        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new WorstFit(),"E://worstFit.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }




}
