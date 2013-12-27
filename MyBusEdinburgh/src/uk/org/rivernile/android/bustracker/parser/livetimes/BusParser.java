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

import java.util.HashMap;

/**
 * The BusParser interface defines a single method that all parsers of the
 * bus times should implement. Essentially this method will take a list of bus
 * stops to get bus times for, and it will return a HashMap of stop codes and
 * BusStop objects.
 * 
 * @author Niall Scott
 */
public interface BusParser {
    
    /** This error is called when the error has not been defined in code yet. */
    public static final byte ERROR_UNKNOWN = 0;
    /**
     * This error is called when a connection could not be made to the server.
     */
    public static final byte ERROR_NOCONNECTION = 1;
    /** This error is called when the server name could not be resolved. */
    public static final byte ERROR_CANNOTRESOLVE = 2;
    /** This error is called when no stop code has been provided. */
    public static final byte ERROR_NOCODE = 3;
    /** This error is called when there was an error parsing the data. */
    public static final byte ERROR_PARSEERR = 4;
    /** This error is called when there was no data for this stop. */
    public static final byte ERROR_NODATA = 5;
    /**
     * This error is called when the URL the client thought it was requesting
     * data from differs from the URL it is receiving data from.
     */
    public static final byte ERROR_URLMISMATCH = 6;
    
    /**
     * Get data for a list of bus stops. This is usually bus times.
     * 
     * @param stopCodes The list of stop codes to return data for.
     * @param numDepartures The max number of departures to show for each
     * service.
     * @return A HashMap of String -> BusStop.
     * @throws BusParserException When an exception occurs during fetching or
     * parsing. Exceptions are wrapped in BusParserException to return a common
     * type.
     */
    public HashMap<String, BusStop> getBusStopData(final String[] stopCodes,
            final int numDepartures) throws BusParserException;
}