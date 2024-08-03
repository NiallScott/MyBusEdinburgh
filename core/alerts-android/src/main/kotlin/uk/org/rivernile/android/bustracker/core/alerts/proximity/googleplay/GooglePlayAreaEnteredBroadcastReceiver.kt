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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.proximity.AreaEnteredHandler
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject

/**
 * This is a [BroadcastReceiver] for receiving proximity events when using the Google Play Services
 * proximity alerting mechanism through [com.google.android.gms.location.GeofencingClient].
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class GooglePlayAreaEnteredBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var areaEnteredHandler: AreaEnteredHandler
    @Inject
    lateinit var exceptionLogger: ExceptionLogger
    @Inject
    @ForApplicationCoroutineScope
    lateinit var applicationCoroutineScope: CoroutineScope
    @Inject
    @ForDefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        GeofencingEvent
            .fromIntent(intent)
            ?.let(::handleGeofencingEvent)
    }

    /**
     * Handle the received [GeofencingEvent].
     *
     * @param event The received [GeofencingEvent].
     */
    private fun handleGeofencingEvent(event: GeofencingEvent) {
        if (event.hasError()) {
            exceptionLogger.log(
                RuntimeException(
                    "Error while handling GeofencingEvent: " +
                            GeofenceStatusCodes.getStatusCodeString(event.errorCode)
                )
            )

            return
        }

        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = event
                .triggeringGeofences
                ?.takeIf { it.isNotEmpty() }
                ?: return

            val pendingResult = goAsync()

            applicationCoroutineScope.launch(defaultDispatcher) {
                try {
                    triggeringGeofences.forEach { geofence ->
                        geofence
                            .requestId
                            .toIntOrNull()
                            ?.let {
                                areaEnteredHandler.handleAreaEntered(it)
                            }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}