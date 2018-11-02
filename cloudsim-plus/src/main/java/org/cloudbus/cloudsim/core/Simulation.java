/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.core;

import java.util.*;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.events.SimEvent;

import java.util.function.Predicate;

import org.cloudbus.cloudsim.core.predicates.PredicateAny;
import org.cloudbus.cloudsim.core.predicates.PredicateNone;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.network.topologies.NetworkTopology;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.EventListener;

/**
 * An interface to be implemented by a class that manages simulation
 * execution, controlling all the simulation life cycle.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Manoel Campos da Silva Filho
 *
 * @see CloudSim
 * @since CloudSim Plus 1.0
 */
public interface Simulation {
    /**
     * A standard predicate that matches any event.
     */
    PredicateAny SIM_ANY = new PredicateAny();

    /**
     * A standard predicate that does not match any events.
     */
    PredicateNone SIM_NONE = new PredicateNone();

    /**
     * An attribute that implements the Null Object Design Pattern for {@link Simulation}
     * objects.
     */
    Simulation NULL = new SimulationNull();

    /**
     * Aborts the simulation without finishing the processing
     * of entities in the {@link #getEntityList() entities list}, <b>what may give
     * unexpected results</b>.
     * <p><b>Use this method just if you want to abandon the simulation an usually ignore the results.</b></p>
     */
    void abort();

    /**
     * Adds a new entity to the simulation. Each {@link CloudSimEntity} object
     * register itself when it is instantiated.
     *
     * @param e The new entity
     */
    void addEntity(CloudSimEntity e);

    /**
     * Cancels the first event from the future event queue that matches a given predicate
     * and was sent by a given entity, then removes it from the queue.
     *
     * @param src Id of entity that scheduled the event
     * @param p   the event selection predicate
     * @return the removed event or {@link SimEvent#NULL} if not found
     */
    SimEvent cancel(SimEntity src, Predicate<SimEvent> p);

    /**
     * Cancels all events from the future event queue that matches a given predicate
     * and were sent by a given entity, then removes those ones from the queue.
     *
     * @param src Id of entity that scheduled the event
     * @param p   the event selection predicate
     * @return true if at least one event has been cancelled; false otherwise
     */
    boolean cancelAll(SimEntity src, Predicate<SimEvent> p);

    /**
     * Gets the current simulation time in seconds.
     *
     * @return
     * @see #isRunning()
     */
    double clock();

    /**
     * Gets the current simulation time in minutes.
     *
     * @return
     * @see #isRunning()
     */
    double clockInMinutes();

    /**
     * Gets the current simulation time in hours.
     *
     * @return
     * @see #isRunning()
     */
    double clockInHours();

    /**
     * Find first deferred event matching a predicate.
     *
     * @param dest Id of entity that the event has to be sent to
     * @param p    the event selection predicate
     * @return the first matched event or {@link SimEvent#NULL} if not found
     */
    SimEvent findFirstDeferred(SimEntity dest, Predicate<SimEvent> p);

    /**
     * Gets a new copy of initial simulation Calendar.
     *
     * @return a new copy of Calendar object
     * @pre $none
     * @post $none
     */
    Calendar getCalendar();

    /**
     * Gets the {@link CloudInformationService}.
     *
     * @return the Entity
     */
    CloudInformationService getCloudInfoService();

    /**
     * Sends a request to Cloud Information Service (CIS) entity to get the list
     * of all Cloud Datacenter IDs.
     *
     * @return a List containing Datacenter IDs
     * @pre $none
     * @post $none
     */
    Set<Datacenter> getDatacenterList();

    /**
     * Returns a read-only list of entities created for the simulation.
     *
     * @return
     */
    List<SimEntity> getEntityList();

    /**
     * Returns the minimum time between events (in seconds).
     * Events within shorter periods after the last event are discarded.
     *
     * @return the minimum time between events (in seconds).
     */
    double getMinTimeBetweenEvents();

    /**
     * Get the current number of entities in the simulation.
     *
     * @return The number of entities
     */
    int getNumEntities();

