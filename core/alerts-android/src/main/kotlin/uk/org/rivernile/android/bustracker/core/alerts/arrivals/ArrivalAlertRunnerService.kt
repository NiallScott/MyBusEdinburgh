/*
 * Copyright (C) 2019 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.arrivals

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForServiceCoroutineScope
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * This is an Android [Service] which runs the arrival alert checker.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class ArrivalAlertRunnerService : Service() {

    companion object {

        private const val FOREGROUND_NOTIFICATION_ID = 100
    }

    @Inject
    internal lateinit var timeAlertRunner: TimeAlertRunner
    @Inject
    internal lateinit var notificationFactory: ArrivalServiceNotificationFactory
    @Inject
    internal lateinit var unableToRunArrivalAlertsHandler: Lazy<UnableToRunArrivalAlertsHandler>
    @Inject
    @ForServiceCoroutineScope
    internal lateinit var serviceCoroutineScope: CoroutineScope
    @Inject
    @ForDefaultDispatcher
    internal lateinit var defaultDispatcher: CoroutineDispatcher

    private val isStarted = AtomicBoolean(false)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isStarted.compareAndSet(false, true)) {
            serviceCoroutineScope.launch {
                try {
                    runService()
                } finally {
                    // This will usually be because a CancellationException has been thrown
                    // upstream.
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        unableToRunArrivalAlertsHandler.get().handleUnableToRunArrivalAlerts()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceCoroutineScope.cancel()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private suspend fun runService() {
        try {
            startForeground()
        } catch (_: IllegalStateException) {
            unableToRunArrivalAlertsHandler.get().handleUnableToRunArrivalAlerts()
            return
        }

        withContext(defaultDispatcher) {
            timeAlertRunner.run()
        }
    }

    private fun startForeground() {
        startForeground(
            FOREGROUND_NOTIFICATION_ID,
            notificationFactory.createForegroundNotification(this)
        )
    }
}
