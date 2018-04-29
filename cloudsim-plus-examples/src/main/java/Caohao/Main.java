package Caohao;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationMedianAbsoluteDeviation;
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.util.Log;

import java.util.ArrayList;
import java.util.List;

import static Caohao.Constants.HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION;

/**
 * created by xdCao on 2018/4/26
 */

public class Main {

    public static void main(String[] args) {
//        doParallel();

        doSingle1();

//        doSingle2();


    }

    public static void doSingle1() {



        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(new VmAllocationPolicySimple());
        migrationWithEnergy.run();
        migrationWithEnergy.print();


    }

    public static void doSingle2() {

        final VmAllocationPolicyMigrationStaticThreshold fallback =
            new VmAllocationPolicyMigrationStaticThreshold(
                new PowerVmSelectionPolicyMinimumUtilization(), HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION);


        VmAllocationPolicy allocationPolicy =
            new VmAllocationPolicyMigrationMedianAbsoluteDeviation(
                new PowerVmSelectionPolicyMinimumUtilization(),
                HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION+0.2, fallback);

        MigrationWithEnergy migrationWithEnergy = new MigrationWithEnergy(allocationPolicy);
        migrationWithEnergy.run();
        migrationWithEnergy.print();


    }

    public static void doParallel() {

        Log.disable();

        List<MigrationWithEnergy> simulationList = new ArrayList<>(2);


        simulationList.add(
            new MigrationWithEnergy(new VmAllocationPolicySimple())
        );


        final VmAllocationPolicyMigrationStaticThreshold fallback =
            new VmAllocationPolicyMigrationStaticThreshold(
                new PowerVmSelectionPolicyMinimumUtilization(), HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION);


        VmAllocationPolicy allocationPolicy =
            new VmAllocationPolicyMigrationMedianAbsoluteDeviation(
                new PowerVmSelectionPolicyMinimumUtilization(),
                HOST_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION+0.2, fallback);

        simulationList.add(
            new MigrationWithEnergy(allocationPolicy)
        );


        final long startTimeMilliSec = System.currentTimeMillis();

        simulationList.parallelStream().forEach(MigrationWithEnergy::run);

        final long finishTimeMilliSec = System.currentTimeMillis() - startTimeMilliSec;

        Log.enable();

        simulationList.forEach(MigrationWithEnergy::print);
    }


}
