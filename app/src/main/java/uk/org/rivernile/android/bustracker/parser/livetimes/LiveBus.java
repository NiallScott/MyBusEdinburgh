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
import java.util.Date;

/**
 * A LiveBus is a bus that is part of a service which has real-time tracking
 * information associated in it, described by this class.
 * 
 * @author Niall Scott
 */
public abstract class LiveBus implements Comparable<LiveBus> {
    
    private final String destination;
    private final Date departureTime;
    
    /**
     * Create a new LiveBus.
     * 
     * @param destination The destination of the bus. Must not be null or empty
     * String.
     * @param departureTime A Date object representing the departure time. Must
     * not be null.
     */
    public LiveBus(final String destination, final Date departureTime) {
        if (TextUtils.isEmpty(destination)) {
            throw new IllegalArgumentException("The destination must not be "
                    + "null or empty.");
        }
        
        if (departureTime == null) {
            throw new IllegalArgumentException("The departureTime must not be "
                    + "null or empty.");
        }
        
        this.destination = destination;
        this.departureTime = departureTime;
    }

    /**
     * Get the name of the destination of this bus.
     * 
     * @return The name of the destination of this bus.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Get a Date object representing the departure time.
     * 
     * @return A Date object representing the departure time.
     */
    public Date getDepartureTime() {
        return departureTime;
    }
    
    /**
     * Get the number of minutes to departure relative to the time that this
     * method is called. A negative number will be returned if the departure
     * time has been passed.
     * 
     * @return The number of minutes to departure. A negative number will be
     * returned if the departure time has been passed.
     */
    public int getDepartureMinutes() {
        final long differenceMillis = departureTime.getTime() -
                new Date().getTime();
        return (int) (differenceMillis / 60000);
    }
    
    /**
     * Get the stop code of the terminating bus stop.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return The stop code of the terminating bus stop. Can be null or an
     * empty String.
     */
    public abstract String getTerminus();
    
    /**
     * Get the unique ID of the journey of this bus.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return The unique ID of the journey of this bus. Can be null or an
     * empty String.
     */
    public abstract String getJourneyId();
    
    /**
     * Get whether the time is estimated or not.
     * 
     * @return true if the time is estimated, false if it is real-time.
     */
    public abstract boolean isEstimatedTime();
    
    /**
     * Get whether the service is delayed or not.
     * 
     * @return true if the service is delayed, false if not.
     */
    public abstract boolean isDelayed();
    
    /**
     * Get whether the service is diverted or not.
     * 
     * @return true if the service is diverted, false if not.
     */
    public abstract boolean isDiverted();
    
    /**
     * Get whether this point is a terminus stop on the journey's route.
     * 
     * @return true if this point is a terminus for this journey, false if not.
     */
    public abstract boolean isTerminus();
    
    /**
     * Get whether this journey is only going to complete part of its route or
     * not.
     * 
     * @return true if this journey will only complete part of its route, false
     * if not.
     */
    public abstract boolean isPartRoute();

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final LiveBus another) {
        return another != null ?
                departureTime.compareTo(another.departureTime) : -1;
    }
}