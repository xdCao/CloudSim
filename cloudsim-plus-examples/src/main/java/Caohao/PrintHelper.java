package Caohao;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostStateHistoryEntry;
import org.cloudbus.cloudsim.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Caohao.Constants.HOST_INITIAL_PES;
import static Caohao.Constants.HOST_MIPS;
import static Caohao.Constants.SCHEDULE_INTERVAL;

/**
 * created by xdCao on 2018/4/26
 */

public class PrintHelper {


    public static void printEnergy(List<Host> hostList){

        double energy=0;
        System.out.println("-------------------------------------------------------------------------------------------");
        for (Host host:hostList){
            double hostEne= getHostEnergy(host);
            System.out.printf("Host: %6d | Energy: %6.2f|\n",host.getId(), hostEne);
            System.out.println("-------------------------------------------------------------------------------------------");
            energy+=hostEne;
        }
        System.out.printf("total energy: %6.2f",energy);
        System.out.println();
        System.out.println("-------------------------------------------------------------------------------------------");

    }


    public static double getHostEnergy(Host host) {

        double energy=0;

        List<HostStateHistoryEntry> stateHistory = host.getStateHistory();
        for (int i = 0; i < stateHistory.size(); i++) {
            if (i!=stateHistory.size()-1){
                double cpuUsage=stateHistory.get(i).getAllocatedMips()/(HOST_MIPS*HOST_INITIAL_PES);
                double timeDif=stateHistory.get(i+1).getTime()-stateHistory.get(i).getTime();
                energy+=host.getPowerModel().getPower(cpuUsage)*timeDif;
            }else {
                double cpuUsage=stateHistory.get(i).getAllocatedMips()/(HOST_MIPS*HOST_INITIAL_PES);
                energy+=host.getPowerModel().getPower(cpuUsage)*SCHEDULE_INTERVAL;
            }
        }

        return energy;

    }


    public static void printHistory(Host host){
        if(printHostStateHistory(host)) {
            printHostCpuUsageAndPowerConsumption(host);
        }
    }


    public static boolean printHostStateHistory(Host host) {
        if(host.getStateHistory().stream().anyMatch(HostStateHistoryEntry::isActive)) {
            System.out.printf("\nHost: %6d State getHostEnergy\n", host.getId());
            System.out.println("-------------------------------------------------------------------------------------------");
            host.getStateHistory().forEach(System.out::print);
            System.out.println();
            return true;
        } else {
            System.out.printf("Host: %6d was powered off during all the simulation\n", host.getId());
        }
        return false;
    }


    public static void printHostCpuUsageAndPowerConsumption(final Host host) {
        System.out.printf("Host: %6d | CPU Usage | Power Consumption\n", host.getId());
        System.out.println("-------------------------------------------------------------------------------------------");

        List<HostStateHistoryEntry> stateHistory = host.getStateHistory();
        for (HostStateHistoryEntry entry:stateHistory){
            double cpuUsage=entry.getAllocatedMips()/(HOST_MIPS*HOST_INITIAL_PES);
            System.out.printf("Time: %6.2f | %9.4f | %.2f\n", entry.getTime(), cpuUsage , host.getPowerModel().getPower(cpuUsage));
        }

        System.out.println();

    }

    public void showCpuUtilizationForAllHosts(List<Host> hostList) {
        Log.printLine("\nHosts CPU utilization history for the entire simulation period");
        int numberOfUsageHistoryEntries = 0;
        final double interval = 1;
        for (Host host : hostList) {
            double mipsByPe = host.getTotalMipsCapacity() / (double)host.getNumberOfPes();
            Log.printFormattedLine("Host %d: Number of PEs %2d, MIPS by PE %.0f", host.getId(), host.getNumberOfPes(), mipsByPe);
            for(HostStateHistoryEntry history: host.getStateHistory()){
                numberOfUsageHistoryEntries++;
                Log.printFormattedLine(
                    "\tTime: %2.0f CPU Utilization (MIPS): %.0f ",
                    history.getTime(), history.getAllocatedMips());
            }
        }
        if(numberOfUsageHistoryEntries == 0) {
            Log.printLine(" No CPU usage history was found");
        }
    }

}
