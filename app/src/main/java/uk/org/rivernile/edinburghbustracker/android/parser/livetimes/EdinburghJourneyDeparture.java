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

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import java.util.Date;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyDeparture;

/**
 * This class defines an Edinburgh specific JourneyDeparture.
 * 
 * @author Niall Scott
 */
public class EdinburghJourneyDeparture extends JourneyDeparture {
    
    private final int departureMinutes;
    private final char reliability;
    private final char type;
    private final boolean isDisrupted;
    
    /**
     * Create a new EdinburghJourneyDeparture.
     * 
     * @param stopCode The stop code where the departure takes place from.
     * @param stopName The stop name where the departure takes place from. Can
     * be null.
     * @param departureTime A Date object representing the time of departure.
     * @param departureMinutes The number of minutes until the departure takes
     * place, as calculated by the server.
     * @param reliability The 'reliability' field in the response.
     * @param type The 'type' field in the response.
     * @param isDisrupted true if the bus stop is disrupted, false if not.
     * @param order The value of the 'order' field in the response.
     */
    public EdinburghJourneyDeparture(final String stopCode,
            final String stopName, final Date departureTime,
            final int departureMinutes, final char reliability, final char type,
            final boolean isDisrupted, final int order) {
        super(stopCode, stopName, departureTime, order);
        
        this.departureMinutes = departureMinutes;
        this.reliability = reliability;
        this.type = type;
        this.isDisrupted = isDisrupted;
    }

    /**
     * Get the number of minutes from the time that the request was made with
     * the API server until the bus is due to arrive the bus stop. This differs
     * from {@link JourneyDeparture#getDepartureMinutes()} as in this case the
     * time is calculated by the server rather than on device.
     * 
     * @return The number of minutes until the bus is due to arrive at the stop.
     * This time is relative to when the request was made with the server.
     */
    @Override
    public int getDepartureMinutes() {
        return departureMinutes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBusStopDisrupted() {
        return isDisrupted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEstimatedTime() {
        return reliability == EdinburghConstants.RELIABILITY_ESTIMATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDelayed() {
        return reliability == EdinburghConstants.RELIABILITY_DELAYED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDiverted() {
        return reliability == EdinburghConstants.RELIABILITY_DIVERTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminus() {
        return type == EdinburghConstants.TYPE_TERMINUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPartRoute() {
        return type == EdinburghConstants.TYPE_PART_ROUTE;
    }
}