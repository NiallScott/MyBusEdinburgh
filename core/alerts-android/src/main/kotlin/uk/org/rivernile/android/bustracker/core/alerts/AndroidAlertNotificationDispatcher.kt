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

package uk.org.rivernile.android.bustracker.core.alerts

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.permission.AndroidPermissionChecker
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import javax.inject.Inject

/**
 * This is the Android-specific implementation of [AlertNotificationDispatcher].
 *
 * @param context The application [Context].
 * @param notificationManager The [NotificationManagerCompat].
 * @param busStopsRepository The repository to access bus stop data.
 * @param deeplinkIntentFactory An implementation which creates [Intent]s for deeplinking.
 * @param textFormattingUtils Utility class for formatting text of stop name.
 * @author Niall Scott
 */
internal class AndroidAlertNotificationDispatcher @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
    private val busStopsRepository: BusStopsRepository,
    private val permissionChecker: AndroidPermissionChecker,
    private val deeplinkIntentFactory: DeeplinkIntentFactory,
    private val textFormattingUtils: TextFormattingUtils
) : AlertNotificationDispatcher, ErrorNotificationDispatcher {

    companion object {

        private const val NOTIFICATION_ID_ARRIVAL = 2
        private const val NOTIFICATION_ID_PROXIMITY = 3

        private const val TIMEOUT_ALERT_MILLIS = 1800000L // 30 minutes
    }

    override suspend fun dispatchTimeAlertNotification(
        arrivalAlert: ArrivalAlert,
        qualifyingServices: List<Service>
    ) {
        if (permissionChecker.checkPostNotificationPermission()) {
            val title = context.getString(R.string.arrival_alert_notification_title)
            val summary = createAlertSummaryString(arrivalAlert, qualifyingServices)
            val pendingIntent = createArrivalAlertPendingIntent(arrivalAlert.stopCode)

            NotificationCompat.Builder(
                context,
                CHANNEL_ARRIVAL_ALERTS
            ).apply {
                setSmallIcon(R.drawable.ic_directions_bus_black)
                setContentTitle(title)
                setContentText(summary)
                setTicker(summary)
                setStyle(NotificationCompat.BigTextStyle().bigText(summary))
                priority = NotificationCompat.PRIORITY_HIGH
                setCategory(NotificationCompat.CATEGORY_ALARM)
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setTimeoutAfter(TIMEOUT_ALERT_MILLIS)
            }.let {
                notificationManager.notify(NOTIFICATION_ID_ARRIVAL, it.build())
            }
        }
    }

    override suspend fun dispatchProximityAlertNotification(proximityAlert: ProximityAlert) {
        if (permissionChecker.checkPostNotificationPermission()) {
            val stopName = getDisplayableStopName(proximityAlert.stopCode)
            val title = context.getString(R.string.proximity_alert_notification_title, stopName)
            val ticker = context.getString(R.string.proximity_alert_notification_ticker, stopName)
            val summary = context.getString(
                R.string.proximity_alert_notification_summary,
                proximityAlert.distanceFrom,
                stopName
            )
            val pendingIntent = createProximityAlertPendingIntent(proximityAlert.stopCode)

            NotificationCompat.Builder(
                context,
                CHANNEL_PROXIMITY_ALERTS
            ).apply {
                setSmallIcon(R.drawable.ic_directions_bus_black)
                setContentTitle(title)
                setContentText(summary)
                setTicker(ticker)
                setStyle(NotificationCompat.BigTextStyle().bigText(summary))
                priority = NotificationCompat.PRIORITY_HIGH
                setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setTimeoutAfter(TIMEOUT_ALERT_MILLIS)
            }.let {
                notificationManager.notify(NOTIFICATION_ID_PROXIMITY, it.build())
            }
        }
    }

    override fun dispatchUnableToRunArrivalAlertsNotification() {
        if (permissionChecker.checkPostNotificationPermission()) {
            val title = context.getString(R.string.arrival_alert_notification_unable_to_run_title)
            val summary = context
                .getString(R.string.arrival_alert_notification_unable_to_run_summary)

            NotificationCompat.Builder(
                context,
                CHANNEL_ARRIVAL_ALERTS
            ).apply {
                setSmallIcon(R.drawable.ic_directions_bus_black)
                setContentTitle(title)
                setContentText(summary)
                setTicker(summary)
                setStyle(NotificationCompat.BigTextStyle().bigText(summary))
                priority = NotificationCompat.PRIORITY_HIGH
                setCategory(NotificationCompat.CATEGORY_ALARM)
                setContentIntent(createAlertManagerPendingIntent(NOTIFICATION_ID_ARRIVAL))
                setAutoCancel(true)
                setTimeoutAfter(TIMEOUT_ALERT_MILLIS)
            }.let {
                notificationManager.notify(NOTIFICATION_ID_ARRIVAL, it.build())
            }
        }
    }

    override fun dispatchUnableToRunProximityAlertsNotification() {
        if (permissionChecker.checkPostNotificationPermission()) {
            val title = context.getString(R.string.proximity_alert_notification_unable_to_run_title)
            val summary = context
                .getString(R.string.proximity_alert_notification_unable_to_run_summary)

            NotificationCompat.Builder(
                context,
                CHANNEL_PROXIMITY_ALERTS
            ).apply {
                setSmallIcon(R.drawable.ic_directions_bus_black)
                setContentTitle(title)
                setContentText(summary)
                setTicker(summary)
                setStyle(NotificationCompat.BigTextStyle().bigText(summary))
                priority = NotificationCompat.PRIORITY_HIGH
                setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                setContentIntent(createAlertManagerPendingIntent(NOTIFICATION_ID_PROXIMITY))
                setAutoCancel(true)
                setTimeoutAfter(TIMEOUT_ALERT_MILLIS)
            }.let {
                notificationManager.notify(NOTIFICATION_ID_PROXIMITY, it.build())
            }
        }
    }

    /**
     * Create the summary [String] that is shown to the user detailing the alert.
     *
     * @param arrivalAlert The [ArrivalAlert] that caused the notification.
     * @param qualifyingServices What services caused the notification to be fired.
     * @return The summary [String] shown to the user.
     */
    private suspend fun createAlertSummaryString(
        arrivalAlert: ArrivalAlert,
        qualifyingServices: List<Service>
    ): String {
        val serviceListing = qualifyingServices.joinToString { it.serviceName }
        val numberOfMinutes = getAlertNumberOfMinutesString(arrivalAlert.timeTrigger)
        val displayableStopName = getDisplayableStopName(arrivalAlert.stopCode)

        return context.resources.getQuantityString(
            R.plurals.arrival_alert_notification_services,
            qualifyingServices.size,
            serviceListing,
            numberOfMinutes,
            displayableStopName
        )
    }

    /**
     * Get a formatted [String] to display to the user of the number of minutes until arrival.
     *
     * @param minutes The number of minutes time trigger.
     * @return Human readable number of minutes until arrival.
     */
    private fun getAlertNumberOfMinutesString(minutes: Int): String {
        return if (minutes < 2) {
            context.getString(R.string.arrival_alert_notification_time_now)
        } else {
            context.resources.getQuantityString(
                R.plurals.arrival_alert_notifications_time_plural,
                minutes,
                minutes
            )
        }
    }

    /**
     * Get the stop name to display.
     *
     * @param stopCode The stop code.
     * @return The stop name to display.
     */
    private suspend fun getDisplayableStopName(stopCode: String): String {
        return textFormattingUtils.formatBusStopNameWithStopCode(
            stopCode,
            busStopsRepository.getNameForStop(stopCode)
        )
    }

    /**
     * Create a [PendingIntent] for a fired arrival alert - the action which is performed when the
     * user clicks on the notification.
     *
     * @param stopCode The stop code.
     * @return A [PendingIntent] which is executed when the user clicks on the notification.
     */
    private fun createArrivalAlertPendingIntent(stopCode: String) =
        deeplinkIntentFactory.createShowBusTimesIntent(stopCode)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .let {
                PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID_ARRIVAL,
                    it,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }

    /**
     * Create a [PendingIntent] for a fired proximity alert - the action which is performed when the
     * user clicks on the notification.
     *
     * @param stopCode The stop code.
     * @return A [PendingIntent] which is executed when the user clicks on the notification.
     */
    private fun createProximityAlertPendingIntent(stopCode: String) =
        (deeplinkIntentFactory.createShowStopOnMapIntent(stopCode)
            ?: deeplinkIntentFactory.createShowBusTimesIntent(stopCode))
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .let {
                PendingIntent.getActivity(
                    context,
                    NOTIFICATION_ID_PROXIMITY,
                    it,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }

    /**
     * Create a [PendingIntent] which deep-links the user in to the alert manager UI when the user
     * clicks on the notification.
     *
     * @param notificationId The ID of the notification.
     * @return A [PendingIntent] which is executed when the user clicks on the notification.
     */
    private fun createAlertManagerPendingIntent(notificationId: Int): PendingIntent {
        return deeplinkIntentFactory.createManageAlertsIntent()
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let {
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    it,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }
    }
}
