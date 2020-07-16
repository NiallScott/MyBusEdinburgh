/*
 * Copyright (C) 2014 - 2020 Niall 'Rivernile' Scott
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
import androidx.annotation.NonNull;

import uk.org.rivernile.android.bustracker.endpoints.BusTrackerEndpoint;
import uk.org.rivernile.android.fetchutils.loaders.support.SimpleAsyncTaskLoader;

/**
 * This {@link SimpleAsyncTaskLoader} deals with fetching bus times data from any source. The
 * implementation for fetching is not defined here. It's defined by the class represented by the
 * {@link BusParser} object.
 * 
 * @author Niall Scott
 * @see SimpleAsyncTaskLoader
 */
public class LiveBusTimesLoader extends SimpleAsyncTaskLoader<LiveTimesResult<LiveBusTimes>> {
    
    private final BusTrackerEndpoint endpoint;
    private final String[] stopCodes;
    private final int numberOfDepartures;
    
    /**
     * Create a new {@code LiveBusTimesLoader}. All arguments are mandatory, exceptions will be
     * thrown if they are not supplied.
     * 
     * @param context A {@link Context} instance.
     * @param endpoint The {@link BusTrackerEndpoint}.
     * @param stopCodes A {@link String} array of bus stop codes to load.
     * @param numberOfDepartures The number of departures for each service to load.
     */
    public LiveBusTimesLoader(
            @NonNull final Context context,
            @NonNull final BusTrackerEndpoint endpoint,
            @NonNull final String[] stopCodes,
            final int numberOfDepartures) {
        super(context);
        
        if (stopCodes.length == 0) {
            throw new IllegalArgumentException("Stop codes must be supplied.");
        }
        
        if (numberOfDepartures < 1) {
            throw new IllegalArgumentException("The number of departures must be greater than 0.");
        }
        
        this.endpoint = endpoint;
        this.stopCodes = stopCodes;
        this.numberOfDepartures = numberOfDepartures;
    }

    @Override
    public LiveTimesResult<LiveBusTimes> loadInBackground() {
        try {
            final LiveBusTimes busTimes = endpoint.getBusTimes(stopCodes, numberOfDepartures);
            return new LiveTimesResult<>(busTimes, busTimes.getReceiveTime());
        } catch (LiveTimesException e) {
            return new LiveTimesResult<>(e, SystemClock.elapsedRealtime());
        }
    }
}