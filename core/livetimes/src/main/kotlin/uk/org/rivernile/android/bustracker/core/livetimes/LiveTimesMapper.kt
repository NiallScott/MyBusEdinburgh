/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject

/**
 * This class provides lives times mapping implementations.
 *
 * @param timeUtils Time utils.
 * @author Niall Scott
 */
internal class LiveTimesMapper @Inject constructor(
    private val timeUtils: TimeUtils
) {

    /**
     * Map a [LiveTimesResponse] to a [LiveTimesResult].
     *
     * @param response A [LiveTimesResponse].
     * @return The mapped [LiveTimesResult].
     */
    fun mapToLiveTimesResult(response: LiveTimesResponse): LiveTimesResult {
        return when (response) {
            is LiveTimesResponse.Success -> LiveTimesResult.Success(response.liveTimes)
            is LiveTimesResponse.Error.NoConnectivity ->
                LiveTimesResult.Error.NoConnectivity(timeUtils.currentTimeMills)
            is LiveTimesResponse.Error.Io ->
                LiveTimesResult.Error.Io(timeUtils.currentTimeMills, response.throwable)
            is LiveTimesResponse.Error.ServerError.Authentication ->
                LiveTimesResult.Error.ServerError.Authentication(timeUtils.currentTimeMills)
            is LiveTimesResponse.Error.ServerError.Maintenance ->
                LiveTimesResult.Error.ServerError.Maintenance(timeUtils.currentTimeMills)
            is LiveTimesResponse.Error.ServerError.SystemOverloaded ->
                LiveTimesResult.Error.ServerError.SystemOverloaded(timeUtils.currentTimeMills)
            is LiveTimesResponse.Error.ServerError.Other ->
                LiveTimesResult.Error.ServerError.Other(timeUtils.currentTimeMills)
        }
    }
}