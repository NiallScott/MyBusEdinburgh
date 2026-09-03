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

package uk.org.rivernile.android.bustracker.core.location

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Android-specific implementation of [LocationRepository].
 *
 * @param androidLocationSupport Used as a proxy to the real Android APIs - this exists so the
 * repository can be tested in unit tests.
 * @param androidLocationPermissionChecker Used to check location permissions.
 * @param locationSource The location source - an abstraction because location can come from
 * multiple sources.
 * @author Niall Scott
 */
@Singleton
internal class AndroidLocationRepository @Inject constructor(
    private val androidLocationSupport: AndroidLocationSupport,
    private val androidLocationPermissionChecker: AndroidLocationPermissionChecker,
    private val locationSource: LocationSource,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope
) : LocationRepository {

    override val hasLocationFeature by lazy {
        androidLocationSupport.hasLocationFeature
    }

    override val hasGpsLocationProvider by lazy {
        androidLocationSupport.hasGpsLocationProvider
    }

    override val isLocationEnabledFlow = androidLocationSupport
        .isLocationEnabledFlow
        .shareIn(
            scope = applicationCoroutineScope,
            started = SharingStarted.WhileSubscribed(
                replayExpirationMillis = 0L
            ),
            replay = 1
        )

    override val isGpsLocationProviderEnabledFlow = androidLocationSupport
        .isGpsLocationProviderEnabledFlow
        .shareIn(
            scope = applicationCoroutineScope,
            started = SharingStarted.WhileSubscribed(
                replayExpirationMillis = 0L
            ),
            replay = 1
        )

    override val locationUpdatesFlow: Flow<LocationUpdate> get() {
        return if (hasLocationFeature) {
            getLocationUpdatesFlowWhenHasLocationFeature()
        } else {
            flowOf(LocationUpdate.Error.NoLocationFeature)
        }
    }

    override fun distanceBetween(first: LatLon, second: LatLon) =
        androidLocationSupport.distanceBetween(first, second)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getLocationUpdatesFlowWhenHasLocationFeature(): Flow<LocationUpdate> {
        val hasSufficientLocationPermission = androidLocationPermissionChecker
            .checkHasEitherFineOrCoarseLocationPermission()

        return if (hasSufficientLocationPermission) {
            isLocationEnabledFlow
                .distinctUntilChanged()
                .flatMapLatest(::getLocationsFlowWhenPermissionsIsSufficient)
        } else {
            flowOf(LocationUpdate.Error.InsufficientLocationPermissions)
        }
    }

    private fun getLocationsFlowWhenPermissionsIsSufficient(
        isLocationEnabled: Boolean
    ): Flow<LocationUpdate> {
        return if (isLocationEnabled) {
            locationSource.locationUpdatesFlow
        } else {
            flowOf(LocationUpdate.Error.LocationOff)
        }
    }
}
