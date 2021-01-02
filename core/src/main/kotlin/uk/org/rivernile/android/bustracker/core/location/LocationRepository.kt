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
 * @author Niall Scott
 */
@Singleton
class LocationRepository @Inject internal constructor(
        private val hasLocationFeatureDetector: HasLocationFeatureDetector,
        private val isLocationEnabledDetector: IsLocationEnabledDetector) {

    /**
     * Does this device have location-aware features or not?
     */
    val hasLocationFeature by lazy { hasLocationFeatureDetector.hasLocationFeature() }

    /**
     * Get a [Flow] which returns the location enabled status. Any updates to the status will be
     * emitted from the returned [Flow] until cancelled.
     *
     * @return The [Flow] which emits location enabled status.
     */
    @ExperimentalCoroutinesApi
    fun getIsLocationEnabledFlow() = isLocationEnabledDetector.getIsLocationEnabledFlow()
}