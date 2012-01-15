/*
 * Copyright (C) 2011 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 */

package uk.org.rivernile.android.bustracker.parser.livetimes;

import java.util.ArrayList;

/**
 * A bus service is a collection of buses. This class is used to represent all
 * buses within a bus service at a particular bus stop. Use the addBus() method
 * to add new buses, either using the generic type Bus, or by using a class
 * which subclasses Bus which suits a particular implementation. This class
 * itself may be extended to suit a particular implementation.
 * 
 * @author Niall Scott
 */
public class BusService {
    
    private String serviceName;
    private String route;
    private ArrayList<Bus> buses;
    
    /**
     * Create a new instance of BusService. This is an immutable class.
     * 
     * @param serviceName The name of the bus service.
     * @param route A String which describes the route this service takes.
     */
    public BusService(final String serviceName, final String route) {
        if(serviceName == null || serviceName.length() == 0)
            throw new IllegalArgumentException("The service name must not be " +
                    "null or blank");
        
        if(route == null || route.length() == 0)
            throw new IllegalArgumentException("The route must not be null " +
                    "or blank.");
        
        this.serviceName = serviceName;
        this.route = route;
        
        buses = new ArrayList<Bus>();
    }
    
    /**
     * Get the name of this bus service.
     * 
     * @return The name of this bus service.
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Get the String which describes the route that this bus service takes.
     * 
     * @return  The String which describes the route that this bus service
     * takes.
     */
    public String getRoute() {
        return route;
    }
    
    /**
     * Add a new bus to this bus service.
     * 
     * @param bus The bus to add to this service.
     */
    public void addBus(final Bus bus) {
        if(bus == null)
            throw new IllegalArgumentException("Bus must not be null.");
        
        buses.add(bus);
    }
    
    /**
     * Get the ArrayList which contains the list of buses.
     * 
     * @return The ArrayList which contains the list of buses.
     */
    public ArrayList<Bus> getBuses() {
        return (ArrayList<Bus>)buses.clone();
    }
    
    /**
     * Get the first bus in the array.
     * 
     * @return The first bus in the array.
     */
    public Bus getFirstBus() {    
        return buses.isEmpty() ? null : buses.get(0);
    }
    
    /**
     * Return the name of the bus service.
     * 
     * @return The name of the bus service.
     */
    @Override
    public String toString() {
        return serviceName;
    }
}