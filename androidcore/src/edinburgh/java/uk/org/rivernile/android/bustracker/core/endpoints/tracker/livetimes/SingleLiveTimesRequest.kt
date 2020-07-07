/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes

import retrofit2.Call
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.ErrorMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerRequest
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes
import java.io.IOException

/**
 * This class represents a [TrackerRequest] which only needs to perform a single request to the
 * endpoint to retrieve all live times.
 *
 * @param call The Retrofit call to the endpoint.
 * @param liveTimesMapper Used to map the response body to [LiveTimes].
 * @param errorMapper Used to map HTTP status code errors.
 * @author Niall Scott
 */
internal class SingleLiveTimesRequest(private val call: Call<BusTimes>,
                                      private val liveTimesMapper: LiveTimesMapper,
                                      private val errorMapper: ErrorMapper)
    : TrackerRequest<LiveTimes> {

    override fun performRequest() = try {
        val response = call.execute()

        if (response.isSuccessful) {
            response.body()?.let {
                liveTimesMapper.mapToLiveTimes(it)
            } ?: liveTimesMapper.emptyLiveTimes()
        } else {
            throw errorMapper.mapHttpStatusCode(response.code())
        }
    } catch (e: IOException) {
        throw NetworkException(e)
    }

    override fun cancel() {
        call.cancel()
    }
}