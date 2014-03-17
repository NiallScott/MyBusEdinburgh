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
 * A JourneyDeparture represents a departure point on a journey. A journey is a
 * list of points that a service calls at. This class should be subclassed to
 * customise it for a specific real-time system.
 * 
 * @author Niall Scott
 */
public abstract class JourneyDeparture implements Comparable<JourneyDeparture> {
    
    private final int order;
    private final String stopCode;
    private final String stopName;
    private final Date departureTime;
    
    /**
     * Create a new JourneyDeparture.
     * 
     * @param stopCode The stop code where the departure takes place from.
     * @param stopName The stop name where the departure takes place from. Can
     * be null.
     * @param departureTime A Date object representing the time of departure.
     * @param order What order this departure is in a list of departures.
     */
    public JourneyDeparture(final String stopCode, final String stopName,
            final Date departureTime, final int order) {
        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("The stopCode must not be null "
                    + "or empty.");
        }
        
        if (departureTime == null) {
            throw new IllegalArgumentException("The departureTime must not be "
                    + "null.");
        }
        
        this.stopCode = stopCode;
        this.stopName = stopName;
        this.departureTime = departureTime;
        this.order = order;
    }

    /**
     * Get the stopCode of the departure bus stop.
     * 
     * @return The stopCode of the departure bus stop.
     */
    public String getStopCode() {
        return stopCode;
    }
    
    /**
     * Get the stopName of the departure bus stop.
     * 
     * @return The stopName of the departure bus stop. May be null.
     */
    public String getStopName() {
        return stopName;
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
     * Get whether the bus stop has a current disruption set or not.
     * 
     * @return true if the bus stop is disrupted, false if not.
     */
    public abstract boolean isBusStopDisrupted();
    
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
    public int compareTo(final JourneyDeparture another) {
        return another != null ? order - another.order : -1;
    }
}