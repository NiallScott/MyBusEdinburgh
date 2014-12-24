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

import android.text.TextUtils;
import java.util.List;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;

/**
 * This class defines an Edinburgh-specific {@link Journey}.
 * 
 * @author Niall Scott
 */
public class EdinburghJourney extends Journey<EdinburghJourneyDeparture> {
    
    private final String operator;
    private final String route;
    private final String destination;
    private final String terminus;
    private final boolean globalDisruption;
    private final boolean serviceDisruption;
    private final boolean serviceDiversion;
    
    /**
     * Create a new Journey.
     * 
     * @param journeyId The unique ID of the journey. Cannot be null or empty.
     * @param serviceName The public visible name of the journey. Cannot be null
     * or empty.
     * @param departures A List of {@link JourneyDeparture}s for this journey.
     * This cannot be null.
     * @param operator The name of the operator that operates the route that
     * completes this journey, for example 'LB'.
     * @param route A textual description of the route of the service.
     * @param destination The name of the destination of this journey.
     * @param terminus The stopCode of the terminating bus stop.
     * @param globalDisruption true if there's a global disruption, false if
     * not.
     * @param serviceDisruption true if the service is disrupted, false if not.
     * @param serviceDiversion true if the service is diverted, false if not.
     * @param receiveTime The time, as per
     * {@link android.os.SystemClock#elapsedRealtime()}, that the data was
     * received at.
     */
    public EdinburghJourney(final String journeyId, final String serviceName,
            final List<EdinburghJourneyDeparture> departures,
            final String operator, final String route, final String destination,
            final String terminus, final boolean globalDisruption,
            final boolean serviceDisruption, final boolean serviceDiversion,
            final long receiveTime) {
        super(journeyId, serviceName, departures, receiveTime);
        
        if (TextUtils.isEmpty(terminus)) {
            throw new IllegalArgumentException("The terminus must not be null "
                    + "or empty.");
        }
        
        this.operator = operator;
        this.route = route;
        this.destination = destination;
        this.terminus = terminus;
        this.globalDisruption = globalDisruption;
        this.serviceDisruption = serviceDisruption;
        this.serviceDiversion = serviceDiversion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOperator() {
        return operator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoute() {
        return route;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestination() {
        return destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTerminus() {
        return terminus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasGlobalDisruption() {
        return globalDisruption;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasServiceDisruption() {
        return serviceDisruption;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasServiceDiversion() {
        return serviceDiversion;
    }
}