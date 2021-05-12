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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is the Android-specific implementation of [HasLocationFeatureDetector] and
 * [IsLocationEnabledFetcher].
 *
 * @param context The application [Context].
 * @param packageManager The Android [PackageManager].
 * @param isLocationEnabledFetcher An implementation which retrieves the location enabled setting
 * depending on the platform version.
 * @author Niall Scott
 */
@Singleton
internal class AndroidLocationSupport @Inject constructor(
        private val context: Context,
        private val packageManager: PackageManager,
        private val isLocationEnabledFetcher: IsLocationEnabledFetcher)
    : HasLocationFeatureDetector, IsLocationEnabledDetector, DistanceCalculator {

    override fun hasLocationFeature() =
            packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)

    @ExperimentalCoroutinesApi
    override fun getIsLocationEnabledFlow(): Flow<Boolean> = callbackFlow {
        val locationEnabledReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                launch {
                    getAndSendIsLocationEnabled(channel)
                }
            }
        }

        context.registerReceiver(locationEnabledReceiver,
                IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        getAndSendIsLocationEnabled(channel)

        awaitClose {
            context.unregisterReceiver(locationEnabledReceiver)
        }
    }

    override fun distanceBetween(first: DeviceLocation, second: DeviceLocation): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
                first.latitude,
                first.longitude,
                second.latitude,
                second.longitude,
                results)

        return results[0]
    }

    /**
     * Get the current location services enabled state and set the state on the given [channel].
     *
     * @param channel The [SendChannel] to send the enabled state to.
     */
    private suspend fun getAndSendIsLocationEnabled(channel: SendChannel<Boolean>) {
        channel.send(isLocationEnabledFetcher.isLocationEnabled())
    }
}