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

package uk.org.rivernile.android.bustracker.core.location.platform

import android.Manifest
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationSource
import uk.org.rivernile.android.bustracker.core.permission.PermissionChecker
import javax.inject.Inject

/**
 * This is the Android platform implementation of [LocationSource]. This is used as the fallback
 * when the preferred implementation is not available. It obtains locations directly from
 * [LocationManager] rather than via another source.
 *
 * @param locationManager The Android platform [LocationManager] where locations are obtained from.
 * @param permissionChecker Used to check the permission status.
 * @author Niall Scott
 */
internal class PlatformLocationSource @Inject constructor(
        private val locationManager: LocationManager,
        private val permissionChecker: PermissionChecker) : LocationSource {

    companion object {

        private const val LOCATION_DWELL_TIME = 120000L

        private const val USER_VISIBLE_LOCATION_MIN_TIME_MILLIS = 5000L
        private const val USER_VISIBLE_LOCATION_MIN_DISTANCE_METERS = 10f
    }

    @ExperimentalCoroutinesApi
    override val userVisibleLocationFlow get() = if (permissionChecker.checkLocationPermission()) {
        callbackFlow<Location> {
            // Before registering for location updates, immediately obtain the last location from
            // the OS and send it to the channel. This may be null if there is no previous location.
            getBestInitialLocation()?.let {
                channel.send(it)
            }

            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    launch {
                        channel.send(location)
                    }
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
                    // This is deprecated in Android Q and above. But we don't need anything from
                    // this callback anyway.
                }

                override fun onProviderEnabled(provider: String) {
                    // We don't need to concern ourselves about this, as the listener is registered
                    // against each provider, irrespective of its enabled state. Android will
                    // deliver new locations for the provider in onLocationChanged() when it's
                    // enabled, and won't when it's disabled. We just register the listener against
                    // each provider we're interested in and Android does the rest, until we later
                    // unregister.
                }

                override fun onProviderDisabled(provider: String) {
                    // See comment in onProviderEnabled() - it applies here too.
                }
            }

            // Android S introduces LocationManager.hasProvider(), but for backwards compatibility,
            // we need to loop through all providers to determine existence.
            locationManager.allProviders.forEach {
                if (it == LocationManager.NETWORK_PROVIDER || it == LocationManager.GPS_PROVIDER) {
                    // Request location updates from both network and GPS providers. We'll only get
                    // location updates from active providers, but we can just ignored their active
                    // state as the OS will deal with that for us.
                    //
                    // Location updates will be delivered on the main thread, but this is fine as
                    // a coroutine is immediately launched from there, thus not blocking the thread.
                    locationManager.requestLocationUpdates(
                            it,
                            USER_VISIBLE_LOCATION_MIN_TIME_MILLIS,
                            USER_VISIBLE_LOCATION_MIN_DISTANCE_METERS,
                            locationListener,
                            Looper.getMainLooper())
                }
            }

            awaitClose {
                locationManager.removeUpdates(locationListener)
            }
        }.scan<Location, Location?>(null) { accumulator, value ->
            value.takeIf { isBetterLocation(it, accumulator) } ?: accumulator
        }.mapNotNull { location ->
            location?.let {
                DeviceLocation(it.latitude, it.longitude)
            }
        }.distinctUntilChanged() // Prevent unnecessary downstream processing.
    } else {
        emptyFlow()
    }

    /**
     * Get the best initial fix on a [Location].
     *
     * This will loop through all known system location providers and get a location from each
     * provider. They will all be compared to return the best location.
     *
     * @return A [Location] which contains the best initial location, or `null` if an initial
     * location could not be determined.
     */
    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    private fun getBestInitialLocation(): Location? {
        return locationManager.allProviders.fold<String, Location?>(null) { best, provider ->
            locationManager.getLastKnownLocation(provider)
                    ?.takeIf { isBetterLocation(it, best) } ?: best
        }
    }

    /**
     * Determines whether [newLocation] is a better [Location] fix than the current detected
     * [currentBestLocation] fix.
     *
     * The algorithm in this method was taken from an old article on the Android Developer site
     * which no longer seems to exist.
     *
     * @param newLocation The new [Location] fix.
     * @param currentBestLocation The currently held [Location] fix.
     * @return `true` if [newLocation] is a better location fix than [currentBestLocation],
     * otherwise `false`.
     */
    private fun isBetterLocation(newLocation: Location, currentBestLocation: Location?): Boolean {
        // A new location is always better than no location.
        val currentBest = currentBestLocation ?: return true

        val timeDelta = newLocation.time - currentBest.time

        if (timeDelta > LOCATION_DWELL_TIME) {
            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved.
            return true
        } else if (timeDelta < -LOCATION_DWELL_TIME) {
            // If the new location is more than two minutes older, it must be worse.
            return false
        }

        val accuracyDelta = newLocation.accuracy - currentBest.accuracy
        val isNewer = timeDelta > 0L
        val isLessAccurate = accuracyDelta > 0f
        val isSignificantlyLessAccurate = accuracyDelta > 200f

        // Determine location quality using a combination of timeliness and accuracy.
        return accuracyDelta < 0f ||
                (isNewer && !isLessAccurate) ||
                (isNewer && !isSignificantlyLessAccurate &&
                        newLocation.provider == currentBest.provider)
    }
}