/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.util.Log;

import java.util.*;

/**
 * A Cloud Information Service (CIS) is an entity that provides cloud resource
 * registration, indexing and discovery services. The Cloud hostList tell their
 * readiness to process Cloudlets by registering themselves with this entity.
 * Other entities such as the resource broker can contact this class for
 * resource discovery service, which returns a list of registered resource IDs.
 * <p>
 * In summary, it acts like a yellow page service. This class will be created by
 * CloudSim upon initialisation of the simulation. Hence, do not need to worry
 * about creating an object of this class.
 *
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @since CloudSim Toolkit 1.0
 */
public class CloudInformationService extends CloudSimEntity {

    /**
     * A list containing all Datacenters that are registered at the
     * Cloud Information Service (CIS).
     */
    private final Set<Datacenter> datacenterList;

    /**
     * List of all regional CIS.
     */
    private final Set<CloudInformationService> cisList;

    /**
     * Instantiates a new CloudInformationService object.
     *
     * @param simulation The CloudSim instance that represents the simulation the Entity is related to
     * @pre name != null
     * @post $none
     */
    CloudInformationService(CloudSim simulation) {
        super(simulation);
        datacenterList = new TreeSet<>();
        cisList = new TreeSet<>();
    }

    /**
     * The method has no effect at the current class.
     */
    @Override
    protected void startEntity() {
    }

    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case CloudSimTags.REGISTER_REGIONAL_CIS:
                cisList.add((CloudInformationService) ev.getData());
            break;

            case CloudSimTags.REQUEST_REGIONAL_CIS:
                super.send(ev.getSource(), 0, ev.getTag(), cisList);
            break;

            case CloudSimTags.DATACENTER_REGISTRATION_REQUEST:
                datacenterList.add((Datacenter) ev.getData());
            break;

            // A Broker is requesting a list of all datacenters.
            case CloudSimTags.DATACENTER_LIST_REQUEST:
                super.send(ev.getSource(), 0, ev.getTag(), datacenterList);
            break;

            default:
                processOtherEvent(ev);
            break;
        }
    }

    @Override
    public void shutdownEntity() {
        notifyAllEntity();
    }

    /**
     * Gets the list of all registered Datacenters.
     *
     * @return
     * @pre $none
     * @post $none
     */
    public Set<Datacenter> getDatacenterList() {
        return datacenterList;
    }

    /**
     * Process non-default received events that aren't processed by the
     * {@link #processEvent(SimEvent)} method. This
     * method should be overridden by subclasses in other to process new defined
     * events.
     *
     * @param ev a CloudSimEvent object
     * @pre ev != null
     * @post $none
     */
    protected void processOtherEvent(final SimEvent ev) {
        if (ev == null) {
            Log.printConcatLine("CloudInformationService.processOtherEvent(): ",
                "Unable to handle a request since the event is null.");
            return;
        }

        Log.printLine("CloudInformationSevice.processOtherEvent(): " + "Unable to handle a request from "
            + ev.getSource().getName() + " with event tag = " + ev.getTag());
    }

    /**
     * Tells all registered entities about the end of simulation.
     *
     * @pre $none
     * @post $none
     */
    private void notifyAllEntity() {
        Log.printConcatLine(super.getName(), ": Notify all CloudSim Plus entities to shutdown.");

        signalShutdown(datacenterList);
        signalShutdown(cisList);

        // reset the values
        datacenterList.clear();
        cisList.clear();
    }

    /**
     * Sends a {@link CloudSimTags#END_OF_SIMULATION} signal to all entity IDs
     * mentioned in the given list.
     *
     * @param list List of entities to notify about simulation end
     * @pre list != null
     * @post $none
     */
    protected void signalShutdown(final Collection<? extends SimEntity> list) {
        // checks whether a list is empty or not
        if (list == null) {
            return;
        }

        // Send END_OF_SIMULATION event to all entities in the list
        list.forEach(entity -> super.send(entity, 0L, CloudSimTags.END_OF_SIMULATION));
    }

}
