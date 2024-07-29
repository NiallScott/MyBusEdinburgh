/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.proximity.googleplay

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import uk.org.rivernile.android.bustracker.core.alerts.proximity.GeofencingManager
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.permission.AndroidPermissionChecker
import javax.inject.Inject

private const val PENDING_INTENT_FLAGS =
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE

/**
 * An implementation of [GeofencingManager] where the underlying implementation uses the Google
 * Play Services Location library [GeofencingClient] to monitor geofences.
 *
 * @param context The [android.app.Application] [Context].
 * @param geofencingClient The Google Play Services [GeofencingClient].
 * @param permissionChecker Used to check permission grants.
 * @param exceptionLogger Used to log errors.
 * @author Niall Scott
 */
internal class GooglePlayGeofencingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient,
    private val permissionChecker: AndroidPermissionChecker,
    private val exceptionLogger: ExceptionLogger
) : GeofencingManager {

    override fun addGeofence(
        id: Int,
        latitude: Double,
        longitude: Double,
        radius: Float,
        duration: Long
    ) {
        if (checkPermission()) {
            val geofence = Geofence.Builder()
                .setRequestId(id.toString())
                .setCircularRegion(latitude, longitude, radius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(duration)
                .build()

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient
                .addGeofences(request, createPendingIntent(id))
                .addOnFailureListener {
                    exceptionLogger.log(it)
                }
        }
    }

    override fun removeGeofence(id: Int) {
        if (checkPermission()) {
            geofencingClient.removeGeofences(listOf(id.toString()))
        }

        getPendingIntentOrNull(id)?.cancel()
    }

    /**
     * Check that we have the required permissions to add a geofence with [GeofencingClient].
     *
     * @return `true` if we have the required permissions, otherwise `false`.
     */
    private fun checkPermission(): Boolean  {
        return permissionChecker.checkFineLocationPermission() &&
                permissionChecker.checkBackgroundLocationPermission()
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
            areaEnteredIntent,
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
            areaEnteredIntent,
            PENDING_INTENT_FLAGS or PendingIntent.FLAG_NO_CREATE
        )

    /**
     * This is the [Intent] which is sent to [GooglePlayAreaEnteredBroadcastReceiver] when the
     * proximity alert geofence has been entered.
     */
    private val areaEnteredIntent get() =
        Intent(context, GooglePlayAreaEnteredBroadcastReceiver::class.java)
}