/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import uk.org.rivernile.android.bustracker.core.alerts.proximity.GeofencingManager
import uk.org.rivernile.android.bustracker.core.permission.AndroidPermissionChecker
import javax.inject.Inject

private const val PENDING_INTENT_FLAGS =
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE

/**
 * The base implementation of [GeofencingManager] which uses the AOSP [LocationManager] for
 * geofencing.
 *
 * @param context The application [Context].
 * @param locationManager The Android [LocationManager].
 * @param permissionChecker Used to check granted permissions.
 * @author Niall Scott
 */
internal class AndroidGeofencingManager @Inject constructor(
    private val context: Context,
    private val locationManager: LocationManager,
    private val permissionChecker: AndroidPermissionChecker
) : GeofencingManager {

    override fun addGeofence(
        id: Int,
        latitude: Double,
        longitude: Double,
        radius: Float,
        duration: Long
    ) {
        if (permissionChecker.checkFineLocationPermission()) {
            locationManager.addProximityAlert(
                latitude,
                longitude,
                radius,
                duration,
                createPendingIntent(id)
            )
        }
    }

    override fun removeGeofence(id: Int) {
        val pendingIntent = getPendingIntentOrNull(id) ?: return

        if (permissionChecker.checkFineLocationPermission()) {
            locationManager.removeProximityAlert(pendingIntent)
        }

        pendingIntent.cancel()
    }

    /**
     * Create a [PendingIntent] which describes what component to invoke when a proximity geofence
     * has been entered.
     *
     * @param alertId The ID of the alert.
     * @return The newly created (or updated) [PendingIntent].
     */
    private fun createPendingIntent(alertId: Int) =
        PendingIntent.getBroadcast(
            context,
            alertId,
            createIntent(alertId),
            PENDING_INTENT_FLAGS
        )

    /**
     * Get an existing [PendingIntent] which is fired when a proximity geofence has been entered.
     *
     * @param alertId The ID of the alert.
     * @return The existing [PendingIntent], or `null` if it does not exist.
     */
    private fun getPendingIntentOrNull(alertId: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            alertId,
            createIntent(alertId),
            PENDING_INTENT_FLAGS or PendingIntent.FLAG_NO_CREATE
        )

    /**
     * Create an [Intent] which is used by the [PendingIntent] to send when the proximity area has
     * been entered.
     *
     * @param alertId The ID of the alert.
     * @return The [Intent] used when the proximity area has been entered.
     */
    private fun createIntent(alertId: Int) =
        Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
            .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, alertId)
}