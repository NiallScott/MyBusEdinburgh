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

package uk.org.rivernile.android.bustracker.core.alerts.proximity.android

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import uk.org.rivernile.android.bustracker.core.alerts.proximity.GeofencingManager
import uk.org.rivernile.android.bustracker.core.permission.AndroidPermissionChecker
import javax.inject.Inject

/**
 * The base implementation of [GeofencingManager] which uses the AOSP [LocationManager] for
 * geofencing.
 *
 * @param context The application [Context].
 * @param locationManager The Android [LocationManager].
 * @param permissionChecker Used to check granted permissions.
 * @author Niall Scott
 */
class AndroidGeofencingManager @Inject constructor(
        private val context: Context,
        private val locationManager: LocationManager,
        private val permissionChecker: AndroidPermissionChecker) : GeofencingManager {

    override fun addGeofence(
            id: Int,
            latitude: Double,
            longitude: Double,
            radius: Float,
            duration: Long) {
        if (permissionChecker.checkFineLocationPermission()) {
            createPendingIntent(id).let {
                locationManager.addProximityAlert(latitude, longitude, radius, duration, it)
            }
        }
    }

    override fun removeGeofence(id: Int) {
        if (permissionChecker.checkFineLocationPermission()) {
            createPendingIntent(id).let {
                locationManager.removeProximityAlert(it)
                it.cancel()
            }
        }
    }

    /**
     * Create a [PendingIntent] which describes what component to invoke when a proximity has been
     * entered.
     *
     * @param alertId The ID of the alert.
     */
    @SuppressLint("InlinedApi")
    private fun createPendingIntent(alertId: Int) =
            Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                    .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, alertId)
                    .let {
                        PendingIntent.getBroadcast(
                                context,
                                alertId,
                                it,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                    }
}