/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.location

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access any device location properties, for the purpose of providing
 * location-aware functionality.
 *
 * @param hasLocationFeatureDetector Used to determine if the device is capable of location-aware
 * functionality.
 * @param isLocationEnabledDetector Used to determine if location features are currently enabled on
 * the device.
 * @param locationSource An implementation used to provide the device location.
 * @author Niall Scott
 */
@Singleton
class LocationRepository @Inject internal constructor(
        private val hasLocationFeatureDetector: HasLocationFeatureDetector,
        private val isLocationEnabledDetector: IsLocationEnabledDetector,
        private val locationSource: LocationSource,
        private val distanceCalculator: DistanceCalculator) {

    /**
     * Does this device have location-aware features or not?
     */
    val hasLocationFeature by lazy { hasLocationFeatureDetector.hasLocationFeature() }

    /**
     * Get a [Flow] which returns the location enabled status. Any updates to the status will be
     * emitted from the returned [Flow] until cancelled.
     */
    @ExperimentalCoroutinesApi
    val isLocationEnabledFlow get() = isLocationEnabledDetector.getIsLocationEnabledFlow()

    /**
     * Get a [Flow] which emits the latest [DeviceLocation] and any further location changes until
     * cancelled.
     *
     * Callers should check [hasLocationFeature] first before requesting this [Flow]. When
     * [hasLocationFeature] is `false`, the [Flow] returned by this property will only ever emit
     * `null`.
     *
     * Callers should also check to see if location permissions have been granted. If they have not
     * been, this property will return a [Flow] which only emits `null`.
     *
     * A [Flow] emitting `null` will also be returned when device location services are not enabled.
     */
    @ExperimentalCoroutinesApi
    val userVisibleLocationFlow: Flow<DeviceLocation?> get() = if (hasLocationFeature) {
        isLocationEnabledFlow
                .distinctUntilChanged()
                .flatMapLatest(this::createUserVisibleLocationFlow)
    } else {
        // The location feature detection is a hard no in this case. We don't expect it to change,
        // so we just emit a Flow of `null`.
        flowOf(null)
    }

    /**
     * Get the distance, in meters, between [first] and [second].
     *
     * @param first The first location coordinate.
     * @param second The second location coordinate.
     * @return The number of meters between the two coordinates. A negative value implies the
     * distance could not be calculated.
     */
    fun distanceBetween(first: DeviceLocation, second: DeviceLocation): Float =
            distanceCalculator.distanceBetween(first, second)

    /**
     * Create a [Flow] which emits [DeviceLocation] objects for user visible features. This is
     * dependent upon whether device location services are enabled or not. If they are not enabled,
     * a [Flow] which merely emits `null` will be returned.
     *
     * @param locationEnabled Are device location services enabled or not?
     * @return A [Flow] with the latest [DeviceLocation] if available, or `null` if not available.
     */
    private fun createUserVisibleLocationFlow(locationEnabled: Boolean) = if (locationEnabled) {
        locationSource.userVisibleLocationFlow
    } else {
        flowOf(null)
    }
}