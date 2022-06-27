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

package uk.org.rivernile.android.bustracker.core.location.googleplay

import android.Manifest
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationSource
import uk.org.rivernile.android.bustracker.core.permission.PermissionChecker
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This is the Google Play Location implementation of [LocationSource]. This is the preferred
 * location source on devices which are capable of Google Play Services.
 *
 * @param fusedLocationProviderClient The [FusedLocationProviderClient] from Google Play Services.
 * @param permissionChecker Used to check the permission status.
 * @author Niall Scott
 */
internal class GooglePlayLocationSource @Inject constructor(
        private val fusedLocationProviderClient: FusedLocationProviderClient,
        private val permissionChecker: PermissionChecker) : LocationSource {

    companion object {

        private const val LAST_LOCATION_TIMEOUT_MILLIS = 2000L
        private const val USER_VISIBLE_LOCATION_INTERVAL_MILLIS = 5000L
        private const val USER_VISIBLE_LOCATION_FASTEST_INTERVAL_MILLIS = 2000L
    }

    private val userVisibleLocationRequest by lazy {
        LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(USER_VISIBLE_LOCATION_INTERVAL_MILLIS)
                .setFastestInterval(USER_VISIBLE_LOCATION_FASTEST_INTERVAL_MILLIS)
    }

    @ExperimentalCoroutinesApi
    override val userVisibleLocationFlow get() = if (permissionChecker.checkLocationPermission()) {
        callbackFlow {
            getLastLocation()?.let {
                channel.send(it)
            }

            // As getLastLocation() is not cancellable and can be long running, make sure we're
            // still active here.
            if (isActive) {
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        launch {
                            mapToDeviceLocation(result.lastLocation)?.let {
                                channel.send(it)
                            }
                        }
                    }
                }

                fusedLocationProviderClient.requestLocationUpdates(
                        userVisibleLocationRequest,
                        callback,
                        Looper.getMainLooper())

                awaitClose {
                    fusedLocationProviderClient.removeLocationUpdates(callback)
                }
            }
        }.distinctUntilChanged() // Prevent unnecessary downstream processing.
    } else {
        emptyFlow()
    }

    /**
     * Get the last location held by this device.
     *
     * @return The last [DeviceLocation] held by this device, or `null` if there is none.
     */
    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    private suspend fun getLastLocation() = withTimeoutOrNull(LAST_LOCATION_TIMEOUT_MILLIS) {
        suspendCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                val result = if (it.isSuccessful) {
                    it.result
                } else {
                    null
                }

                continuation.resume(mapToDeviceLocation(result))
            }
        }
    }

    /**
     * Given a [Location], map this to a [DeviceLocation]. If the [Location] is `null`, then this
     * method will return `null`.
     *
     * @param location The [Location] to map.
     * @return The mapped location, or `null` if the input was `null`.
     */
    private fun mapToDeviceLocation(location: Location?) = location?.let {
        DeviceLocation(it.latitude, it.longitude)
    }
}