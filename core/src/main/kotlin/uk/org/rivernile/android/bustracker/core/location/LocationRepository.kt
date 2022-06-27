/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
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
 * @param distanceCalculator Used to calculate the distance between two lat/lon points.
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
    val hasLocationFeature by lazy { hasLocationFeatureDetector.hasLocationFeature }

    /**
     * Does this device have a GPS location provider?
     */
    val hasGpsLocationProvider by lazy { hasLocationFeatureDetector.hasGpsLocationProvider }

    /**
     * Get a [Flow] which returns the location enabled status. Any updates to the status will be
     * emitted from the returned [Flow] until cancelled.
     */
    @ExperimentalCoroutinesApi
    val isLocationEnabledFlow get() = isLocationEnabledDetector.isLocationEnabledFlow

    /**
     * Is the GPS location provider enabled?
     */
    val isGpsLocationProviderEnabled get() = isLocationEnabledDetector.isGpsLocationProviderEnabled

    /**
     * Get a [Flow] which emits the latest [DeviceLocation] and any further location changes until
     * cancelled.
     *
     * Callers should check [hasLocationFeature] first before requesting this [Flow]. When
     * [hasLocationFeature] is `false`, the [Flow] returned by this property will only be an empty
     * [Flow].
     *
     * Callers should also check to see if location permissions have been granted. If they have not
     * been, this property will return an empty [Flow].
     *
     * A empty [Flow] will also be returned when device location services are not enabled.
     */
    @ExperimentalCoroutinesApi
    val userVisibleLocationFlow: Flow<DeviceLocation> get() = if (hasLocationFeature) {
        isLocationEnabledFlow
                .distinctUntilChanged()
                .flatMapLatest(this::createUserVisibleLocationFlow)
    } else {
        // The location feature detection is a hard no in this case. Return an empty Flow.
        emptyFlow()
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
     * an empty [Flow] will be returned.
     *
     * @param locationEnabled Are device location services enabled or not?
     * @return A [Flow] with the latest [DeviceLocation] if available, or an empty [Flow] if not
     * available.
     */
    @ExperimentalCoroutinesApi
    private fun createUserVisibleLocationFlow(locationEnabled: Boolean) = if (locationEnabled) {
        locationSource.userVisibleLocationFlow
    } else {
        emptyFlow()
    }
}