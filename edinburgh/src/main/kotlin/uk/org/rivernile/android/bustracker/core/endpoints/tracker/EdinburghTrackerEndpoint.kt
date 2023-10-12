/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

import kotlinx.serialization.SerializationException
import okio.IOException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.edinburghbustrackerapi.ApiKeyGenerator
import uk.org.rivernile.edinburghbustrackerapi.EdinburghBusTrackerApi
import javax.inject.Inject

/**
 * This class is the Edinburgh-specific implementation of a [TrackerEndpoint].
 *
 * @param api An instance of the [EdinburghBusTrackerApi].
 * @param apiKeyGenerator An implementation to generate API keys for the API.
 * @param liveTimesMapper An implementation used to map the API objects to our model objects.
 * @param errorMapper An implementation used to map errors.
 * @param connectivityRepository Used to check connectivity.
 * @param exceptionLogger Used to log exceptions.
 * @author Niall Scott
 */
internal class EdinburghTrackerEndpoint @Inject constructor(
    private val api: EdinburghBusTrackerApi,
    private val apiKeyGenerator: ApiKeyGenerator,
    private val liveTimesMapper: LiveTimesMapper,
    private val errorMapper: ErrorMapper,
    private val responseHandler: ResponseHandler,
    private val connectivityRepository: ConnectivityRepository,
    private val exceptionLogger: ExceptionLogger): TrackerEndpoint {

    override suspend fun getLiveTimes(
        stopCode: String,
        numberOfDepartures: Int): LiveTimesResponse {
        return if (connectivityRepository.hasInternetConnectivity) {
            try {
                val response = api.getBusTimes(
                    apiKeyGenerator.hashedApiKey,
                    numberOfDepartures,
                    stopCode)

                responseHandler.handleLiveTimesResponse(response)
            } catch (e: IOException) {
                exceptionLogger.log(e)
                LiveTimesResponse.Error.Io(e)
            } catch (e: SerializationException) {
                exceptionLogger.log(e)
                LiveTimesResponse.Error.Io(e)
            }
        } else {
            LiveTimesResponse.Error.NoConnectivity
        }
    }

    override suspend fun getLiveTimes(
        stopCodes: List<String>,
        numberOfDepartures: Int): LiveTimesResponse {
        return if (connectivityRepository.hasInternetConnectivity) {
            try {
                val response = api.getBusTimes(
                    apiKeyGenerator.hashedApiKey,
                    numberOfDepartures,
                    stopCodes[0],
                    stopCodes.getOrNull(1),
                    stopCodes.getOrNull(2),
                    stopCodes.getOrNull(3),
                    stopCodes.getOrNull(4))

                responseHandler.handleLiveTimesResponse(response)
            } catch (e: IOException) {
                exceptionLogger.log(e)
                LiveTimesResponse.Error.Io(e)
            } catch (e: SerializationException) {
                exceptionLogger.log(e)
                LiveTimesResponse.Error.Io(e)
            }
        } else {
            LiveTimesResponse.Error.NoConnectivity
        }
    }
}