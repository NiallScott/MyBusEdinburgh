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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopDetails
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import javax.inject.Inject

/**
 * This class is used to retrieve items for display on the UI. It combines the various data sources
 * and provides a [Flow] which emits a [List] of [UiItem]s.
 *
 * @param busStopsRepository The repository to obtain bus stop data from.
 * @param distanceRetriever Used to retrieve the distance between the device and the stop.
 * @param servicesRetriever The repository to obtain service data from.
 * @param featureRepository Used to determine device feature availability.
 * @author Niall Scott
 */
class UiItemRetriever @Inject constructor(
    private val busStopsRepository: BusStopsRepository,
    private val distanceRetriever: DistanceRetriever,
    private val servicesRetriever: ServicesRetriever,
    private val featureRepository: FeatureRepository) {

    private val hasMapFeature by lazy { featureRepository.hasStopMapUiFeature }

    /**
     * Create a [Flow] which emits a [List] of [UiItem]s.
     *
     * @param stopIdentifierFlow This provides the stop code for the data to be loaded.
     * @param permissionsStateFlow This provides the current permissions state.
     * @param coroutineScope The [CoroutineScope] to execute shared [Flow]s under.
     * @return A [Flow] which emits a [List] of [UiItem]s.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun createUiItemFlow(
        stopIdentifierFlow: SharedFlow<StopIdentifier?>,
        permissionsStateFlow: Flow<PermissionsState>,
        coroutineScope: CoroutineScope
    ): Flow<List<UiItem>> {
        val busStopDetailsFlow = stopIdentifierFlow
            .flatMapLatest(this::loadStop)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), 1)

        val distanceFlow = distanceRetriever.createDistanceFlow(
            permissionsStateFlow,
            busStopDetailsFlow
        )

        val servicesFlow = stopIdentifierFlow
            .flatMapLatest(this::loadServices)

        return combine(
            busStopDetailsFlow,
            distanceFlow,
            servicesFlow,
            this::assembleItems)
    }

    /**
     * Given a [stopIdentifier], load the stop details for this stop code.
     *
     * @param stopIdentifier The stop code to load stop details for.
     * @return A [Flow] which emits details for the given [stopIdentifier], or emits `null` when
     * [stopIdentifier] is `null`.
     */
    private fun loadStop(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        busStopsRepository.getBusStopDetailsFlow(it)
    } ?: flowOf(null)

    /**
     * Given a [stopIdentifier], load services for that stop.
     *
     * @param stopIdentifier The stop code to load services for.
     * @return A [Flow] containing service details for this stop, or a [Flow] which emits `null`
     * when the [stopIdentifier] is `null`.
     */
    private fun loadServices(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        servicesRetriever.getServicesFlow(it)
    } ?: flowOf(null)

    /**
     * Given the various streams of data, assemble them in to a [List] of [UiItem]s.
     *
     * @param stopDetails The details for the stop.
     * @param distance An object representing the distance between the device and the stop, and
     * various failure states which can occur with this.
     * @param services A [List] of services for the stop.
     * @return A [List] of [UiItem]s for display on the UI.
     */
    private fun assembleItems(
        stopDetails: StopDetails?,
        distance: UiItem.Distance,
        services: List<UiItem.Service>?
    ): List<UiItem> {
        val items = mutableListOf<UiItem>()

        stopDetails?.let {
            // Add a map item only if we have stop details (which contains coordinates) and the
            // map feature is on.
            if (hasMapFeature) {
                items += UiItem.Map(
                    it.location.latitude,
                    it.location.longitude,
                    it.orientation)
            }
        }

        // Add the distance item only when it's not the NoLocationFeature item.
        if (distance !is UiItem.Distance.NoLocationFeature) {
            items += distance
        }

        // Add all the service items. If they don't exist, add a NoServices item instead.
        services?.ifEmpty { null }?.let(items::addAll)
            ?: items.add(UiItem.NoServices)

        return items
    }
}
