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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopLocation
import uk.org.rivernile.android.bustracker.core.config.ConfigRepository
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.location.LatLon
import uk.org.rivernile.android.bustracker.core.location.Location
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.location.LocationUpdate
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Used to retrieve the nearest stops to the device. This contains the logic used to determine the
 * current state of the location services on the device.
 *
 * @author Niall Scott
 */
internal interface NearestStopsRetriever {

    /**
     * A [Flow] which emits the current state of the nearest stops.
     */
    val nearestStopsStateFlow: Flow<NearestStopsState>
}

private const val LOCATION_UNKNOWN_PROGRESS_MILLIS = 10000L

internal class RealNearestStopsRetriever @Inject constructor(
    private val state: State,
    private val locationRepository: LocationRepository,
    private val configRepository: ConfigRepository,
    private val busStopsRepository: BusStopsRepository,
    private val locationAccuracyGenerator: UiLocationAccuracyGenerator
) : NearestStopsRetriever {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val nearestStopsStateFlow get() = state
        .permissionsStateFlow
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest(::getNearestStopsFlowWithPermissionsState)
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNearestStopsFlowWithPermissionsState(
        permissionsState: PermissionsState
    ): Flow<NearestStopsState> {
        val combinedFlow = combine(
            locationRepository.locationUpdatesFlow,
            state.selectedServicesFlow,
            locationAccuracyGenerator.getUiLocationAccuracyFlow(permissionsState),
            ::Triple
        )

        return combinedFlow
            .distinctUntilChanged()
            .flatMapLatest {
                getNearestStopsFlowWithLocationUpdateAndSelectedServices(
                    locationUpdate = it.first,
                    selectedServices = it.second,
                    locationAccuracy = it.third
                )
            }
    }

    private fun getNearestStopsFlowWithLocationUpdateAndSelectedServices(
        locationUpdate: LocationUpdate,
        selectedServices: Set<ServiceDescriptor>?,
        locationAccuracy: UiLocationAccuracy?
    ): Flow<NearestStopsState> {
        return when (locationUpdate) {
            is LocationUpdate.AwaitingLocation -> flow {
                emit(NearestStopsState.AwaitingLocation)
                delay(LOCATION_UNKNOWN_PROGRESS_MILLIS.milliseconds)
                emit(NearestStopsState.Error.LocationUnknown)
            }
            is LocationUpdate.Update -> getNearestStopsFlowWithLocationAndSelectedServices(
                location = locationUpdate.location,
                selectedServices = selectedServices,
                locationAccuracy = locationAccuracy
            )
            is LocationUpdate.Error.NoLocationFeature ->
                flowOf(NearestStopsState.Error.NoLocationFeature)
            is LocationUpdate.Error.InsufficientLocationPermissions ->
                flowOf(NearestStopsState.Error.InsufficientLocationPermissions)
            is LocationUpdate.Error.LocationOff -> flowOf(NearestStopsState.Error.LocationOff)
        }
    }

    private fun getNearestStopsFlowWithLocationAndSelectedServices(
        location: Location,
        selectedServices: Set<ServiceDescriptor>?,
        locationAccuracy: UiLocationAccuracy?
    ): Flow<NearestStopsState> {
        val latLon = location.latLon
        val latitudeSpan = configRepository.nearestStopsLatitudeSpan
        val longitudeSpan = configRepository.nearestStopsLongitudeSpan

        val minLatitude = latLon.latitude - latitudeSpan
        val maxLatitude = latLon.latitude + latitudeSpan
        val minLongitude = latLon.longitude - longitudeSpan
        val maxLongitude = latLon.longitude + longitudeSpan

        return busStopsRepository
            .getStopDetailsWithinSpanFlow(
                minLatitude = minLatitude,
                minLongitude = minLongitude,
                maxLatitude = maxLatitude,
                maxLongitude = maxLongitude,
                serviceFilter = selectedServices
            )
            .map { stopDetails ->
                NearestStopsState.Stops(
                    stops = stopDetails
                        ?.toNearestStops {
                            locationRepository
                                .distanceBetween(it.toLatLon(), latLon)
                                .absoluteValue
                                .toInt()
                        }
                        ?.ifEmpty { null },
                    locationAccuracy = locationAccuracy
                )
            }
    }

    private fun StopLocation.toLatLon() = LatLon(
        latitude = latitude,
        longitude = longitude
    )
}
