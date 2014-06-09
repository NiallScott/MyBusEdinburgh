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

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.utils.LoaderResult;
import uk.org.rivernile.android.utils.SimpleResultLoader;

/**
 * This Loader deals with fetching journey times data from any source. The
 * implementation for fetching is not defined here, it's defined by the class
 * represented by the BusParser object. This Loader simply puts it in another
 * Thread and the LoaderManager deals with its life cycle.
 * 
 * @author Niall Scott
 * @see SimpleResultLoader
 */
public class JourneyTimesLoader extends
        SimpleResultLoader<LoaderResult<Journey, LiveTimesException>>{
    
    private final BusApplication app;
    private final String stopCode;
    private final String journeyId;
    
    /**
     * Create a new JourneyTimesLoader. All arguments are mandatory,
     * exceptions will be thrown if they are not supplied.
     * 
     * @param context A Context instance.
     * @param stopCode The stopCode that the service is departing from.
     * @param journeyId A unique ID for the journey to return.
     */
    public JourneyTimesLoader(final Context context, final String stopCode,
            final String journeyId) {
        super(context);
        
        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("The stopCode should not be "
                    + "null or empty.");
        }
        
        if (TextUtils.isEmpty(journeyId)) {
            throw new IllegalArgumentException("The journeyId should not be "
                    + "null or empty.");
        }
        
        app = (BusApplication) context.getApplicationContext();
        this.stopCode = stopCode;
        this.journeyId = journeyId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoaderResult<Journey, LiveTimesException> loadInBackground() {
        LoaderResult<Journey, LiveTimesException> result;
        
        try {
            final Journey journey = app.getBusTrackerEndpoint()
                    .getJourneyTimes(stopCode, journeyId);
            result = new LoaderResult<Journey, LiveTimesException>(journey,
                    journey.getReceiveTime());
        } catch (LiveTimesException e) {
            result = new LoaderResult<Journey, LiveTimesException>(e,
                    SystemClock.elapsedRealtime());
        }
        
        return result;
    }
}