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

import java.util.Map;
import java.util.Set;

/**
 * LiveBusTimes maps unique bus stop codes to {@link LiveBusStop} instances. It
 * also returns data that is global to the bus times request.
 * 
 * @author Niall Scott
 * @param <T> The implementation-specific type of {@link LiveBusStop} that this
 * class will map a bus stop code to.
 */
public abstract class LiveBusTimes<T extends LiveBusStop> {
    
    private final Map<String, T> busStops;
    private final long receiveTime;
    
    /**
     * Create a new LiveBusTimes instance.
     * 
     * @param busStops The mapping of bus stop codes to {@link LiveBusStop}
     * instances. Must not be null.
     * @param receiveTime The time, as per
     * {@link android.os.SystemClock#elapsedRealtime()}, that the data was
     * received at.
     */
    public LiveBusTimes(final Map<String, T> busStops, final long receiveTime) {
        if (busStops == null) {
            throw new IllegalArgumentException("The busStops must not be "
                    + "null.");
        }
        
        this.busStops = busStops;
        this.receiveTime = receiveTime;
    }
    
    /**
     * Does this instance contain a mapping from the specified stopCode?
     * 
     * @param stopCode The bus stop code to test a mapping for.
     * @return true if this instance is aware of the given stopCode, false if
     * not.
     */
    public boolean containsBusStop(final String stopCode) {
        return busStops.containsKey(stopCode);
    }
    
    /**
     * Get the {@link LiveBusStop} that is mapped to by stopCode.
     * 
     * @param stopCode The stopCode of the {@link LiveBusStop} to fetch.
     * @return An instance of {@link LiveBusStop} which matches the mapping
     * given by stopCode, or null if the mapping does not exist.
     */
    public T getBusStop(final String stopCode) {
        return busStops.get(stopCode);
    }
    
    /**
     * Get a Set of all the bus stop codes that this instance is aware of.
     * 
     * @return A Set of bus stop codes that this instance is aware of.
     */
    public Set<String> getBusStops() {
        return busStops.keySet();
    }
    
    /**
     * Get the time, as per {@link android.os.SystemClock#elapsedRealtime()},
     * that the data was received at. This may be used later to determine how
     * old the data is. The reason that it is based on
     * {@link android.os.SystemClock#elapsedRealtime()} is because that does not
     * change if the user changes the system clock.
     * 
     * @return The number of milliseconds since system boot that the data was
     * received and parsed at.
     */
    public long getReceiveTime() {
        return receiveTime;
    }
    
    /**
     * Test whether this instance if aware of any bus stops at all.
     * 
     * @return true if this instance contains no bus stops, false if there is at
     * least 1 bus stop.
     */
    public boolean isEmpty() {
        return busStops.isEmpty();
    }
    
    /**
     * Get the number of bus stops that this instance is aware of.
     * 
     * @return The number of bus stops that this instance is aware of.
     */
    public int size() {
        return busStops.size();
    }
    
    /**
     * Is there a disruption that affects the whole network?
     * 
     * Not all real-time systems will support this flag. In this case,
     * subclasses should return false here in all instances.
     * 
     * @return true if there is a disruption that affects the whole network,
     * false if not.
     */
    public abstract boolean isGlobalDisruption();
}