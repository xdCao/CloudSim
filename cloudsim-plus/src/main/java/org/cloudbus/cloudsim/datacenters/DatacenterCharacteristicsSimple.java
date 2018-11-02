/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.datacenters;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.Pe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.lists.PeList;

/**
 * Represents static properties of a Datacenter such as architecture, Operating
 * System (OS), management policy (time- or space-shared), cost and time zone at
 * which the resource is located along resource configuration. Each
 * {@link Datacenter} has to have its own instance of this class, since it
 * stores the Datacenter host list.
 *
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterCharacteristicsSimple implements DatacenterCharacteristics {

    /**
     * @see #getArchitecture()
     */
    private String architecture;

    /**
     * @see #getOs()
     */
    private String os;

    /**
     * @see #getTimeZone()
     */
    private double timeZone;

    /**
     * @see #getCostPerSecond()
     */
    private double costPerSecond;

    /**
     * @see #getVmm()
     */
    private String vmm;

    /**
     * @see #getCostPerMem()
     */
    private double costPerMem;

    /**
     * @see #getCostPerStorage()
     */
    private double costPerStorage;

    /**
     * @see #getCostPerBw()
     */
    private double costPerBw;

    /** @see #getDatacenter() */
    private Datacenter datacenter;

    /**
     * Creates a DatacenterCharacteristics with default values
     * for {@link #getArchitecture() architecture}, {@link #getOs() OS}, {@link #getTimeZone() Time Zone} and
     * {@link #getVmm() VMM}. The costs for {@link #getCostPerBw() BW}, {@link #getCostPerMem()} () RAM}
     * and {@link #getCostPerStorage()} () Storage} are set to zero.
     *
     * @pre machineList != null
     * @post $none
     */
    public DatacenterCharacteristicsSimple(final Datacenter datacenter){
        setArchitecture(DEFAULT_ARCH);
        setOs(DEFAULT_OS);
        setTimeZone(DEFAULT_TIMEZONE);
        setVmm(DEFAULT_VMM);
        setCostPerSecond(0);
        setCostPerMem(0);
        setCostPerStorage(0);
        setCostPerBw(0);
        this.datacenter = datacenter;
    }

    @Override
    public double getMips() {
        return datacenter.getHostList().stream().mapToDouble(Host::getTotalMipsCapacity).sum();
    }

    @Override
    public int getNumberOfPes() {
        return HostList.getNumberOfPes(datacenter.getHostList());
    }

    @Override
    public int getNumberOfFreePes() {
        return HostList.getNumberOfFreePes(datacenter.getHostList());
    }

    @Override
    public boolean setPeStatus(final Pe.Status status, final int hostId, final int peId) {
        return HostList.setPeStatus(datacenter.getHostList(), status, hostId, peId);
    }

    @Override
    public long getNumberOfFailedHosts() {
        return datacenter.getHostList().stream().filter(Host::isFailed).count();
    }

    @Override
    public boolean isWorking() {
        boolean result = false;
        if (getNumberOfFailedHosts() == 0) {
            result = true;
        }

        return result;
    }

    @Override
    public double getCostPerMem() {
        return costPerMem;
    }

    @Override
    public final DatacenterCharacteristics setCostPerMem(final double costPerMem) {
        this.costPerMem = costPerMem;
        return this;
    }

    @Override
    public double getCostPerStorage() {
        return costPerStorage;
    }

    @Override
    public final DatacenterCharacteristics setCostPerStorage(final double costPerStorage) {
        this.costPerStorage = costPerStorage;
        return this;
    }

    @Override
    public double getCostPerBw() {
        return costPerBw;
    }

    @Override
    public final DatacenterCharacteristics setCostPerBw(final double costPerBw) {
        this.costPerBw = costPerBw;
        return this;
    }

    @Override
    public String getVmm() {
        return vmm;
    }

    /**
     * Gets the Datacenter id, setup when Datacenter is created.
     * @return
     */
    @Override
    public int getId() {
        return datacenter.getId();
    }

    @Override
    public String getArchitecture() {
        return architecture;
    }

    @Override
    public final DatacenterCharacteristics setArchitecture(String architecture) {
        this.architecture = architecture;
        return this;
    }

    @Override
    public String getOs() {
        return os;
    }

    @Override
    public final DatacenterCharacteristics setOs(String os) {
        this.os = os;
        return this;
    }

    @Override
    public double getTimeZone() {
        return timeZone;
    }

    @Override
    public final DatacenterCharacteristics setTimeZone(final double timeZone) {
        this.timeZone = timeZone < -12 || timeZone > 13 ? 0 : timeZone;
        return this;
    }

    @Override
    public double getCostPerSecond() {
        return costPerSecond;
    }

    @Override
    public final DatacenterCharacteristics setCostPerSecond(double costPerSecond) {
        this.costPerSecond = costPerSecond;
        return this;
    }

    @Override
    public final DatacenterCharacteristics setVmm(final String vmm) {
        this.vmm = vmm;
        return this;
    }

    @Override
    public Datacenter getDatacenter() {
        return datacenter;
    }

}
