/*
 * Copyright (C) 2009 - 2010 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import java.util.Date;

/**
 * This class is a convenience class to help sort the bus times in order of
 * time. The Date class has a comparitor and this class just keeps everything
 * bundled together.
 *
 * @author Niall Scott
 */
public class BusServiceTimeMapping implements
        Comparable<BusServiceTimeMapping>
{
    private String serviceName;
    private Date arrivalTime;

    /**
     * Create a new service name to arrival time mapping.
     *
     * @param serviceName The name of the bus service.
     * @param arrivalTime The Date object which contains the arrival time of the
     * bus service.
     */
    public BusServiceTimeMapping(final String serviceName,
            final Date arrivalTime) {
        this.serviceName = serviceName;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Get the bus service name.
     *
     * @return The bus service name.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the arrival time Date object.
     *
     * @return The arrival time Date object.
     */
    public Date getArrivalTime() {
        return arrivalTime;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final BusServiceTimeMapping a) {
        return arrivalTime.compareTo(a.getArrivalTime());
    }
}