    /**
     * Removes a listener from the onEventProcessingListener List.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    boolean removeOnEventProcessingListener(EventListener<SimEvent> listener);

    /**
     * Adds an {@link EventListener} object that will be notified when the simulation is paused.
     * When this Listener is notified, it will receive an {@link EventInfo} informing
     * the time the pause occurred.
     *
     * <p>This object is just information about the event
     * that happened. In fact, it isn't generated an actual {@limk SimEvent} for a pause event
     * because there is not need for that.</p>
     *
     * @param listener the event listener to add
     * @return
     */
    Simulation addOnSimulationPausedListener(EventListener<EventInfo> listener);

    /**
     * Removes a listener from the onSimulationPausedListener List.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
     boolean removeOnSimulationPausedListener(EventListener<EventInfo> listener);

    /**
     * Adds a {@link EventListener} object that will be notified when any event
     * is processed by CloudSim. When this Listener is notified, it will receive
     * the {@link SimEvent} that was processed.
     *
     * @param listener the event listener to add
     * @return
     */
    Simulation addOnEventProcessingListener(EventListener<SimEvent> listener);

    /**
     * Adds a {@link EventListener} object that will be notified every time when the
     * simulation clock advances. Notifications are sent in a second interval to avoid notification flood.
     * Thus, if the clock changes, for instance, from 1.0, to 1.1, 2.0, 2.1, 2.2, 2.5 and then 3.2,
     * notifications will just be sent for the times 1, 2 and 3 that represent the integer
     * part of the simulation time.
     *
     * @param listener the event listener to add
     * @return
     */
    Simulation addOnClockTickListener(EventListener<EventInfo> listener);

    /**
     * Removes a listener from the onClockTickListener List.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    boolean removeOnClockTickListener(EventListener<? extends EventInfo> listener);

    /**
     * Pauses an entity for some time.
     *  @param src   id of entity to be paused
     * @param delay the time period for which the entity will be inactive
     */
    void pauseEntity(SimEntity src, double delay);

    /**
     * Holds an entity for some time.
     *  @param src   id of entity to be held
     * @param delay How many seconds after the current time the entity has to be held
     */
    void holdEntity(SimEntity src, long delay);

    /**
     * Checks if the simulation is paused.
     *
     * @return
     */
    boolean isPaused();

    /**
     * Requests the simulation to be paused as soon as possible.
     *
     * @return true if the simulation was paused, false if it was already paused or has finished
     */
    boolean pause();

    /**
     * Requests the simulation to be paused at a given time.
     * The method schedules the pause request and then returns immediately.
     *
     * @param time the time at which the simulation has to be paused
     * @return true if pause request was successfully received (the given time
     * is greater than or equal to the current simulation time), false otherwise.
     */
    boolean pause(double time);

    /**
     * This method is called if one wants to resume the simulation that has
     * previously been paused.
     *
     * @return true if the simulation has been restarted or false if it wasn't paused.
     */
    boolean resume();

    /**
     * Check if the simulation is still running.
     * Even if the simulation {@link #isPaused() is paused},
     * the method returns true to indicate that the simulation is
     * in fact active yet.
     * <p>
     * This method should be used by
     * entities to check if they should continue executing.
     *
     * @return
     */
    boolean isRunning();

    /**
     * Selects the first deferred event that matches a given predicate
     * and removes it from the queue.
     *
     * @param dest entity that the event has to be sent to
     * @param p    the event selection predicate
     * @return the removed event or {@link SimEvent#NULL} if not found
     */
    SimEvent select(SimEntity dest, Predicate<SimEvent> p);

    /**
     * Sends an event from one entity to another.
     *  @param src  entity that scheduled the event
     * @param dest  entity that the event will be sent to
     * @param delay How many seconds after the current simulation time the event should be sent
     * @param tag   the {@link SimEvent#getTag() tag} that classifies the event
     * @param data  the {@link SimEvent#getData() data} to be sent inside the event
     */
    void send(SimEntity src, SimEntity dest, double delay, int tag, Object data);

    /**
     * Sends an event from one entity to another, adding it to the beginning of the queue in order to give priority to it.
     *  @param src  entity that scheduled the event
     * @param dest  entity that the event will be sent to
     * @param delay How many seconds after the current simulation time the event should be sent
     * @param tag   the {@link SimEvent#getTag() tag} that classifies the event
     * @param data  the {@link SimEvent#getData() data} to be sent inside the event
     */
    void sendFirst(SimEntity src, SimEntity dest, double delay, int tag, Object data);

