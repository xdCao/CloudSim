package Caohao;

import Caohao.MigrationPolicy.EnergyMigrationPolicy;
import Caohao.MigrationPolicy.WorstFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationMedianAbsoluteDeviation;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.util.Log;

import java.util.ArrayList;
import java.util.List;

import static Caohao.Constants.HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION;
import static Caohao.Constants.SCHEDULE_INTERVAL;

/**
 * created by xdCao on 2018/4/26
 */

public class Main {


    public static void main(String[] args) {
//          doParallel();
//
        doSingle1();
//
//        doSingle2();


    }

    public static void doSingle1() {


        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new EnergyMigrationPolicy());

        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());

    }

    public static void doSingle2() {

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new WorstFit());
        migrationWithEnergy.run();
        migrationWithEnergy.print();
        System.out.println("simulation time: "+migrationWithEnergy.getTime());


    }

    public static void doParallel() {

        Log.disable();

        List<MigrationWithEnergy> simulationList = new ArrayList<>(2);


        simulationList.add(
            new MigrationWithEnergy(new EnergyMigrationPolicy())
        );


        simulationList.add(
            new MigrationWithEnergy(new WorstFit())
        );


        final long startTimeMilliSec = System.currentTimeMillis();

        simulationList.parallelStream().forEach(MigrationWithEnergy::run);

        final long finishTimeMilliSec = System.currentTimeMillis() - startTimeMilliSec;

        Log.enable();

        simulationList.forEach(MigrationWithEnergy::print);

        for (MigrationWithEnergy migrationWithEnergy:simulationList){
            PrintHelper.printEnergy(migrationWithEnergy.getHostList());
            System.out.println("simulation time: "+migrationWithEnergy.getTime());
        }


    }


}
