/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
 * This is an Android [Service] which runs the proximity alert checker.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class ProximityAlertRunnerService : Service() {

    @Inject
    internal lateinit var permissionChecker: ProximityPermissionChecker
    @Inject
    internal lateinit var manageProximityAlertsRunner: ManageProximityAlertsRunner
    @Inject
    internal lateinit var notificationFactory: ProximityServiceNotificationFactory
    @Inject
    internal lateinit var unableToRunProximityAlertsHandler: Lazy<UnableToRunProximityAlertsHandler>
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
        // API 35+ only.
        // This is not expected to be called because this foreground service type is "location"
        // which is not documented as capable of being timed out by the system. But as a
        // precaution we'll stop ourself here so the system does not forcefully kill us.
        unableToRunProximityAlertsHandler.get().handleUnableToRunProximityAlerts()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceCoroutineScope.cancel()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private suspend fun runService() {
        // Because we are started with startForegroundService(), we need to attempt to start
        // foreground first to fulfil the contract, so we are not ANR'd for not attempting to start
        // in the foreground. If the permission check was performed first and it failed, it could
        // still cause an ANR as we wouldn't have started in the foreground, therefore breaking the
        // contract.
        try {
            startForeground()
        } catch (_: IllegalStateException) {
            unableToRunProximityAlertsHandler.get().handleUnableToRunProximityAlerts()
            return
        }

        if (!permissionChecker.checkPermission()) {
            return
        }

        withContext(defaultDispatcher) {
            manageProximityAlertsRunner.run()
        }
    }

    private fun startForeground() {
        startForeground(
            FOREGROUND_NOTIFICATION_ID,
            notificationFactory.createForegroundNotification(this)
        )
    }
}
