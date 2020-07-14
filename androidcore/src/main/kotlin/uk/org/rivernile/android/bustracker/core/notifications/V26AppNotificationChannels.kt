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

package uk.org.rivernile.android.bustracker.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import uk.org.rivernile.android.bustracker.androidcore.R

/**
 * This is an implementation of [AppNotificationChannels] that runs on API [Build.VERSION_CODES.O]
 * and above.
 *
 * @param context The application [Context].
 * @param notificationManager The system [NotificationManagerCompat].
 * @author Niall Scott
 */
@RequiresApi(Build.VERSION_CODES.O)
internal class V26AppNotificationChannels(
        private val context: Context,
        private val notificationManager: NotificationManagerCompat)
    : AppNotificationChannels {

    override fun createNotificationChannels() {
        listOf(createForegroundTasksNotificationChannel(),
                createArrivalAlertNotificationChannel(),
                createProximityAlertNotificationChannel())
                .let(notificationManager::createNotificationChannels)
    }

    /**
     * Create the required [NotificationChannel] for foreground tasks.
     *
     * @return The [NotificationChannel] for foreground tasks.
     */
    private fun createForegroundTasksNotificationChannel(): NotificationChannel {
        val name = context.getString(R.string.notification_channel_foreground_name)
        val description = context.getString(R.string.notification_channel_foreground_description)

        return NotificationChannel(AppNotificationChannels.CHANNEL_FOREGROUND_TASKS, name,
                NotificationManager.IMPORTANCE_LOW).apply {
            setDescription(description)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
        }
    }

    /**
     * Create the required [NotificationChannel] for arrival alerts.
     *
     * @return The [NotificationChannel] for arrival alerts.
     */
    private fun createArrivalAlertNotificationChannel(): NotificationChannel {
        val name = context.getString(R.string.notification_channel_arrival_alert_name)
        val description = context.getString(R.string.notification_channel_arrival_alert_description)

        return NotificationChannel(AppNotificationChannels.CHANNEL_ARRIVAL_ALERTS, name,
                NotificationManager.IMPORTANCE_HIGH).apply {
            setDescription(description)
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
        }
    }

    /**
     * Create the required [NotificationChannel] for proximity alerts.
     *
     * @return The [NotificationChannel] for proximity alerts.
     */
    private fun createProximityAlertNotificationChannel(): NotificationChannel {
        val name = context.getString(R.string.notification_channel_proximity_alert_name)
        val description = context.getString(
                R.string.notification_channel_proximity_alert_description)

        return NotificationChannel(AppNotificationChannels.CHANNEL_PROXIMITY_ALERTS, name,
                NotificationManager.IMPORTANCE_HIGH).apply {
            setDescription(description)
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
        }
    }
}