/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.config.ConfigRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import javax.inject.Inject
import kotlin.math.absoluteValue

/**
 * A class used to get the latest [UiState]. It combines a number of factors to derive the current
 * [UiState] which are emitted as a [Flow].
 *
 * @param locationRepository Used to obtain location details.
 * @param busStopsRepository Used to obtain the stop data.
 * @param configRepository Used to obtain app-specific configuration - namely, the lat/lon bounds
 * for nearest stops.
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class UiStateRetriever @Inject constructor(
        private val locationRepository: LocationRepository,
        private val busStopsRepository: BusStopsRepository,
        private val configRepository: ConfigRepository) {

    /**
     * Return a [Flow] which emits [UiState]s. This is based upon the state of the provided
     * [permissionStateFlow], [serviceFilterFlow] and other factors.
     *
     * @param permissionStateFlow A [Flow] which represents the latest [PermissionsState].
     * @param serviceFilterFlow A [Flow] which represents the user's service filter.
     * @return A [Flow] which emits [UiState]s.
     */
    fun getUiStateFlow(
            permissionStateFlow: Flow<PermissionsState>,
            serviceFilterFlow: Flow<List<String>?>): Flow<UiState> {
        return if (locationRepository.hasLocationFeature) {
            permissionStateFlow
                    .map(this::isPermissionsSufficient)
                    .flatMapLatest { handlePermissionsStateChanged(it, serviceFilterFlow) }
        } else {
            flowOf(UiState.Error.NoLocationFeature)
        }
    }

    /**
     * Handle the state of the permissions changing.
     *
     * When the permissions are insufficient, the [Flow] will emit a
     * [UiState.Error.InsufficientLocationPermissions].
     *
     * @param isPermissionsSufficient Are the granted permissions sufficient for obtaining a device
     * location?
     * @param serviceFilterFlow A [Flow] which emits the user's selected services filter.
     * @return A [Flow] of [UiState].
     */
    private fun handlePermissionsStateChanged(
            isPermissionsSufficient: Boolean,
            serviceFilterFlow: Flow<List<String>?>): Flow<UiState> {
        return if (isPermissionsSufficient) {
            locationRepository.isLocationEnabledFlow
                    .distinctUntilChanged()
                    .flatMapLatest { handleLocationEnabled(it, serviceFilterFlow) }
        } else {
            flowOf(UiState.Error.InsufficientLocationPermissions)
        }
    }

    /**
     * Handle the location enabled state changing.
     *
     * When location is not enabled, this method will return a [Flow] that emits a single
     * [UiState.Error.LocationOff]. Otherwise, another [Flow] of [UiState] will be returned.
     *
     * @param isEnabled Is location enabled?
     * @param serviceFilterFlow A [Flow] which emits the user's selected services filter.
     * @return A [Flow] of [UiState].
     */
    private fun handleLocationEnabled(
            isEnabled: Boolean,
            serviceFilterFlow: Flow<List<String>?>): Flow<UiState> {
        return if (isEnabled) {
            locationRepository.userVisibleLocationFlow
                    .flatMapLatest { loadNearestStops(it, serviceFilterFlow) }
                    .onStart { emit(UiState.Error.LocationUnknown) }
                    .distinctUntilChanged()
        } else {
            flowOf(UiState.Error.LocationOff)
        }
    }

    /**
     * Given a determined [DeviceLocation] and a [serviceFilterFlow], load the nearest stops
     * listing.
     *
     * @param location The determined [DeviceLocation].
     * @param serviceFilterFlow A [Flow] which emits [List]s of [String], with each string being a
     * service name. This is the service filter used to filter results.
     * @return A [Flow] which emits [UiState]s.
     */
    private fun loadNearestStops(
            location: DeviceLocation,
            serviceFilterFlow: Flow<List<String>?>): Flow<UiState> {
        return serviceFilterFlow
                .flatMapLatest { loadNearestStops(location, it) }
    }

    /**
     * Given a determined [DeviceLocation] and an optional [serviceFilter], load the listing of
     * nearest stops from the stop database.
     *
     * @param location The determined [DeviceLocation].
     * @param serviceFilter A [List] of service names of service to include in the results. Any
     * stops which don't include these services won't be included. A `null` or empty [List] means no
     * filter is applied.
     * @return A [Flow] which emits [UiState]s.
     */
    private fun loadNearestStops(
            location: DeviceLocation,
            serviceFilter: List<String>?): Flow<UiState> {
        val latitudeSpan = configRepository.nearestStopsLatitudeSpan
        val longitudeSpan = configRepository.nearestStopsLongitudeSpan

        val minLatitude = location.latitude - latitudeSpan
        val maxLatitude = location.latitude + latitudeSpan
        val minLongitude = location.longitude - longitudeSpan
        val maxLongitude = location.longitude + longitudeSpan

        return busStopsRepository.getStopDetailsWithinSpanFlow(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude,
                serviceFilter)
                .map { mapToNearestStops(it, location) }
    }

    /**
     * Given a [List] of [StopDetailsWithServices] objects, and a [DeviceLocation], combine these
     * together. When there are stops available, a [UiState.Success] is returned, otherwise (when
     * [stops] is `null` or empty), return a [UiState.Error.NoNearestStops].
     *
     * When [UiState.Success] is returned, the resulting stops are ordered by distance ascending.
     *
     * @param stops The [List] of [StopDetailsWithServices] which has been loaded from the stop
     * database.
     * @param location The determined [DeviceLocation].
     * @return [UiState.Success] when there are stops, otherwise [UiState.Error.NoNearestStops].
     */
    private fun mapToNearestStops(
            stops: List<StopDetailsWithServices>?,
            location: DeviceLocation): UiState {
        return stops?.ifEmpty { null }?.let { stopsNonNull ->
            stopsNonNull.map { mapToNearestStop(it, location) }
                    .sortedBy { it.distance }
                    .let { UiState.Success(it) }
        } ?: UiState.Error.NoNearestStops
    }

    /**
     * Given a [stop] and a [location], combine these in to a [UiNearestStop].
     *
     * @param stop The stop details.
     * @param location The determined device location.
     * @return The resulting [UiNearestStop] of combining these together.
     */
    private fun mapToNearestStop(
            stop: StopDetailsWithServices,
            location: DeviceLocation): UiNearestStop {
        val stopLocation = DeviceLocation(stop.latitude, stop.longitude)
        val distanceBetween = locationRepository.distanceBetween(stopLocation, location)

        return UiNearestStop(
                stop.stopCode,
                stop.stopName,
                stop.serviceListing,
                distanceBetween.absoluteValue.toInt(),
                stop.orientation)
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