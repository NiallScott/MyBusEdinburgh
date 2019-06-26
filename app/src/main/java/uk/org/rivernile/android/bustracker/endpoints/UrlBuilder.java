/*
 * Copyright (C) 2014 - 2019 Niall 'Rivernile' Scott
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

import android.net.Uri;
import androidx.annotation.NonNull;

/**
 * The {@code UrlBuilder} contains methods for constructing a URL for a particular environment.
 * 
 * @author Niall Scott
 */
public interface UrlBuilder {
    
    /**
     * Get a {@link Uri} instance which represents a URL for getting the topology ID from the bus
     * tracker API.
     * 
     * @return A {@link Uri} instance which represents a URL for getting the topology ID from the
     * bus tracker API.
     */
    @NonNull
    Uri getTopologyUrl();
    
    /**
     * Get a {@link Uri} instance which represents a URL for getting bus stop times from the bus
     * tracker API.
     * 
     * @param stopCodes The bus stop codes to request. Only the first 6 stop codes in the array
     * will be dealt with as the API only accepts 6 stop codes in a single request. If this is
     * empty, an {@link IllegalArgumentException} will be thrown.
     * @param numDepartures The number of departures to return for each service.
     * @return A {@link Uri} instance which represents a URL for getting bus stop times from the
     * bus tracker API.
     */
    @NonNull
    Uri getBusTimesUrl(@NonNull String[] stopCodes, int numDepartures);
    
    /**
     * Get a {@link Uri} instance which represents a URL for getting journey times for a specific
     * journey ID departing from a specific {@code stopCode} from the bus tracker API.
     * 
     * @param stopCode The bus stop code to request.
     * @param journeyId The unique ID of the journey leaving from the given {@code stopCode}.
     * @return A {@link Uri} instance which represents a URL for getting journey times from the
     * bus tracker API.
     */
    @NonNull
    Uri getJourneyTimesUrl(@NonNull String stopCode, @NonNull String journeyId);
    
    /**
     * Get a {@link Uri} instance which represents a URL for getting Twitter updates.
     * 
     * @return A {@link Uri} instance which represents a URL for getting Twitter updates.
     */
    @NonNull
    Uri getTwitterUpdatesUrl();
}