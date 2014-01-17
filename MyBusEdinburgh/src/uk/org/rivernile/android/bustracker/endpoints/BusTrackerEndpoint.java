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

package uk.org.rivernile.android.bustracker.endpoints;

import java.util.HashMap;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParserException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;

/**
 * A bus tracker endpoint is an abstraction layer to help with testing and the
 * ability to slot in new implementations quickly. Subclasses define the way
 * that data is fetched for the parser. For example, a subclass could define
 * that data comes from HTTP, while another one may wish to take it from the
 * application assets. To get an instance of this object, call
 * {@link uk.org.rivernile.edinburghbustracker.android.Application#getBusTrackerEndpoint()}.
 * 
 * @author Niall Scott
 */
public abstract class BusTrackerEndpoint {
    
    private final BusParser parser;
    
    /**
     * Create a new endpoint.
     * 
     * @param parser The parser to use to parse the data that comes from the
     * source. Must not be null.
     */
    public BusTrackerEndpoint(final BusParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("The parser must not be null.");
        }
        
        this.parser = parser;
    }
    
    /**
     * Get the parser instance.
     * 
     * @return The parser instance.
     */
    protected final BusParser getParser() {
        return parser;
    }
    
    /**
     * Get the bus times for the bus stops specified in stopCodes.
     * 
     * @param stopCodes The bus stops to get times for.
     * @param numDepartures The number of departures to get for each service at
     * each stop.
     * @return A HashMap of stopCode -> BusStop.
     * @throws BusParserException If there was a problem while parsing data.
     */
    public abstract HashMap<String, BusStop> getBusTimes(String[] stopCodes,
            int numDepartures) throws BusParserException;
}