/*
 * Copyright (C) 2011 - 2016 Niall 'Rivernile' Scott
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.json.JSONException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.fetchutils.fetchers.Fetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;

/**
 * This is the Edinburgh specific implementation of {@link BusParser}.
 * 
 * @author Niall Scott
 */
public final class EdinburghParser implements BusParser {
    
    @NonNull
    @Override
    public LiveBusTimes getBusTimes(@NonNull final Fetcher fetcher) throws LiveTimesException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);

            return BusTimesParser.parseBusTimes(reader.getJSONObject());
        } catch (IOException e) {
            throw new LiveTimesException(e);
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        }
    }

    @NonNull
    @Override
    public Journey getJourneyTimes(@NonNull final Fetcher fetcher) throws LiveTimesException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);

            return JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());
        } catch (IOException e) {
            throw new LiveTimesException(e);
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        }
    }
    
    /**
     * A utility method to add minutes on to a {@link GregorianCalendar}.
     *
     * @param time A {@link GregorianCalendar} instance to use. If {@code null}, a new instance
     * will be created with the UK locale and will be initialised to the current time.
     * @param minutes The number of minutes to add.
     * @return A {@link Date} instance which points to the newly added time.
     */
    @NonNull
    static Date addMinutes(@Nullable GregorianCalendar time, final int minutes) {
        if (time == null) {
            time = new GregorianCalendar(Locale.UK);
        }

        time.add(Calendar.MINUTE, minutes);

        return time.getTime();
    }
    
    /**
     * Convert service names in to their public display names. For example, the tram isn't in the
     * system as a tram.
     * 
     * @param serviceName The service name to possibly convert.
     * @return The converted service name, or the same service name if no conversion was required.
     */
    @Nullable
    static String serviceNameConversion(@Nullable final String serviceName) {
        return "50".equals(serviceName) || "T50".equals(serviceName) ? "TRAM" : serviceName;
    }
}