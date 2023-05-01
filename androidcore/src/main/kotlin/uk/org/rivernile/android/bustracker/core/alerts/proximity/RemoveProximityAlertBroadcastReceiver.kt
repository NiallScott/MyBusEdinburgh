/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import javax.inject.Inject

/**
 * This [BroadcastReceiver] is called when the user has tapped 'Remove' on the proximity alert
 * notification.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class RemoveProximityAlertBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alertsRepository: AlertsRepository
    @Inject
    @ForApplicationCoroutineScope
    lateinit var applicationCoroutineScope: CoroutineScope
    @Inject
    @ForDefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        applicationCoroutineScope.launch(defaultDispatcher) {
            alertsRepository.removeAllProximityAlerts()
        }
    }
}