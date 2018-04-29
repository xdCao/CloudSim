package org.cloudbus.cloudsim.vms;

import java.util.Collections;
import java.util.List;

/**
 * A class that implements the Null Object Design Pattern for {@link UtilizationHistory}
 * objects.
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.4
 * @see UtilizationHistory#NULL
 */
final class UtilizationHistoryNull implements UtilizationHistory {
    @Override public double getUtilizationMad() { return 0; }
    @Override public double getUtilizationMean() { return 0; }
    @Override public double getUtilizationVariance() { return 0; }
    @Override public void addUtilizationHistory(double time) {/**/}
    @Override public List<Double> getHistory() { return Collections.EMPTY_LIST; }
    @Override public double getPreviousTime() { return 0; }
    @Override public void setPreviousTime(double previousTime) {/**/}
    @Override public boolean isEnabled() { return false; }
    @Override public void enable() {/**/}
    @Override public void disable() {/**/}
    @Override public int getMaxHistoryEntries() { return 0; }
    @Override public void setMaxHistoryEntries(int maxHistoryEntries) {/**/}
}
