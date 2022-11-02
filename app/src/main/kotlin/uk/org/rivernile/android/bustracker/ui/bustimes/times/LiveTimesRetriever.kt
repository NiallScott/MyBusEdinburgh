/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.livetimes.LiveTimesRepository
import uk.org.rivernile.android.bustracker.core.livetimes.Result
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This retrieves the live times from the [LiveTimesRepository] and combines the returned results
 * with any other data it requires, such as the display colours for the services.
 *
 * @param liveTimesRepository Used to access live times.
 * @param servicesRepository Used to access service data.
 * @param liveTimesMapper Used to map the obtained data in to a [UiResult].
 * @author Niall Scott
 */
class LiveTimesRetriever @Inject constructor(
        private val liveTimesRepository: LiveTimesRepository,
        private val servicesRepository: ServicesRepository,
        private val liveTimesMapper: LiveTimesMapper) {

    /**
     * Attempt a fetch of [LiveTimes] and then return the results as a [UiResult] via a [Flow]. This
     * is a [Flow] because it incorporates progress, success and error events, and also handles some
     * underlying data changing causing new emissions.
     *
     * @param stopCode The stop code to get live times for.
     * @param numberOfDepartures The number of departures per service to obtain.
     * @return A [Flow] which contains the updating [UiResult].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLiveTimesFlow(stopCode: String, numberOfDepartures: Int): Flow<UiResult> =
            liveTimesRepository.getLiveTimesFlow(stopCode, numberOfDepartures)
                    .flatMapLatest {
                        obtainColoursForLiveTimes(stopCode, it)
                    }
                    .map {
                        liveTimesMapper.mapLiveTimesAndColoursToUiResult(stopCode, it.first,
                                it.second)
                    }

    /**
     * This method returns a [Flow] which is a [Pair] of [Result] and a [Map] of service name to
     * colour integer. The purpose of this is to combine a [Result] with service colours.
     *
     * The data is represented as a [Pair] at this stage to satisfy the [Flow] operator interfaces.
     * The [Pair] is mapped to a [UiResult] later which combines these two things.
     *
     * When the [Result] is [Result.InProgress] or [Result.Error], the service colour [Map] will be
     * `null` as there are no services to in these scenarios to load colours for.
     *
     * When [Result] is [Result.Success], and there are live times for the given [stopCode], and
     * there are colour results in the [ServicesRepository] for the services returned in the
     * [LiveTimes], the service colour [Map] will be populated. If there are no results, the [Map]
     * will be `null`.
     *
     * @param stopCode The stop code live times are being obtained for.
     * @param liveTimesResult The current [Result] of attempting to obtain [LiveTimes].
     * @return A [Flow] which is a [Pair] of [Result] and maybe a [Map] of service colours, if
     * available.
     */
    private fun obtainColoursForLiveTimes(stopCode: String, liveTimesResult: Result<LiveTimes>)
            : Flow<Pair<Result<LiveTimes>, Map<String, Int>?>> {
        return when (liveTimesResult) {
            is Result.Success -> getColoursForLiveTimes(stopCode, liveTimesResult.result)
            else -> createNullColourFlow()
        }.map {
            Pair(liveTimesResult, it)
        }
    }

    /**
     * Given a [LiveTimes] success result and the [stopCode] (as [LiveTimes] can contain results for
     * multiple stops, so we need the [stopCode] to be able to look up the correct stop), load the
     * service colours.
     *
     * If the [stopCode] isn't in the result, or there were no services, or none of the services
     * have colours attributed to them, then a [Flow] which only emits a single `null` will be
     * returned. Otherwise, a [Flow] will be returned which emits [Map]s of the service name to a
     * colour integer, and this will emit new items if the backing data changes.
     *
     * @param stopCode The stop code we are referencing in the [LiveTimes].
     * @param liveTimes This contains the [LiveTimes] data.
     * @return A [Flow] which emits [Map]s of service names to colours, or emits `null` if unable to
     * get colours.
     */
    private fun getColoursForLiveTimes(stopCode: String, liveTimes: LiveTimes)
            : Flow<Map<String, Int>?> {
        return liveTimes.stops[stopCode]
                ?.services
                ?.map { it.serviceName }
                ?.ifEmpty { null }
                ?.toSet()
                ?.let(servicesRepository::getColoursForServicesFlow)
                ?: createNullColourFlow()
    }

    /**
     * Create a [Flow] for the [Map] of service name to colour which merely emits a single `null`
     * item to represent that there's no data available.
     *
     * @return A [Flow] which emits a single `null` item to represent that there's no data
     * available.
     */
    private fun createNullColourFlow(): Flow<Map<String, Int>?> = flowOf(null)
}