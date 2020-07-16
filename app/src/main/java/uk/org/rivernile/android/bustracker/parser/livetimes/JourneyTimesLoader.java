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
import android.text.TextUtils;
import uk.org.rivernile.android.bustracker.endpoints.BusTrackerEndpoint;
import uk.org.rivernile.android.fetchutils.loaders.support.SimpleAsyncTaskLoader;

/**
 * This {@link SimpleAsyncTaskLoader} deals with fetching journey times data from any source. The
 * implementation for fetching is not defined here. It's defined by the class represented by the
 * {@link BusParser} object.
 * 
 * @author Niall Scott
 * @see SimpleAsyncTaskLoader
 */
public class JourneyTimesLoader extends SimpleAsyncTaskLoader<LiveTimesResult<Journey>> {
    
    private final BusTrackerEndpoint endpoint;
    private final String stopCode;
    private final String journeyId;
    
    /**
     * Create a new {@code JourneyTimesLoader}. All arguments are mandatory, exceptions will be
     * thrown if they are not supplied.
     * 
     * @param context A {@link Context} instance.
     * @param endpoint The {@link BusTrackerEndpoint} to use.
     * @param stopCode The {@code stopCode} that the service is departing from.
     * @param journeyId A unique ID for the journey to return.
     */
    public JourneyTimesLoader(
            @NonNull final Context context,
            @NonNull final BusTrackerEndpoint endpoint,
            @NonNull final String stopCode,
            @NonNull final String journeyId) {
        super(context);
        
        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("The stopCode should not be null or empty.");
        }
        
        if (TextUtils.isEmpty(journeyId)) {
            throw new IllegalArgumentException("The journeyId should not be null or empty.");
        }

        this.endpoint = endpoint;
        this.stopCode = stopCode;
        this.journeyId = journeyId;
    }

    @Override
    public LiveTimesResult<Journey> loadInBackground() {
        try {
            final Journey journey = endpoint.getJourneyTimes(stopCode, journeyId);
            return new LiveTimesResult<>(journey, journey.getReceiveTime());
        } catch (LiveTimesException e) {
            return new LiveTimesResult<>(e, SystemClock.elapsedRealtime());
        }
    }
}