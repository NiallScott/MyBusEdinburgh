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

import java.util.List;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;

/**
 * This class defines an Edinburgh-specific {@link LiveBusService}.
 * 
 * @author Niall Scott
 */
public class EdinburghLiveBusService extends LiveBusService<EdinburghLiveBus> {
    
    private final String operator;
    private final String route;
    private final boolean disrupted;
    private final boolean diverted;
    
    /**
     * Create a new EdinburghLiveBusService.
     * 
     * @param serviceName The public visible name of the service. Cannot be null
     * or empty.
     * @param buses A List of {@link EdinburghLiveBus}es attributed to this bus
     * service. Cannot be null.
     * @param operator The name of the operator of this service, for example
     * 'LB'.
     * @param route A textual description of the route of the service.
     * @param disrupted Whether this service is disrupted or not.
     * @param diverted Whether this service is diverted or not.
     */
    public EdinburghLiveBusService(final String serviceName,
            final List<EdinburghLiveBus> buses, final String operator,
            final String route, final boolean disrupted,
            final boolean diverted) {
        super(serviceName, buses);
        
        this.operator = operator;
        this.route = route;
        this.disrupted = disrupted;
        this.diverted = diverted;
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
    public boolean isDisrupted() {
        return disrupted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDiverted() {
        return diverted;
    }
}