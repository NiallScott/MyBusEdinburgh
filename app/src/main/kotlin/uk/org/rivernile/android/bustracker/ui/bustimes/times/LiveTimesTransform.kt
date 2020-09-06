/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.distinctUntilChanged
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import javax.inject.Inject

/**
 * The responsibility of this class is to take a [Flow] of [UiResult] (produced by loading the live
 * times), and mutate this result in to a form which is suitable for consumption by the UI. Also,
 * this class reacts in response to some user actions, such as the user expanding/collapsing
 * services, changing their sorting preference etc. This class will mutate the data correctly
 * according to these UI events.
 *
 * @param preferenceRepository Where the user preferences are stored. This class uses some user
 * preferences to affect the output in response to the user's preference.
 * @param transformations An implementation which performs the actual transformations.
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class LiveTimesTransform @Inject constructor(
        private val preferenceRepository: PreferenceRepository,
        private val transformations: LiveTimesTransformations) {

    /**
     * Get a [Flow] which is the result of transforming the [UiResult] in to a
     * [UiTransformedResult]. This will apply transformations based on the current UI state and user
     * preferences on to the [UiResult] and emit this as a [UiTransformedResult].
     *
     * @param liveTimesFlow The [Flow] of [UiResult] that will be transformed.
     * @param expandedServicesFlow A [Flow] containing the [Set] of expanded services.
     * @return A [Flow] where the [UiResult] has been transformed in to a [UiTransformedResult].
     */
    fun getLiveTimesTransformFlow(
            liveTimesFlow: Flow<UiResult>,
            expandedServicesFlow: Flow<Set<String>>): Flow<UiTransformedResult> {
        return liveTimesFlow.combine(getParametersFlow(expandedServicesFlow)) { liveTimes, params ->
            transformResult(liveTimes, params)
        }
    }

    /**
     * Get a [Flow] which contains the parameters which influence how the [Flow] of [UiResult] is
     * transformed.
     *
     * @param expandedServicesFlow The [Flow] which contains which services have been expanded.
     * @return A [Flow] containing the transformation parameters.
     */
    private fun getParametersFlow(expandedServicesFlow: Flow<Set<String>>) =
            getSortByTimeFlow()
                    .combine(getShowNightServicesFlow()) { sortByTime, showNightServices ->
                        Preferences(sortByTime, showNightServices)
                    }
                    .combine(expandedServicesFlow
                            .distinctUntilChanged()) { preferences, expandedServices ->
                        TransformationParameters(preferences, expandedServices)
                    }

    /**
     * Get the [Flow] which emits the user's sorting preference.
     *
     * @return The [Flow] which emits the user's sorting preference.
     */
    private fun getSortByTimeFlow() =
            // Distinct so that unnecessary computations are not carried out.
            preferenceRepository.isLiveTimesSortByTimeFlow().distinctUntilChanged()

    /**
     * Get the [Flow] which emits the user's night services preference.
     *
     * @return The [Flow] which emits the user's night services preference.
     */
    private fun getShowNightServicesFlow() =
            // Distinct so that unnecessary computations are not carried out.
            preferenceRepository.isLiveTimesShowNightServicesEnabledFlow().distinctUntilChanged()

    /**
     * Given a [UiResult], transform this in to a [UiTransformedResult].
     *
     * @param result The [UiResult] from upstream.
     * @param parameters The parameters to apply to the [UiResult].
     * @return The produced [UiTransformedResult].
     */
    private fun transformResult(
            result: UiResult,
            parameters: TransformationParameters) = when (result) {
        is UiResult.InProgress -> UiTransformedResult.InProgress
        is UiResult.Success -> mapSuccess(result, parameters)
        is UiResult.Error -> mapError(result)
    }

    /**
     * Map the [UiResult.Success] case in to a [UiTransformedResult].
     *
     * A success upstream does not necessarily mean this method will yield success. If we produce
     * an empty [List] of [UiLiveTimesItem]s, then this method will produce a
     * [UiTransformedResult.Error].
     *
     * @param success The upstream [UiResult.Success].
     * @param parameters The parameters required to know what transformations to perform on the
     * data.
     * @return The calculated [UiTransformedResult].
     */
    private fun mapSuccess(
            success: UiResult.Success,
            parameters: TransformationParameters): UiTransformedResult {
        return success.stop.services.let {
            transformations.filterNightServices(it, parameters.preferences.showNightServices)
        }.let {
            transformations.sortServices(it, parameters.preferences.sortByTime)
        }.let {
            transformations.applyExpansions(it, parameters.expandedServices)
        }.ifEmpty { null }?.let {
            UiTransformedResult.Success(success.receiveTime, it)
        } ?: UiTransformedResult.Error(success.receiveTime, ErrorType.NO_DATA)
    }

    /**
     * Map an [UiResult.Error] to a [UiTransformedResult.Error].
     *
     * @param error The [UiResult.Error] to map.
     * @return The error mapped to a [UiTransformedResult.Error].
     */
    private fun mapError(error: UiResult.Error) =
            UiTransformedResult.Error(error.receiveTime, error.error)

    /**
     * This class is used to store resolved preferences while in transition through the [Flow]
     * operators.
     */
    private data class Preferences(
            val sortByTime: Boolean,
            val showNightServices: Boolean)

    /**
     * This class is used to store the transformation parameters while in transition through the
     * [Flow] operators.
     *
     * @property preferences The resolved preference values.
     * @property expandedServices The resolved [Set] of expanded services.
     */
    private data class TransformationParameters(
            val preferences: Preferences,
            val expandedServices: Set<String>)
}