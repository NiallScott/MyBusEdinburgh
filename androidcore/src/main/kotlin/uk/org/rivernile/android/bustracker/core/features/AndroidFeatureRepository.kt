/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.features

import android.content.pm.PackageManager
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Android-specific implementation of [FeatureRepository].
 *
 * @param stopMapFeatureAvailabilityProvider An implementation which tells us if the stop map
 * feature is available.
 * @param locationRepository Used to obtain location service information.
 * @param packageManager Used to query the package manager for device features.
 * @author Niall Scott
 */
@Singleton
internal class AndroidFeatureRepository @Inject constructor(
        private val stopMapFeatureAvailabilityProvider: StopMapFeatureAvailabilityProvider,
        private val locationRepository: LocationRepository,
        private val packageManager: PackageManager) : FeatureRepository {

    override val hasStopMapUiFeature get() =
        stopMapFeatureAvailabilityProvider.isStopMapFeatureAvailable()

    override val hasArrivalAlertFeature get() = true

    override val hasProximityAlertFeature get() =
        locationRepository.hasLocationFeature

    override val hasCameraFeature get() =
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}