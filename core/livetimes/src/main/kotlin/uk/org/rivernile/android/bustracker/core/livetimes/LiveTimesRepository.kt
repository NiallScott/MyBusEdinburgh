/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
 *
 */

package uk.org.rivernile.android.bustracker.core.livetimes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import javax.inject.Inject

/**
 * This repository is used to access live times.
 *
 * @author Niall Scott
 */
public interface LiveTimesRepository {

    /**
     * Get a [Flow] object which contains the [LiveTimesResult] of loading [LiveTimes].
     *
     * @param stopIdentifier The stop to load [LiveTimes] for.
     * @param numberOfDepartures The number of departures per services to obtain.
     * @return A [Flow] object containing the [LiveTimesResult] of loading [LiveTimes].
     */
    public fun getLiveTimesFlow(
        stopIdentifier: StopIdentifier,
        numberOfDepartures: Int
    ): Flow<LiveTimesResult>
}

internal class RealLiveTimesRepository @Inject constructor(
    private val trackerEndpoint: TrackerEndpoint,
    private val timeUtils: TimeUtils
) : LiveTimesRepository {

    override fun getLiveTimesFlow(
        stopIdentifier: StopIdentifier,
        numberOfDepartures: Int
    ): Flow<LiveTimesResult> = flow {
        emit(LiveTimesResult.InProgress)
        emit(fetchLiveTimes(stopIdentifier, numberOfDepartures))
    }

    /**
     * Attempts to fetch the live times from the server and returns the appropriate
     * [LiveTimesResult].
     *
     * @param stopIdentifier The stop to fetch live times for.
     * @param numberOfDepartures The number of departures per service to obtain.
     * @return A [LiveTimesResult] for the response.
     */
    private suspend fun fetchLiveTimes(
        stopIdentifier: StopIdentifier,
        numberOfDepartures: Int
    ): LiveTimesResult {
        return trackerEndpoint
            .getLiveTimes(stopIdentifier, numberOfDepartures)
            .toLiveTimesResult(timeUtils)
    }
}
