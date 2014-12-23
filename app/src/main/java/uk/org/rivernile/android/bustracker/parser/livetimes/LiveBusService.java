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
import com.davekoelle.alphanum.AlphanumComparator;
import java.util.List;

/**
 * A LiveBusService represents a single bus service that stops at a single bus
 * stop. It holds a collection of LiveBuses (or rather, live bus departures).
 * 
 * @author Niall Scott
 * @param <T> The implementation-specific type of {@link LiveBus} that this
 * class will hold.
 */
public abstract class LiveBusService<T extends LiveBus>
        implements Comparable<LiveBusService> {
    
    private static final AlphanumComparator comparator =
            new AlphanumComparator();
    private final String serviceName;
    private final List<T> buses;
    
    /**
     * Create a new LiveBusService.
     * 
     * @param serviceName The public visible name of the service. Cannot be null
     * or empty.
     * @param buses A List of {@link LiveBus}es attributed to this bus service.
     * Cannot be null.
     */
    public LiveBusService(final String serviceName, final List<T> buses) {
        if (TextUtils.isEmpty(serviceName)) {
            throw new IllegalArgumentException("The serviceName must not be "
                    + "null or empty.");
        }
        
        if (buses == null) {
            throw new IllegalArgumentException("The List of buses must not be "
                    + "null.");
        }
        
        this.serviceName = serviceName;
        this.buses = buses;
    }
    
    /**
     * Get the name of this service.
     * 
     * @return The name of this service. Will be non-null.
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Get the List of bus departures attributed to this service.
     * 
     * @return The List of bus departures attributed to this service. Will be
     * non-null.
     */
    public List<T> getLiveBuses() {
        return buses;
    }
    
    /**
     * Get the name of the operator performing the journey.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return The name of the operator performing the journey. Could be null
     * or an empty String.
     */
    public abstract String getOperator();
    
    /**
     * Get a textual description of the route.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return A textual description of the route. Could be null or an empty
     * String.
     */
    public abstract String getRoute();
    
    /**
     * Is the service disrupted?
     * 
     * Not all real-time systems will support this flag. In this case,
     * subclasses should return false here in all instances.
     * 
     * @return true if there is a disruption in place for this service, false if
     * not.
     */
    public abstract boolean isDisrupted();
    
    /**
     * Is the service diverted?
     * 
     * Not all real-time systems will support this flag. In this case,
     * subclasses should return false here in all instances.
     * 
     * @return true if this service is diverted, false if not.
     */
    public abstract boolean isDiverted();

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final LiveBusService another) {
        return another != null ?
                comparator.compare(this.serviceName, another.serviceName) : -1;
    }
}