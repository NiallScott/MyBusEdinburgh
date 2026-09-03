/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopDetails
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import javax.inject.Inject

/**
 * Obtains and emits the stop details for the stop details screen.
 *
 * @author Niall Scott
 */
internal interface UiStopDetailsRetriever {

    /**
     * A [Flow] which emits the stop details for the given [stopIdentifier].
     *
     * @param stopIdentifier The stop to get the details for.
     * @return A [Flow] which emits the stop details. `null` is emitted if the stop is unknown.
     */
    fun getUiStopDetailsFlow(stopIdentifier: StopIdentifier): Flow<UiStopDetails?>
}

internal class RealUiStopDetailsRetriever @Inject constructor(
    private val busStopsRepository: BusStopsRepository,
    private val featureRepository: FeatureRepository,
    private val stopDistanceRetriever: UiStopDistanceRetriever
) : UiStopDetailsRetriever {

    private val hasStopMapUiFeature by lazy { featureRepository.hasStopMapUiFeature }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUiStopDetailsFlow(stopIdentifier: StopIdentifier): Flow<UiStopDetails?> {
        return busStopsRepository
            .getBusStopDetailsFlow(stopIdentifier = stopIdentifier)
            .flatMapLatest(::getUiStopDetailsFlowWithStopDetails)
    }

    private fun getUiStopDetailsFlowWithStopDetails(
        stopDetails: StopDetails?
    ): Flow<UiStopDetails?> {
        return if (stopDetails != null) {
            stopDistanceRetriever
                .getUiStopDistanceFlow(
                    stopLocation = stopDetails.location
                )
                .map { stopDistance ->
                    stopDetails
                        .toUiStopDetails(
                            stopDistance = stopDistance,
                            isMapShown = hasStopMapUiFeature
                        )
                }
        } else {
            flowOf(null)
        }
    }
}
