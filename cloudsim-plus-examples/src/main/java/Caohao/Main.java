package Caohao;

import Caohao.MigrationPolicy.*;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationBestFitStaticThreshold;
import org.cloudbus.cloudsim.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by xdCao on 2018/4/26
 */

public class Main {


    public static void main(String[] args) throws IOException {
//
        uniform();

        FirstFit();
//
        FirstFitMig();
//
        staticVar();

//        worstFit();

//        MySim();




    }

    public static void MySim() throws IOException {


        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new EnergyMigrationPolicy(),"/Users/caohao/share.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }

    public static void uniform() throws IOException {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new UniformedDynamicVar(),"/Users/caohao/uniform.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }


    public static void staticVar() throws IOException {
        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new StaticVarThreshold(),"/Users/caohao/staticVar.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }




    public static void worstFit() throws IOException {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new WorstFit(),"/Users/caohao/worstFit.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }


    public static void FirstFit() throws IOException {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new FirstFit(),"/Users/caohao/firstFit.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }


    public static void FirstFitMig() throws IOException {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new FirstFitMigration(),"/Users/caohao/firstFitMig.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }


}
