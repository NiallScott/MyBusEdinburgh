/*
 * Copyright (C) 2019 - 2026 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerializationException
import okio.IOException
import uk.org.rivernile.android.bustracker.core.domain.AtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.EdinburghOpenApi
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.emptyLiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.toLiveTimes
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import javax.inject.Inject
import kotlin.time.Instant

private const val HTTP_UNAUTHORISED = 401

/**
 * This class is the Edinburgh-specific implementation of a [TrackerEndpoint].
 *
 * @param api An instance of the [EdinburghOpenApi].
 * @param connectivityRepository Used to check connectivity.
 * @param exceptionLogger Used to log exceptions.
 * @param timeUtils Time utilities, to access time functionality.
 * @author Niall Scott
 */
internal class EdinburghTrackerEndpoint @Inject constructor(
    private val api: EdinburghOpenApi,
    private val connectivityRepository: ConnectivityRepository,
    private val exceptionLogger: ExceptionLogger,
    private val timeUtils: TimeUtils
): TrackerEndpoint {

    private val timeZone = TimeZone.of("Europe/London")

    override suspend fun getLiveTimes(
        stopIdentifier: StopIdentifier,
        numberOfDepartures: Int
    ): LiveTimesResponse {
        return if (connectivityRepository.hasInternetConnectivity) {
            try {
                val response = when (stopIdentifier) {
                    is AtcoStopIdentifier -> {
                        api.getStopEventsWithAtcoCode(
                            atcoCode = stopIdentifier.atcoCode,
                            numberOfDepartures = numberOfDepartures
                        )
                    }
                    is NaptanStopIdentifier -> {
                        api.getStopEventsWithSmsCode(
                            smsCode = stopIdentifier.naptanStopCode,
                            numberOfDepartures = numberOfDepartures
                        )
                    }
                }

                if (response.isSuccessful) {
                    val receiveTime = timeUtils.now
                    val liveTimes = response
                        .body()
                        ?.toLiveTimes(
                            stopIdentifier = stopIdentifier,
                            numberOfDepartures = numberOfDepartures,
                            receiveTime = receiveTime,
                            timeZone = timeZone
                        )
                        ?: emptyLiveTimes(receiveTime = receiveTime)

                    LiveTimesResponse.Success(
                        liveTimes = liveTimes
                    )
                } else {
                    val errorBody = response.errorBody()?.string()?.take(256)
                    val error = "Status code = ${response.code()}; Body = $errorBody"

                    exceptionLogger.log(RuntimeException(error))

                    when (response.code()) {
                        HTTP_UNAUTHORISED -> LiveTimesResponse.Error.ServerError.Authentication
                        else -> LiveTimesResponse.Error.ServerError.Other(error = errorBody)
                    }
                }
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
        stopIdentifiers: List<StopIdentifier>,
        numberOfDepartures: Int
    ) = coroutineScope {
        var minReceiveTime = Instant.DISTANT_FUTURE
        val stops = mutableMapOf<StopIdentifier, Stop>()

        stopIdentifiers
            .map { stopIdentifier ->
                async {
                    getLiveTimes(
                        stopIdentifier = stopIdentifier,
                        numberOfDepartures = numberOfDepartures
                    )
                }
            }
            .awaitAll()
            .forEach { response ->
                if (response is LiveTimesResponse.Success) {
                    val liveTimes = response.liveTimes

                    if (liveTimes.receiveTime < minReceiveTime) {
                        minReceiveTime = liveTimes.receiveTime
                    }

                    stops += liveTimes.stops
                } else {
                    return@coroutineScope response
                }
            }

        LiveTimesResponse.Success(
            liveTimes = LiveTimes(
                stops = stops.toMap(),
                receiveTime = minReceiveTime
            )
        )
    }
}
