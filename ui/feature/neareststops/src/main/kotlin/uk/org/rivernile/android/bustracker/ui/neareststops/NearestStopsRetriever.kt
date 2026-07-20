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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopLocation
import uk.org.rivernile.android.bustracker.core.config.ConfigRepository
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import javax.inject.Inject
import kotlin.math.absoluteValue

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

internal class RealNearestStopsRetriever @Inject constructor(
    private val state: State,
    private val locationRepository: LocationRepository,
    private val configRepository: ConfigRepository,
    private val busStopsRepository: BusStopsRepository
) : NearestStopsRetriever {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val nearestStopsStateFlow: Flow<NearestStopsState> get() {
        return if (locationRepository.hasLocationFeature) {
            state
                .permissionsStateFlow
                .filterNotNull()
                .map(PermissionsState::isPermissionsSufficient)
                .distinctUntilChanged()
                .flatMapLatest(::getNearestStopsFlowWithPermissionsState)
        } else {
            flowOf(NearestStopsState.Error.NoLocationFeature)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNearestStopsFlowWithPermissionsState(
        isPermissionsSufficient: Boolean
    ): Flow<NearestStopsState> {
        return if (isPermissionsSufficient) {
            locationRepository
                .isLocationEnabledFlow
                .distinctUntilChanged()
                .flatMapLatest(::getNearestStopsFlowWithEnabledLocation)
        } else {
            flowOf(NearestStopsState.Error.InsufficientLocationPermissions)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNearestStopsFlowWithEnabledLocation(
        isLocationEnabled: Boolean
    ): Flow<NearestStopsState> {
        return if (isLocationEnabled) {
            locationRepository
                .userVisibleLocationFlow
                .flatMapLatest(::getNearestStopsFlowWithLocation)
                .onStart { emit(NearestStopsState.Error.LocationUnknown) }
        } else {
            flowOf(NearestStopsState.Error.LocationOff)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNearestStopsFlowWithLocation(
        deviceLocation: DeviceLocation
    ): Flow<NearestStopsState> {
        return state
            .selectedServicesFlow
            .flatMapLatest {
                getNearestStopsFlowWithLocationAndSelectedServices(
                    deviceLocation = deviceLocation,
                    selectedServices = it
                )
            }
    }

    private fun getNearestStopsFlowWithLocationAndSelectedServices(
        deviceLocation: DeviceLocation,
        selectedServices: Set<ServiceDescriptor>?
    ): Flow<NearestStopsState> {
        val latitudeSpan = configRepository.nearestStopsLatitudeSpan
        val longitudeSpan = configRepository.nearestStopsLongitudeSpan

        val minLatitude = deviceLocation.latitude - latitudeSpan
        val maxLatitude = deviceLocation.latitude + latitudeSpan
        val minLongitude = deviceLocation.longitude - longitudeSpan
        val maxLongitude = deviceLocation.longitude + longitudeSpan

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
                                .distanceBetween(it.toDeviceLocation(), deviceLocation)
                                .absoluteValue
                                .toInt()
                        }
                        ?.ifEmpty { null }
                )
            }
    }

    private fun StopLocation.toDeviceLocation() = DeviceLocation(
        latitude = latitude,
        longitude = longitude
    )
}
