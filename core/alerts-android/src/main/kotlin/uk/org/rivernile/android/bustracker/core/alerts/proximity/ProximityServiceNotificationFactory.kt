/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import uk.org.rivernile.android.bustracker.core.alerts.CHANNEL_FOREGROUND_TASKS
import uk.org.rivernile.android.bustracker.core.alerts.DeeplinkIntentFactory
import uk.org.rivernile.android.bustracker.core.alerts.R
import javax.inject.Inject

/**
 * This is used to create any [Notification]s which are required by [ProximityAlertRunnerService].
 *
 * @author Niall Scott
 */
internal interface ProximityServiceNotificationFactory {

    /**
     * Create the [Notification] which will be shown to the user while the foreground service is
     * running.
     *
     * @param context The [Context] that this notification is created under.
     * @return The [Notification] shown to the user while the foreground service is running.
     */
    fun createForegroundNotification(context: Context): Notification
}

internal const val FOREGROUND_NOTIFICATION_ID = 101

internal class DefaultProximityServiceNotificationFactory @Inject constructor(
    private val deeplinkIntentFactory: DeeplinkIntentFactory
) : ProximityServiceNotificationFactory {

    override fun createForegroundNotification(context: Context): Notification {
        return NotificationCompat
            .Builder(context, CHANNEL_FOREGROUND_TASKS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSmallIcon(R.drawable.ic_directions_bus_black)
            .setContentTitle(
                context.getString(R.string.proximity_foreground_service_notification_title)
            )
            .setContentIntent(createNotificationActionPendingIntent(context))
            .addAction(createRemoveNotificationAction(context))
            .build()
    }

    private fun createNotificationActionPendingIntent(context: Context): PendingIntent {
        return deeplinkIntentFactory.createManageAlertsIntent()
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let {
                PendingIntent.getActivity(
                    context,
                    FOREGROUND_NOTIFICATION_ID,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
    }

    private fun createRemoveNotificationAction(context: Context): NotificationCompat.Action {
        return NotificationCompat.Action
            .Builder(
                R.drawable.ic_action_delete_notification,
                context.getString(R.string.remove_all),
                createRemoveActionButtonPendingIntent(context)
            )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
            .build()
    }

    private fun createRemoveActionButtonPendingIntent(context: Context): PendingIntent {
        return Intent(context, RemoveProximityAlertBroadcastReceiver::class.java)
            .let {
                PendingIntent.getBroadcast(
                    context,
                    FOREGROUND_NOTIFICATION_ID,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
    }
}
