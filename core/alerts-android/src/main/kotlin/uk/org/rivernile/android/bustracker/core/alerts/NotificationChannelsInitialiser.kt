/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.startup.Initializer

/**
 * An app [Initializer] which initialises the notification channels related to alerts.
 *
 * @author Niall Scott
 */
@Suppress("unused")
class NotificationChannelsInitialiser : Initializer<Unit> {

    override fun create(context: Context) {
        listOf(
            createForegroundTasksNotificationChannel(context),
            createArrivalAlertNotificationChannel(context),
            createProximityAlertNotificationChannel(context))
            .let {
                NotificationManagerCompat
                    .from(context)
                    .createNotificationChannelsCompat(it)
            }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    /**
     * Create the required [NotificationChannelCompat] for foreground tasks.
     *
     * @param context The application [Context].
     * @return The [NotificationChannelCompat] for foreground tasks.
     */
    private fun createForegroundTasksNotificationChannel(
        context: Context): NotificationChannelCompat {
        return NotificationChannelCompat.Builder(
            CHANNEL_FOREGROUND_TASKS,
            NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_foreground_name))
            .setDescription(context.getString(R.string.notification_channel_foreground_description))
            .setLightsEnabled(false)
            .setVibrationEnabled(false)
            .setShowBadge(false)
            .build()
    }

    /**
     * Create the required [NotificationChannelCompat] for arrival alerts.
     *
     * @param context The application [Context].
     * @return The [NotificationChannelCompat] for arrival alerts.
     */
    private fun createArrivalAlertNotificationChannel(context: Context): NotificationChannelCompat {
        return NotificationChannelCompat.Builder(
            CHANNEL_ARRIVAL_ALERTS,
            NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.notification_channel_arrival_alert_name))
            .setDescription(
                context.getString(R.string.notification_channel_arrival_alert_description))
            .setLightsEnabled(true)
            .setVibrationEnabled(true)
            .setShowBadge(true)
            .build()
    }

    /**
     * Create the required [NotificationChannelCompat] for proximity alerts.
     *
     * @param context The application [Context].
     * @return The [NotificationChannelCompat] for proximity alerts.
     */
    private fun createProximityAlertNotificationChannel(
        context: Context): NotificationChannelCompat {
        return NotificationChannelCompat.Builder(
            CHANNEL_PROXIMITY_ALERTS,
            NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.notification_channel_proximity_alert_name))
            .setDescription(
                context.getString(R.string.notification_channel_proximity_alert_description))
            .setLightsEnabled(true)
            .setVibrationEnabled(true)
            .setShowBadge(true)
            .build()
    }
}