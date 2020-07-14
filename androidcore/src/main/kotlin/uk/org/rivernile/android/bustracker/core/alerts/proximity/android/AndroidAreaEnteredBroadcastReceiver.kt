/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import dagger.android.AndroidInjection
import uk.org.rivernile.android.bustracker.core.alerts.proximity.AreaEnteredHandler
import javax.inject.Inject

/**
 * This is a [BroadcastReceiver] for receiving proximity events when using the platform's proximity
 * alerting mechanism through [LocationManager].
 *
 * @author Niall Scott
 */
class AndroidAreaEnteredBroadcastReceiver : BroadcastReceiver() {

    companion object {

        /**
         * Add this extra on to the fired [Intent] to specify what alert was triggered.
         */
        internal const val EXTRA_ALERT_ID = "alertId"
    }

    @Inject
    lateinit var areaEnteredHandler: AreaEnteredHandler

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        handleIntent(intent)
    }

    /**
     * Handle the received [Intent].
     *
     * @param intent The received [Intent].
     */
    private fun handleIntent(intent: Intent) {
        val alertId = intent.getIntExtra(EXTRA_ALERT_ID, -1)

        if (alertId >= 0) {
            val isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)

            if (isEntering) {
                areaEnteredHandler.handleAreaEntered(alertId)
            }
        }
    }
}