    /**
     * Sends an event from one entity to another without delaying
     * the message.
     *  @param src  entity that scheduled the event
     * @param dest entity that the event will be sent to
     * @param tag  the {@link SimEvent#getTag() tag} that classifies the event
     * @param data the {@link SimEvent#getData() data} to be sent inside the event
     */
    void sendNow(SimEntity src, SimEntity dest, int tag, Object data);

    /**
     * Starts the execution of CloudSim simulation and <b>waits for complete
     * execution of all entities</b>, i.e. until all entities threads reach
     * non-RUNNABLE state or there are no more events in the future event queue.
     * <p>
     * <b>Note</b>: This method should be called just after all the entities
     * have been setup and added.
     * </p>
     *
     * @return the last clock time
     * @throws RuntimeException When the simulation already run once.
     * If you paused the simulation and wants to resume it,
     * you must use {@link #resume()} instead of calling the current method.
     * @pre $none
     * @post $none
     */
    double start();

    /**
     * Forces the termination of the simulation before it ends.
     *
     * @return true if the simulation was running and the termination request was accepted,
     * false if the simulation was not started yet
     */
    boolean terminate();

    /**
     * Schedules the termination of the simulation for a given time before it has completely finished.
     *
     * @param time the time at which the simulation has to be terminated
     * @return true if the time given is greater than the current simulation time, false otherwise
     */
    boolean terminateAt(double time);

    /**
     * Sets the state of an entity to {@link SimEntity.State#WAITING},
     * making it to wait for events that satisfy a given predicate.
     * Only such events will be passed to the entity.
     * This is done to avoid unnecessary context Datacenter.
     *
     * @param src entity that scheduled the event
     * @param p   the event selection predicate
     */
    void wait(CloudSimEntity src, Predicate<SimEvent> p);

    /**
     * Gets the number of events in the deferred event queue that are targeted to a given entity and
     * match a given predicate.
     *
     * @param dest Id of entity that the event has to be sent to
     * @param p    the event selection predicate
     * @return
     */
    long waiting(SimEntity dest, Predicate<SimEvent> p);

    /**
     * Gets the network topology used for Network simulations.
     *
     * @return
     */
    NetworkTopology getNetworkTopology();

    /**
     * Sets the network topology used for Network simulations.
     *
     * @param networkTopology the network topology to set
     */
    void setNetworkTopology(NetworkTopology networkTopology);

    /**
     * Defines IDs for a list of {@link ChangeableId} entities that don't
     * have one already assigned. Such entities can be a {@link Cloudlet},
     * {@link Vm} or any object that implements {@link ChangeableId}.
     *
     * @param <T> the type of entities to define an ID
     * @param list list of objects to define an ID
     * @return true if the List has any Entity, false if it's empty
     */
    static <T extends ChangeableId> boolean setIdForEntitiesWithoutOne(List<? extends T> list){
        return setIdForEntitiesWithoutOne(list, null);
    }

    /**
     * Defines IDs for a list of {@link ChangeableId} entities that don't
     * have one already assigned. Such entities can be a {@link Cloudlet},
     * {@link Vm} or any object that implements {@link ChangeableId}.
     *
     * @param <T> the type of entities to define an ID
     * @param list list of objects to define an ID
     * @param lastEntity the last created Entity which its ID will be used
     *        as the base for the next IDs
     * @return true if the List has any Entity, false if it's empty
     */
    static <T extends ChangeableId> boolean setIdForEntitiesWithoutOne(final List<? extends T> list, final T lastEntity){
        Objects.requireNonNull(list);
        if(list.isEmpty()){
            return false;
        }

        int id = lastEntity == null ? list.get(list.size()-1).getId() : lastEntity.getId();
        //if the ID is a negative number lower than -1, it's set as -1 to start the first ID as 0
        id = Math.max(id, -1);
        for (final ChangeableId e : list) {
            if(e.getId() < 0) {
                e.setId(++id);
            }
        }

        return true;
    }

    /**
     * Gets the number of events in the future queue
     * which match a given predicate.
     * @param predicate the predicate to filter the list of future events.
     * @return the number of future events which match the predicate
     */
    long getNumberOfFutureEvents(Predicate<SimEvent> predicate);
}
