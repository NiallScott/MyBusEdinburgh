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

package uk.org.rivernile.android.bustracker.parser.livetimes;

/**
 * This class represents a single instance of a bus service. This class may be
 * extended to suit the needs of a particular town or city. The attributes
 * in this class are considered the basic properties of a bus service.
 * 
 * @author Niall Scott
 */
public class Bus {
    private final String destination;
    private final String arrivalTime;
    
    /**
     * Create a single instance of a bus and its corresponding destination and
     * arrival time. This class may be extended to suit the needs of a particlar
     * town or city.
     * 
     * @param destination The final destination of this bus service.
     * @param arrivalTime The arrival time of this bus service. This is left
     * as a String so that you may format the time in any way you wish.
     */
    public Bus(final String destination, final String arrivalTime) {
        if(arrivalTime == null || arrivalTime.length() == 0)
            throw new IllegalArgumentException("The arrival time must not be " +
                    "null or blank.");

        this.destination = destination;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Get the arrival time of the bus service.
     * 
     * @return The arrival time of the bus service.
     */
    public String getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Get the destination of the bus service. Can be null.
     * 
     * @return The destination of the bus service. Can be null.
     */
    public String getDestination() {
        return destination;
    }
}