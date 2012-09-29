/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

import android.content.Context;
import java.util.HashMap;
import uk.org.rivernile.android.utils.SimpleResultLoader;

/**
 * This Loader deals with fetching bus times data from any source. The
 * implementation for fetching is not defined here, it's defined by the class
 * represented by the BusParser object. This Loader simply puts it in another
 * Thread and the LoaderManager deals with its life cycle.
 * 
 * @author Niall Scott
 * @see SimpleResultLoader
 */
public class BusTimesLoader extends SimpleResultLoader<BusTimesResult> {
    
    private final BusParser parser;
    private final String[] stopCodes;
    private final int numberOfDepartures;
    
    /**
     * Create a new BusTimesLoader. All arguments are mandatory, exceptions will
     * be thrown if they are not supplied.
     * 
     * @param context A Context instance.
     * @param parser A reference to the parser that is to be used.
     * @param stopCodes A String Array of bus stop codes to load.
     * @param numberOfDepartures The number of departures for each service to
     * load.
     */
    public BusTimesLoader(final Context context, final BusParser parser,
            final String[] stopCodes, final int numberOfDepartures) {
        super(context);
        
        // Ensure all arguments exist.
        if(parser == null)
            throw new IllegalArgumentException("The parser must not be null.");
        
        if(stopCodes == null || stopCodes.length == 0)
            throw new IllegalArgumentException("Stop codes must be supplied.");
        
        if(numberOfDepartures < 1)
            throw new IllegalArgumentException("The number of departures " +
                    "must be greater than 0.");
        
        this.parser = parser;
        this.stopCodes = stopCodes;
        this.numberOfDepartures = numberOfDepartures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BusTimesResult loadInBackground() {
        HashMap<String, BusStop> res = null;
        byte error = -1;

        try {
            // Get the bus times from the supplied parser.
            res = parser.getBusStopData(stopCodes, numberOfDepartures);
        } catch(BusParserException e) {
            // If an error occurrs, get it from the exception.
            error = e.getCode();
        }
        
        // If there's an error, give the result an error code, otherwise give
        // it the result HashMap.
        if(error < 0) {
            return new BusTimesResult(res, System.currentTimeMillis());
        } else {
            return new BusTimesResult(error, System.currentTimeMillis());
        }
    }
}