package Caohao;

import Caohao.MigrationPolicy.EnergyMigrationPolicy;
import Caohao.MigrationPolicy.FirstFit;
import Caohao.MigrationPolicy.FirstFitMigration;
import Caohao.MigrationPolicy.WorstFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationBestFitStaticThreshold;
import org.cloudbus.cloudsim.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * created by xdCao on 2018/4/26
 */

public class Main {


    public static void main(String[] args) {
        MySim();
////
//        worstFit();
////
//        FirstFit();

//        FirstFitMig();

    }

    public static void MySim() {


        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new EnergyMigrationPolicy(),"/Users/caohao/share.txt");

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }

    public static void worstFit() {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new WorstFit(),"/Users/caohao/worstFit.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }


    public static void FirstFit() {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new FirstFit(),"/Users/caohao/firstFit.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());
    }


    public static void FirstFitMig(){

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new FirstFitMigration(),"/Users/caohao/firstFitMig.txt");
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }


}
