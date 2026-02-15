/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import javax.inject.Inject

/**
 * This class retrieves the alerts state and passes this state on to
 * [NearestStopsFragmentViewModel]. This class exists to reduce the complexity of the view model
 * implementation, so logic is broken out in to here.
 *
 * @param featureRepository The feature repository.
 * @param alertsRepository The alerts repository.
 * @author Niall Scott
 */
class AlertsStateRetriever @Inject constructor(
    private val featureRepository: FeatureRepository,
    private val alertsRepository: AlertsRepository
) {

    /**
     * This [Flow] emits the visibility state of UI allowing the user to see or manipulate arrival
     * alerts.
     */
    val isArrivalAlertVisibleFlow get() = flowOf(featureRepository.hasArrivalAlertFeature)

    /**
     * This [Flow] emits the visibility state of UI allowing the user to see or manipulate
     * proximity alerts.
     */
    val isProximityAlertVisibleFlow get() = flowOf(featureRepository.hasProximityAlertFeature)

    /**
     * Get a [Flow] which uses the [selectedStopIdentifierFlow] as the currently selected stop
     * identifier and this [Flow] emits whether the given stop is added as an arrival alert.
     * `null` will be emitted when loading and when there is no stop identifier.
     *
     * @param selectedStopIdentifierFlow A [Flow] which emits the currently selected stop
     * identifier.
     * @return A [Flow] which emits whether the selected stop is added as an arrival alert or not,
     * or emits `null` when loading or no stop is selected.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHasArrivalAlertFlow(selectedStopIdentifierFlow: Flow<StopIdentifier?>) =
        selectedStopIdentifierFlow
            .flatMapLatest(this::loadHasArrivalAlert)

    /**
     * Get a [Flow] which uses the [selectedStopIdentifierFlow] as the currently selected stop
     * and this [Flow] emits whether the given stop is added as a proximity alert. `null` will be
     * emitted when loading and when there is no stop identifier.
     *
     * @param selectedStopIdentifierFlow A [Flow] which emits the currently selected stop
     * identifier.
     * @return A [Flow] which emits whether the selected stop is added as a proximity alert or not,
     * or emits `null` when loading or no stop is selected.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHasProximityAlertFlow(selectedStopIdentifierFlow: Flow<StopIdentifier?>) =
        selectedStopIdentifierFlow
            .flatMapLatest(this::loadHasProximityAlert)

    /**
     * Load whether the given [stopIdentifier] has an arrival alert set against it or not. If the
     * [stopIdentifier] is `null`, the returned [Flow] emits `null`. `null` will also be emitted in
     * lieu of a value while the status is loading.
     *
     * @param stopIdentifier The stop to get the arrival alert status for.
     * @return A [Flow] which emits whether the given stop identifier has an arrival alert set
     * against it or not.
     */
    private fun loadHasArrivalAlert(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        alertsRepository.hasArrivalAlertFlow(it)
            .onStart<Boolean?> { emit(null) }
    } ?: flowOf(null)

    /**
     * Load whether the given [stopIdentifier] has a proximity alert set against it or not. If the
     * [stopIdentifier] is `null`, the returned [Flow] emits `null`. `null` will also be emitted in
     * lieu of a value while the status is loading.
     *
     * @param stopIdentifier The stop to get the proximity alert status for.
     * @return A [Flow] which emits whether the given stop identifier has a proximity alert set
     * against it or not.
     */
    private fun loadHasProximityAlert(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        alertsRepository.hasProximityAlertFlow(it)
            .onStart<Boolean?> { emit(null) }
    } ?: flowOf(null)
}
