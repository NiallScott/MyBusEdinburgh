/*
 * Copyright (C) 2011 - 2018 Niall 'Rivernile' Scott
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

import androidx.annotation.NonNull;

import uk.org.rivernile.android.fetchutils.fetchers.Fetcher;

/**
 * The {@code BusParser} defines an interface of methods to communicate with the real-time server.
 * 
 * @author Niall Scott
 */
public interface BusParser {
    
    /**
     * Get bus times for the data returned by the given {@link Fetcher}.
     * 
     * @param fetcher The fetcher to use to retrieve data.
     * @return A {@link LiveBusTimes} instance which contains the live bus data.
     * @throws LiveTimesException When there was an error in fetching or parsing of the data.
     * This may be an instance of {@link LiveTimesException} or one of its children. The instance
     * may also have a cause {@link Throwable} included.
     */
    @NonNull
    LiveBusTimes getBusTimes(@NonNull Fetcher fetcher) throws LiveTimesException;
    
    /**
     * Get journey times for a journey returned by the given {@link Fetcher}.
     * 
     * @param fetcher The fetcher to use to retrieve data.
     * @return A {@link Journey} instance which contains the journey data.
     * @throws LiveTimesException When there was an error in fetching or parsing of the data.
     * This may be an instance of {@link LiveTimesException} or one of its children. The instance
     * may also have a cause {@link Throwable} included.
     */
    @NonNull
    Journey getJourneyTimes(@NonNull Fetcher fetcher) throws LiveTimesException;
}