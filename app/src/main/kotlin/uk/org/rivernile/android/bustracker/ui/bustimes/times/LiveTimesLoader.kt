/*
 * Copyright (C) 2020 - 2021 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import javax.inject.Inject

/**
 * This class loads [Flow]s of live time data. It uses triggers and data from other sources to load
 * the live times.
 *
 * - A stop code is required to load live times. If the stop code is `null`, then an error will be
 *   emitted. The stop code is updatable - if it is updated, the data will be reloaded.
 * - We need to know how many departures per service to load. This is obtained from preferences. We
 *   wait until we have this value before we load the data. If this value is updated, a reload of
 *   the data will occur.
 * - A refresh trigger is supported. When a new item appears in the refresh trigger [Flow], the data
 *   will be reloaded with the current stop code and number of departures data.
 *
 * @param liveTimesRetriever This is used to retrieve live times.
 * @param preferenceRepository Used to obtain preferences which are required to load the data.
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class LiveTimesLoader @Inject constructor(
        private val liveTimesRetriever: LiveTimesRetriever,
        private val preferenceRepository: PreferenceRepository) {

    /**
     * Given a [Flow] of stop codes, and a [Flow] to represent refresh triggers, return a [Flow]
     * which provides live times.
     *
     * If [stopCodeFlow] emits a `null`, then a `Flow` containing a [UiResult.Error] of
     * [ErrorType.NO_STOP_CODE] will be emitted.
     *
     * @param stopCodeFlow This [Flow] provides stop codes which should be loaded. A `null` stop
     * code will emit an error. If non-`null`, a load will be attempted (which itself may emit
     * errors). A change in stop code will cause a new load to occur.
     * @param refreshFlow This [Flow] is used as a refresh trigger. Every time a refresh of the data
     * should occur, this [Flow] will emit a new [Unit].
     * @return A [Flow] which emits [UiResult]s of attempting to load live times.
     */
    fun loadLiveTimesFlow(
            stopCodeFlow: Flow<String?>,
            refreshFlow: Flow<Unit?>): Flow<UiResult> {
        return combine(
                stopCodeFlow,
                preferenceRepository.getLiveTimesNumberOfDeparturesFlow(),
                refreshFlow) { stopCode, numberOfDepartures, _ ->
            LoadParams(stopCode, numberOfDepartures)
        }.flatMapLatest { params ->
            params.stopCode?.let {
                liveTimesRetriever.getLiveTimesFlow(it, params.numberOfDepartures)
            } ?: flowOf(UiResult.Error(Long.MAX_VALUE, ErrorType.NO_STOP_CODE))
        }
    }

    /**
     * This data class stores the live times loading parameters as it flows through the [Flow]
     * operators.
     *
     * @param stopCode The stop code. This can be `null`, but if it is, expect the [Flow] to produce
     * an error.
     * @param numberOfDepartures The number of departures per service.
     */
    private data class LoadParams(
            val stopCode: String?,
            val numberOfDepartures: Int)
}