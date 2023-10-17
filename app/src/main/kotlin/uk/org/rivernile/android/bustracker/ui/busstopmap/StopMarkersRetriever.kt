/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetails
import javax.inject.Inject

/**
 * This class is used to retriever stop markers for display on the map.
 *
 * @param savedState A [SavedStateHandle] for observing the saved state.
 * @param busStopsRepository Used to retriever the stop details.
 * @param serviceListingRetriever Used to retrieve the service listing for selected stops.
 * @author Niall Scott
 */
@ViewModelScoped
class StopMarkersRetriever @Inject constructor(
    private val savedState: SavedStateHandle,
    private val busStopsRepository: BusStopsRepository,
    private val serviceListingRetriever: ServiceListingRetriever) {

    companion object {

        private const val STATE_SELECTED_SERVICES = "selectedServices"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
    }

    /**
     * A [Flow] which emits [List]s of [UiStopMarker]s for display on the map.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val stopMarkersFlow: Flow<List<UiStopMarker>?> get() =
            savedState.getStateFlow<Array<String>?>(STATE_SELECTED_SERVICES, null)
                    .flatMapLatest(this::loadBusStops)
                    .combine(serviceListingFlow, this::mapToUiStopMarkers)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val serviceListingFlow get() =
        savedState.getStateFlow<String?>(STATE_SELECTED_STOP_CODE, null)
                .flatMapLatest(serviceListingRetriever::getServiceListingFlow)

    /**
     * This is called when stops should be loaded.
     *
     * @param filteredServices Filtered services, if any.
     */
    private fun loadBusStops(filteredServices: Array<String>?) =
            busStopsRepository.getStopDetailsWithServiceFilterFlow(filteredServices?.toSet())

    /**
     * Given an optional [List] of [StopDetails] and an optional [UiServiceListing], map this to a
     * [List] of [UiStopMarker]. This will be `null` if [stopDetails] is `null` or empty.
     *
     * @param stopDetails The [List] of [StopDetails].
     * @param serviceListing An optional [UiServiceListing] if a stop is currently selected.
     * @return The mapped [List] of [UiStopMarker], or `null`.
     */
    private fun mapToUiStopMarkers(
        stopDetails: List<StopDetails>?,
        serviceListing: UiServiceListing?) =
        stopDetails?.map { sd ->
            val sl = serviceListing?.takeIf { it.stopCode == sd.stopCode }

            mapToUiStopMarker(sd, sl)
        }?.ifEmpty { null }

    /**
     * Given a [StopDetails], map this to a [UiStopMarker]. If [serviceListing] is not `null`, this
     * means this stop is currently selected.
     *
     * @param stopDetails The [StopDetails] to map from.
     * @param serviceListing The [UiServiceListing] if this stop is currently selected.
     * @return The mapped [UiStopMarker].
     */
    private fun mapToUiStopMarker(
        stopDetails: StopDetails,
        serviceListing: UiServiceListing?) =
        UiStopMarker(
            stopDetails.stopCode,
            stopDetails.stopName,
            LatLng(stopDetails.location.latitude, stopDetails.location.longitude),
            stopDetails.orientation,
            serviceListing)
}