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

package uk.org.rivernile.android.bustracker.endpoints;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.fetchutils.fetchers.HttpFetcher;

/**
 * This class defines an endpoint for accessing the bus tracker API via HTTP.
 * 
 * @author Niall Scott
 */
@Singleton
public class HttpBusTrackerEndpoint extends BusTrackerEndpoint {

    private final Context context;
    private final UrlBuilder urlBuilder;
    
    /**
     * Create a new {@code HttpBusTrackerEndpoint}.
     *
     * @param context A {@link Context} instance.
     * @param parser The parser to use to parse the data that comes from the source.
     * @param urlBuilder A {@link UrlBuilder} instance, used to construct URLs for contacting
     * remote resources.
     */
    @Inject
    HttpBusTrackerEndpoint(
            @NonNull final Context context,
            @NonNull final BusParser parser,
            @NonNull final UrlBuilder urlBuilder) {
        super(parser);

        this.context = context;
        this.urlBuilder = urlBuilder;
    }

    @NonNull
    @Override
    public LiveBusTimes getBusTimes(@NonNull final String[] stopCodes,
            final int numDepartures) throws LiveTimesException {
        final HttpFetcher fetcher = createHttpBuilder()
                .setUrl(urlBuilder.getBusTimesUrl(stopCodes, numDepartures).toString())
                .build();

        return getParser().getBusTimes(fetcher);
    }

    @NonNull
    @Override
    public Journey getJourneyTimes(@NonNull final String stopCode,
            @NonNull final String journeyId) throws LiveTimesException {
        final HttpFetcher fetcher = createHttpBuilder()
                .setUrl(urlBuilder.getJourneyTimesUrl(stopCode, journeyId).toString())
                .build();

        return getParser().getJourneyTimes(fetcher);
    }

    /**
     * Create a partially configured instance of {@link HttpFetcher.Builder}.
     *
     * @return A partially configured instance of {@link HttpFetcher.Builder}.
     */
    @NonNull
    private HttpFetcher.Builder createHttpBuilder() {
        return new HttpFetcher.Builder(context)
                .setAllowHostRedirects(false)
                .setUseCaches(false);
    }
}