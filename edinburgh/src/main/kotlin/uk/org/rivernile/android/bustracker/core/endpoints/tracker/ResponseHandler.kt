/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import okio.IOException
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes
import javax.inject.Inject

/**
 * This class provides implementations to handle tracker endpoint responses.
 *
 * @author Niall Scott
 */
internal class ResponseHandler @Inject constructor(
    private val liveTimesMapper: LiveTimesMapper,
    private val errorMapper: ErrorMapper) {

    /**
     * Handle the [Response] from the API and return the appropriate [LiveTimesResponse].
     *
     * @param response The response from the API.
     * @return The [LiveTimesResponse] to describe the response.
     * @throws IOException When there was an IO issue.
     */
    @Throws(IOException::class)
    fun handleLiveTimesResponse(response: Response<BusTimes>): LiveTimesResponse {
        return if (response.isSuccessful) {
            response.body()?.let {
                liveTimesMapper.mapToLiveTimes(it)
            } ?: liveTimesMapper.emptyLiveTimes()
        } else {
            errorMapper.mapHttpStatusCode(response.code())
        }
    }
}