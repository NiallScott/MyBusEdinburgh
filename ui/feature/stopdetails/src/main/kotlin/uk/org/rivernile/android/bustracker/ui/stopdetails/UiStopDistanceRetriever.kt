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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.busstops.StopLocation
import uk.org.rivernile.android.bustracker.core.location.LatLon
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.location.LocationUpdate
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * This retrieves the device location and calculates the distance between the device and the stop.
 * It will also handle the various error cases - e.g. location services are turned off.
 *
 * @author Niall Scott
 */
internal interface UiStopDistanceRetriever {

    /**
     * A [Flow] that emits the [UiStopDistance] between the device and the given [stopLocation].
     *
     * The [UiStopDistance], as well as containing success cases, also provides the necessary
     * return types in the case the location cannot be determined, e.g. location services are turned
     * off. If `null` is emitted then this means the device does not support location services.
     *
     * @param stopLocation The location information of the stop.
     * @return A [Flow] that emits the [UiStopDistance] between the device and the given
     * [stopLocation].
     */
    fun getUiStopDistanceFlow(stopLocation: StopLocation): Flow<UiStopDistance?>
}

internal const val AWAIT_LOCATION_TIMEOUT_SECS = 10

internal class RealUiStopDistanceRetriever @Inject constructor(
    private val state: State,
    private val locationRepository: LocationRepository
) : UiStopDistanceRetriever {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUiStopDistanceFlow(stopLocation: StopLocation): Flow<UiStopDistance?> {
        return state
            .permissionsStateFlow
            .filterNotNull()
            .distinctUntilChanged()
            .combine(
                state
                    .isResumedFlow
                    .distinctUntilChanged(),
                ::Pair
            )
            .flatMapLatest { (permissionsState, isResumed) ->
                getUiStopDistanceFlowWithPermissionsState(
                    stopLocation = stopLocation,
                    isResumed = isResumed,
                    permissionsState = permissionsState
                )
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getUiStopDistanceFlowWithPermissionsState(
        stopLocation: StopLocation,
        isResumed: Boolean,
        permissionsState: PermissionsState
    ): Flow<UiStopDistance?> {
        return if (isResumed) {
            if (permissionsState.isPermissionsSufficient) {
                locationRepository
                    .locationUpdatesFlow
                    .flatMapLatest {
                        getUiStopDistanceWithLocationUpdate(
                            stopLocation = stopLocation,
                            locationUpdate = it
                        )
                    }
            } else {
                flowOf(UiStopDistance.InsufficientLocationPermissions)
            }
        } else {
            emptyFlow()
        }
    }

    private fun getUiStopDistanceWithLocationUpdate(
        stopLocation: StopLocation,
        locationUpdate: LocationUpdate
    ): Flow<UiStopDistance?> {
        return when (locationUpdate) {
            is LocationUpdate.AwaitingLocation -> flow {
                emit(UiStopDistance.ObtainingLocation)
                delay(AWAIT_LOCATION_TIMEOUT_SECS.seconds)
                emit(UiStopDistance.LocationUnknown)
            }
            is LocationUpdate.Update -> {
                val distance = locationRepository
                    .distanceBetween(
                        first = stopLocation.toLatLon(),
                        second = locationUpdate.location.latLon
                    )

                if (distance >= 1000f) {
                    flowOf(
                        UiStopDistance.Distance.Kilometers(
                            distance = distance / 1000f
                        )
                    )
                } else {
                    flowOf(
                        UiStopDistance.Distance.Meters(
                            distance = distance.toInt()
                        )
                    )
                }
            }
            is LocationUpdate.Error.NoLocationFeature -> flowOf(null)
            is LocationUpdate.Error.InsufficientLocationPermissions ->
                flowOf(UiStopDistance.InsufficientLocationPermissions)
            is LocationUpdate.Error.LocationOff -> flowOf(UiStopDistance.LocationOff)
        }
    }

    private fun StopLocation.toLatLon(): LatLon = LatLon(
        latitude = latitude,
        longitude = longitude
    )
}
