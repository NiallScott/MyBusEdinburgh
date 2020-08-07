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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.SingleLiveTimesRequest
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import uk.org.rivernile.edinburghbustrackerapi.ApiKeyGenerator
import uk.org.rivernile.edinburghbustrackerapi.EdinburghBusTrackerApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is the Edinburgh-specific implementation of a [TrackerEndpoint].
 *
 * @param api An instance of the [EdinburghBusTrackerApi].
 * @param apiKeyGenerator An implementation to generate API keys for the API.
 * @param liveTimesMapper An implementation used to map the API objects to our model objects.
 * @param errorMapper An implementation used to map errors.
 * @param connectivityChecker An implementation of [ConnectivityChecker].
 * @author Niall Scott
 */
@Singleton
class EdinburghTrackerEndpoint @Inject internal constructor(
        private val api: EdinburghBusTrackerApi,
        private val apiKeyGenerator: ApiKeyGenerator,
        private val liveTimesMapper: LiveTimesMapper,
        private val errorMapper: ErrorMapper,
        private val connectivityChecker: ConnectivityChecker): TrackerEndpoint {

    override fun createLiveTimesRequest(stopCode: String, numberOfDepartures: Int)
            : TrackerRequest<LiveTimes> {
        val call = api.getBusTimes(apiKeyGenerator.hashedApiKey, numberOfDepartures, stopCode)

        return SingleLiveTimesRequest(call, liveTimesMapper, errorMapper, connectivityChecker)
    }

    override fun createLiveTimesRequest(stopCodes: Array<String>, numberOfDepartures: Int)
            : TrackerRequest<LiveTimes> {
        val newStopCodes = stopCodes.copyOf(5)
        val call = api.getBusTimes(
                apiKeyGenerator.hashedApiKey,
                numberOfDepartures,
                newStopCodes[0],
                newStopCodes[1],
                newStopCodes[2],
                newStopCodes[3],
                newStopCodes[4])

        return SingleLiveTimesRequest(call, liveTimesMapper, errorMapper, connectivityChecker)
    }
}