/*
 * Copyright (C) 2011 - 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.livetimes.parser;

import uk.org.rivernile.android.bustracker.parser.livetimes.Bus;

/**
 * This class contains the Edinburgh specific implementation of the bus tracker
 * API.
 * 
 * @author Niall Scott
 */
public class EdinburghBus extends Bus {
    
    /** This field is used when the reliability is unknown. */
    public static final char RELIABILITY_UNKNOWN = 0;
    /** Denotes a bus which is delayed. */
    public static final char RELIABILITY_DELAYED = 'B';
    /** Denotes when a bus has been delocated. */
    public static final char RELIABILITY_DELOCATED = 'D';
    /** Denotes a bus which is real-time but not low floor */
    public static final char RELIABILITY_REAL_TIME_NOT_LOW_FLOOR = 'F';
    /** Denotes a bus which is real-time and is low floor */
    public static final char RELIABILITY_REAL_TIME_LOW_FLOOR = 'H';
    /** Denotes a bus which is immobilised? Broken down perhaps? */
    public static final char RELIABILITY_IMMOBILISED = 'I';
    /** Denotes a bus which is neutralised? The army got to it? */
    public static final char RELIABILITY_NEUTRALISED = 'N';
    /** Denotes a bus which has a radio fault. */
    public static final char RELIABILITY_RADIO_FAULT = 'R';
    /** Denotes a bus for which real-time tracking is not available. */
    public static final char RELIABILITY_ESTIMATED = 'T';
    /** Denotes a bus which has been diverted. */
    public static final char RELIABILITY_DIVERTED = 'V';
    
    /** This field is used when the type is unknown. */
    public static final char TYPE_UNKNOWN = 0;
    /** Denotes this stop is a terminus on this bus route. */
    public static final char TYPE_TERMINUS = 'D';
    /** Denotes this stop is a normal stop on this bus route. */
    public static final char TYPE_NORMAL = 'N';
    /** Denotes this service is part route. */
    public static final char TYPE_PART_ROUTE = 'P';
    /** Denotes this stop is a timing reference stop on this bus route. */
    public static final char TYPE_REFERENCE = 'R';
    
    private final int arrivalMinutes;
    private final char reliability;
    private final char type;
    private final String terminus;
    
    /**
     * Create a new EdinburghBus object.
     * 
     * @param destination The destination of this bus.
     * @param arrivalDay How many days in to the future this bus will arrive at
     * this stop. 0 = today.
     * @param arrivalTime The time in HH:mm format that the bus will arrive at
     * this bus stop.
     * @param arrivalMinutes How many minutes from when the request was made
     * until the bus arrives at the bus stop.
     * @param reliability The reliability of this bus. See the RELIABILITY_*
     * fields in this class.
     * @param type The type of stop this bus stop is for this bus. See the
     * TYPE_* fields in this class.
     * @param terminus This will be used in the future.
     */
    public EdinburghBus(final String destination, final int arrivalDay,
            final String arrivalTime, final int arrivalMinutes,
            final char reliability, final char type, final String terminus) {
        super(destination, arrivalTime);
        
        this.arrivalMinutes = arrivalMinutes;
        this.reliability = reliability;
        this.type = type;
        this.terminus = terminus;
    }
    
    /**
     * Get how many minutes it is between when the request for this information
     * was made and the bus is due to arrive at the bus stop.
     * 
     * @return Minutes until arrival.
     */
    public int getArrivalMinutes() {
        return arrivalMinutes;
    }
    
    /**
     * Get the reliability of this bus. See the RELIABILITY_* fields in this
     * class.
     * 
     * @return The reliability of this bus.
     */
    public char getReliability() {
        return reliability;
    }
    
    /**
     * Get the type of this bus stop for this bus. See the TYPE_* fields in this
     * class.
     * 
     * @return The type of this bus stop for this bus.
     */
    public char getType() {
        return type;
    }
    
    /**
     * To be used in the future.
     * 
     * @return Don't know as of yet.
     */
    public String getTerminus() {
        return terminus;
    }
    
    /**
     * Returns true if this service is delayed, false if not.
     * 
     * @return True if this service is delayed, false if not.
     */
    public boolean isDelayed() {
        return (reliability == RELIABILITY_DELAYED);
    }
    
    /**
     * Returns true if the time is an estimated time (not based on live
     * tracking), false if not.
     * 
     * @return Returns true if the time is an estimated time, otherwise false.
     */
    public boolean isEstimated() {
        return (reliability == RELIABILITY_ESTIMATED);
    }
    
    /**
     * Returns true if this service is diverted, false if not.
     * 
     * @return True if this service is diverted, false if not.
     */
    public boolean isDiverted() {
        return (reliability == RELIABILITY_DIVERTED);
    }
}