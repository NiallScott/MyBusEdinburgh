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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import javax.inject.Inject
import kotlin.math.absoluteValue

/**
 * @author Niall Scott
 */
internal interface AndroidLocationSupport {

    /**
     * Does this device have location-aware features or not?
     */
    val hasLocationFeature: Boolean

    /**
     * Does this device have a GPS location provider?
     */
    val hasGpsLocationProvider: Boolean

    /**
     * Get a [Flow] which returns the location enabled status. Any updates to the status will be
     * emitted from the returned [Flow] until cancelled.
     */
    val isLocationEnabledFlow: Flow<Boolean>

    /**
     * A [Flow] which emits whether the GPS provider is enabled or not.
     */
    val isGpsLocationProviderEnabledFlow: Flow<Boolean>

    /**
     * Get the distance, in meters, between [first] and [second].
     *
     * @param first The first location coordinate.
     * @param second The second location coordinate.
     * @return The number of meters between the two coordinates. A negative value implies the
     * distance could not be calculated.
     */
    fun distanceBetween(first: LatLon, second: LatLon): Float
}

internal class RealAndroidLocationSupport @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManager: PackageManager,
    private val locationManager: LocationManager,
    private val locationSource: LocationSource,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope
) : AndroidLocationSupport {

    override val hasLocationFeature get() =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)

    override val hasGpsLocationProvider get() =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    override val isLocationEnabledFlow get() = callbackFlow {
        val locationEnabledReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val pendingResult = goAsync()

                launch {
                    try {
                        getAndSendIsLocationEnabled()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }

        context.registerReceiver(
            locationEnabledReceiver,
            IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        )
        getAndSendIsLocationEnabled()

        awaitClose {
            context.unregisterReceiver(locationEnabledReceiver)
        }
    }

    override val isGpsLocationProviderEnabledFlow get() = callbackFlow {
        val providerEnabledReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val pendingResult = goAsync()

                launch {
                    try {
                        getAndSendIsGpsProviderEnabled()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }

        context.registerReceiver(
            providerEnabledReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
        getAndSendIsGpsProviderEnabled()

        awaitClose {
            context.unregisterReceiver(providerEnabledReceiver)
        }
    }

    override fun distanceBetween(first: LatLon, second: LatLon): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            first.latitude,
            first.longitude,
            second.latitude,
            second.longitude,
            results
        )

        return results[0].absoluteValue
    }

    private suspend fun ProducerScope<Boolean>.getAndSendIsLocationEnabled() {
        send(locationManager.isLocationEnabled)
    }

    private suspend fun ProducerScope<Boolean>.getAndSendIsGpsProviderEnabled() {
        send(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }
}
