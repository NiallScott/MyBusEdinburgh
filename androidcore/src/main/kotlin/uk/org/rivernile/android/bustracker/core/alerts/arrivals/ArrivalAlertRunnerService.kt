/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.android.AndroidInjection
import uk.org.rivernile.android.bustracker.androidcore.R
import uk.org.rivernile.android.bustracker.core.deeplinking.DeeplinkIntentFactory
import uk.org.rivernile.android.bustracker.core.notifications.AppNotificationChannels
import javax.inject.Inject

/**
 * This is an Android [Service] which runs the arrival alert checker.
 *
 * @author Niall Scott
 */
class ArrivalAlertRunnerService : Service() {

    companion object {

        private const val FOREGROUND_NOTIFICATION_ID = 100
    }

    @Inject
    lateinit var timeAlertRunner: TimeAlertRunner
    @Inject
    lateinit var deeplinkIntentFactory: DeeplinkIntentFactory

    override fun onCreate() {
        AndroidInjection.inject(this)

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
        timeAlertRunner.start(this::stopSelf)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        timeAlertRunner.stop()
    }

    override fun onBind(intent: Intent): IBinder? = null

    /**
     * Create the [Notification] which will be shown to the user while the foreground service is
     * running.
     *
     * @return The [Notification] shown to the user while the foreground service is running.
     */
    private fun createForegroundNotification(): Notification =
            NotificationCompat.Builder(this, AppNotificationChannels.CHANNEL_FOREGROUND_TASKS)
                    .apply {
                        priority = NotificationCompat.PRIORITY_LOW
                        setCategory(NotificationCompat.CATEGORY_SERVICE)
                        setSmallIcon(R.drawable.ic_directions_bus_black)
                        setContentTitle(
                                getString(R.string.arrival_foreground_service_notification_title))
                        setContentIntent(createNotificationActionPendingIntent())
                        addAction(createRemoveNotificationAction())
                    }
                    .build()

    /**
     * Create the [PendingIntent] which will be called when the user taps on the notification.
     *
     * @return The [PendingIntent] called when the user taps on the notification.
     */
    private fun createNotificationActionPendingIntent() =
            deeplinkIntentFactory.createManageAlertsIntent()
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let {
                        PendingIntent.getActivity(this, 0, it, 0)
                    }

    /**
     * Create a [NotificationCompat.Action] which allows the user to remove a current arrival time
     * check.
     *
     * @return A [NotificationCompat.Action] which allows the user to remove a current arrival time
     * check.
     */
    private fun createRemoveNotificationAction() =
            NotificationCompat.Action.Builder(
                    R.drawable.ic_action_delete,
                    getString(R.string.remove),
                    createRemoveActionButtonPendingIntent())
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
                    .build()

    /**
     * Create a [PendingIntent] which is called when the user wishes to remove the in-progress
     * arrival alert check.
     */
    private fun createRemoveActionButtonPendingIntent() =
            Intent(this, RemoveArrivalAlertBroadcastReceiver::class.java)
                    .let {
                        PendingIntent.getBroadcast(this, 0, it, 0)
                    }
}