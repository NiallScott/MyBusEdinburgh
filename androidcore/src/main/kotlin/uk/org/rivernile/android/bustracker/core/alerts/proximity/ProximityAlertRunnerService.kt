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

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.androidcore.R
import uk.org.rivernile.android.bustracker.core.deeplinking.DeeplinkIntentFactory
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForServiceCoroutineScope
import uk.org.rivernile.android.bustracker.core.notifications.AppNotificationChannels
import javax.inject.Inject

/**
 * This is an Android [Service] which runs the proximity alert checker.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class ProximityAlertRunnerService : Service() {

    companion object {

        private const val FOREGROUND_NOTIFICATION_ID = 101
    }

    @Inject
    lateinit var manageProximityAlertsRunner: ManageProximityAlertsRunner
    @Inject
    lateinit var deeplinkIntentFactory: DeeplinkIntentFactory
    @Inject
    @ForServiceCoroutineScope
    lateinit var serviceCoroutineScope: CoroutineScope
    @Inject
    @ForDefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    private var job: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotification)

        if (job == null) {
            job = serviceCoroutineScope.launch {
                try {
                    withContext(defaultDispatcher) {
                        manageProximityAlertsRunner.run()
                    }
                } finally {
                    // This will usually be because a CancellationException has been thrown
                    // upstream.
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        job?.cancel()
        job = null
        serviceCoroutineScope.cancel()
    }

    override fun onBind(intent: Intent): IBinder? = null

    /**
     * Create the [Notification] which will be shown to the user while the foreground service is
     * running.
     *
     * @return The [Notification] shown to the user while the foreground service is running.
     */
    private val foregroundNotification get() =
        NotificationCompat.Builder(this, AppNotificationChannels.CHANNEL_FOREGROUND_TASKS)
            .apply {
                priority = NotificationCompat.PRIORITY_LOW
                setCategory(NotificationCompat.CATEGORY_SERVICE)
                setSmallIcon(R.drawable.ic_directions_bus_black)
                setContentTitle(getString(R.string.proximity_foreground_service_notification_title))
                setContentIntent(notificationActionPendingIntent)
                addAction(removeNotificationAction)
            }
            .build()

    /**
     * A [PendingIntent] which will be called when the user taps on the notification.
     */
    private val notificationActionPendingIntent get() =
        deeplinkIntentFactory.createManageAlertsIntent()
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let {
                PendingIntent.getActivity(
                    this,
                    FOREGROUND_NOTIFICATION_ID,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

    /**
     * A [NotificationCompat.Action] which allows the user to remove a current proximity alert.
     */
    private val removeNotificationAction get() =
        NotificationCompat.Action.Builder(
            R.drawable.ic_action_delete,
            getString(R.string.remove_all),
            removeActionButtonPendingIntent)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
            .build()

    /**
     * A [PendingIntent] which is called when the user wishes to remove the in-progress
     * proximity alert.
     */
    private val removeActionButtonPendingIntent get() =
        Intent(this, RemoveProximityAlertBroadcastReceiver::class.java)
            .let {
                PendingIntent.getBroadcast(
                    this,
                    FOREGROUND_NOTIFICATION_ID,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
}