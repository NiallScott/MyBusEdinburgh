/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetails
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import javax.inject.Inject

/**
 * This class is used to obtain a [Flow] of [UiItem.Distance], which encapsulates a distance from a
 * stop, or possible error conditions.
 *
 * The purpose of this class is to combine different states, such as location availability,
 * permissions, device capabilities, etc, to then emit the correct state of distance to the stop.
 *
 * @param locationRepository Used to access location information.
 * @author Niall Scott
 */
class DistanceRetriever @Inject constructor(
    private val locationRepository: LocationRepository
) {

    /**
     * Create a [Flow] which emits [UiItem.Distance] items. As a pre-requisite, it requires the
     * [permissionsStateFlow] and [stopDetailsFlow] to emit correct data.
     *
     * @param permissionsStateFlow A [Flow] which emits the current [PermissionsState]. Distances
     * can only be determined when we have the required permissions to obtain location.
     * @param stopDetailsFlow To calculate the distance, we also require the stop's latitude and
     * longitude. This is contained within [StopDetails] objects, so we need this [Flow] to get the
     * latest stop data to calculate the distance from.
     * @return A [Flow] which emits [UiItem.Distance] items.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun createDistanceFlow(
        permissionsStateFlow: Flow<PermissionsState>,
        stopDetailsFlow: Flow<StopDetails?>
    ): Flow<UiItem.Distance> {
        return if (locationRepository.hasLocationFeature) {
            permissionsStateFlow
                .map(this::isPermissionsSufficient)
                .distinctUntilChanged()
                .flatMapLatest { handlePermissionsStateChanged(it, stopDetailsFlow) }
        } else {
            flowOf(UiItem.Distance.NoLocationFeature)
        }
    }

    /**
     * Handle the state of the permissions changing.
     *
     * When the permissions are insufficient, a [Flow] which emits a single
     * [UiItem.Distance.PermissionDenied] is returned. Otherwise, another [Flow] of
     * [UiItem.Distance] will be returned.
     *
     * @param isPermissionsSufficient Are the granted permissions sufficient for obtaining a device
     * location?
     * @param stopDetailsFlow A [Flow] which emits [StopDetails].
     * @return A [Flow] of [UiItem.Distance].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handlePermissionsStateChanged(
        isPermissionsSufficient: Boolean,
        stopDetailsFlow: Flow<StopDetails?>
    ): Flow<UiItem.Distance> {
        return if (isPermissionsSufficient) {
            locationRepository.isLocationEnabledFlow
                .distinctUntilChanged()
                .flatMapLatest { handleLocationEnabled(it, stopDetailsFlow) }
        } else {
            flowOf(UiItem.Distance.PermissionDenied)
        }
    }

    /**
     * Handle the location enabled state changing.
     *
     * When location is not enabled, this method will return a [Flow] that emits a single
     * [UiItem.Distance.LocationOff]. Otherwise, another [Flow] of [UiItem.Distance] will be
     * returned.
     *
     * @param isEnabled Is location enabled?
     * @param stopDetailsFlow A [Flow] which emits [StopDetails].
     * @return A [Flow] of [UiItem.Distance].
     */
    private fun handleLocationEnabled(
        isEnabled: Boolean,
        stopDetailsFlow: Flow<StopDetails?>
    ) = if (isEnabled) {
        locationRepository.userVisibleLocationFlow
            .combine(stopDetailsFlow, this::calculateDistanceBetween)
            .onStart { emit(UiItem.Distance.Unknown) }
            .distinctUntilChanged()
    } else {
        flowOf(UiItem.Distance.LocationOff)
    }

    /**
     * Given the device [location] and [stopDetails], attempt to calculate the distance between the
     * two.
     *
     * If [stopDetails] is `null`, then [UiItem.Distance.Unknown] will be returned.
     *
     * Additionally, [UiItem.Distance.Unknown] will be returned when a distance could not be
     * determined.
     *
     * @param location The device's location.
     * @param stopDetails The details for the stop.
     * @return A [UiItem.Distance.Known] when the distance is known, otherwise
     * [UiItem.Distance.Unknown].
     */
    private fun calculateDistanceBetween(
        location: DeviceLocation,
        stopDetails: StopDetails?
    ): UiItem.Distance {
        val stopLocation = stopDetails?.let {
            DeviceLocation(it.location.latitude, it.location.longitude)
        } ?: return UiItem.Distance.Unknown

        val distanceBetweenMeters = locationRepository.distanceBetween(location, stopLocation)

        return if (distanceBetweenMeters >= 0f) {
            UiItem.Distance.Known(distanceBetweenMeters / 1000)
        } else {
            UiItem.Distance.Unknown
        }
    }

    /**
     * Given a [PermissionsState], is the granted permissions sufficient to allow for obtaining a
     * device location?
     *
     * @param permissionsState The [PermissionsState] to evaluate.
     * @return `true` if the permissions are sufficient, otherwise `false`.
     */
    private fun isPermissionsSufficient(permissionsState: PermissionsState) =
        permissionsState.coarseLocationPermission == PermissionState.GRANTED ||
                permissionsState.fineLocationPermission == PermissionState.GRANTED
}