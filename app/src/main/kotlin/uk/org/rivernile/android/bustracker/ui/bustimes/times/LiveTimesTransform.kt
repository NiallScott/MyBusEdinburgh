/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import javax.inject.Inject

/**
 * The responsibility of this class is to take a [UiResult] (produced by loading the live times),
 * and mutate this result in to a form which is suitable for consumption by the UI. Also,
 * this class reacts in response to some user actions, such as the user expanding/collapsing
 * services, changing their sorting preference etc. This class will mutate the data correctly
 * according to these UI events.
 *
 * @param preferenceRepository Where the user preferences are stored. This class uses some user
 * preferences to affect the output in response to the user's preference.
 * @param transformations An implementation which performs the actual transformations.
 * @param expandedServicesTracker Used to get user-expanded services.
 * @author Niall Scott
 */
@ViewModelScoped
class LiveTimesTransform @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val transformations: LiveTimesTransformations,
    private val expandedServicesTracker: ExpandedServicesTracker
) {

    /**
     * Get a [Flow] which is the result of transforming the [UiResult] in to a
     * [UiTransformedResult]. This will apply transformations based on the current UI state and user
     * preferences on to the [UiResult] and emit this as a [UiTransformedResult].
     *
     * @param result The [UiResult] that will be transformed.
     * @return A [Flow] where the [UiResult] has been transformed in to a [UiTransformedResult].
     */
    fun getLiveTimesTransformFlow(result: UiResult): Flow<UiTransformedResult> =
        combine(
            sortByTimeFlow,
            expandedServicesTracker.expandedServicesFlow
        ) { sortByTime, expandedServices ->
            transformResult(result, sortByTime, expandedServices)
        }

    /**
     * A [Flow] which emits the user's sorting preference.
     */
    private val sortByTimeFlow get() = preferenceRepository
        .isLiveTimesSortByTimeFlow
        .distinctUntilChanged()

    /**
     * Given a [UiResult], transform this in to a [UiTransformedResult].
     *
     * @param result The [UiResult] from upstream.
     * @param sortByTime Whether the services should be sorted by time (or by service name).
     * @param expandedServices A [Set] containing the descriptors of expanded services.
     * @return The produced [UiTransformedResult].
     */
    private fun transformResult(
        result: UiResult,
        sortByTime: Boolean,
        expandedServices: Set<ServiceDescriptor>
    ): UiTransformedResult {
        return when (result) {
            is UiResult.InProgress -> UiTransformedResult.InProgress
            is UiResult.Success -> mapSuccess(
                result,
                sortByTime,
                expandedServices
            )
            is UiResult.Error -> mapError(result)
        }
    }

    /**
     * Map the [UiResult.Success] case in to a [UiTransformedResult].
     *
     * A success upstream does not necessarily mean this method will yield success. If we produce
     * an empty [List] of [UiLiveTimesItem]s, then this method will produce a
     * [UiTransformedResult.Error].
     *
     * @param success The upstream [UiResult.Success].
     * @param sortByTime Whether the services should be sorted by time (or by service name).
     * @param expandedServices A [Set] containing the descriptors of expanded services.
     * @return The calculated [UiTransformedResult].
     */
    private fun mapSuccess(
        success: UiResult.Success,
        sortByTime: Boolean,
        expandedServices: Set<ServiceDescriptor>
    ): UiTransformedResult {
        return success
            .stop
            .services
            .let {
                transformations.sortServices(it, sortByTime)
            }
            .let {
                transformations.applyExpansions(it, expandedServices)
            }
            .ifEmpty { null }
            ?.let {
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
}
