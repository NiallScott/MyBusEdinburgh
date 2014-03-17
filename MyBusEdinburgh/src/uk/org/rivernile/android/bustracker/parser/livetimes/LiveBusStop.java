/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

import android.text.TextUtils;
import java.util.List;

/**
 * A LiveBusStop represents a single bus stop returned from the real-time
 * system. It holds a List of {@link LiveBusService}s which in turn hold a List
 * of {@link LiveBus}s which hold the departure times.
 * 
 * @author Niall Scott
 * @param <T> The implementation-specific type of {@link LiveBusService} that
 * this class will hold.
 */
public abstract class LiveBusStop<T extends LiveBusService> {
    
    private final String stopCode;
    private final String stopName;
    private final List<T> services;
    
    /**
     * Create a new LiveBusStop.
     * 
     * @param stopCode The unique code of this bus stop. Must not be null or
     * empty.
     * @param stopName The name of this bus stop.
     * @param services A List of {@link LiveBusService}s that have live
     * departures at this bus stop.
     */
    public LiveBusStop(final String stopCode, final String stopName,
            final List<T> services) {
        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("The stopCode must not be null "
                    + "or empty.");
        }
        
        if (services == null) {
            throw new IllegalArgumentException("The List of services must not "
                    + "be null.");
        }
        
        this.stopCode = stopCode;
        this.stopName = stopName;
        this.services = services;
    }
    
    /**
     * Get the unique code of this bus stop.
     * 
     * @return The unique code of this bus stop.
     */
    public String getStopCode() {
        return stopCode;
    }
    
    /**
     * Get the name of this bus stop.
     * 
     * @return The name of this bus stop. This can be empty or null.
     */
    public String getStopName() {
        return stopName;
    }
    
    /**
     * Get the List of services that have departures at this bus stop.
     * 
     * @return The List of services that have departures at this bus stop.
     */
    public List<T> getServices() {
        return services;
    }
    
    /**
     * Is the bus stop disrupted?
     * 
     * Not all real-time systems will support this flag. In this case,
     * subclasses should return false here in all instances.
     * 
     * @return true if there is a disruption in place for this bus stop, false
     * if not.
     */
    public abstract boolean isDisrupted();
}