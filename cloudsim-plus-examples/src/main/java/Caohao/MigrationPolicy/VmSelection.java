package Caohao.MigrationPolicy;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * created by xdCao on 2018/5/3
 */

public class VmSelection extends PowerVmSelectionPolicy {



    @Override
    public Vm getVmToMigrate(Host host) {
        final List<? extends Vm> migratableVms = getMigratableVms(host);
        if (migratableVms.isEmpty()) {
            return Vm.NULL;
        }

        final Predicate<Vm> inMigration = Vm::isInMigration;
        final Comparator<? super Vm> cpuUsageComparator =
            Comparator.comparingDouble(vm -> vm.getCpuPercentUsage(vm.getSimulation().clock()));
        final Optional<? extends Vm> optional = migratableVms.stream()
            .filter(inMigration.negate())
            .min(cpuUsageComparator);
        return (optional.isPresent() ? optional.get() : Vm.NULL);
    }




}
