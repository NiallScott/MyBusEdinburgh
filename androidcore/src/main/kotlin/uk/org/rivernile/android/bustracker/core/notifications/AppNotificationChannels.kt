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

/**
 * This interface allows notification channels to be interacted with safely depending on what
 * platform version we're running on.
 *
 * @author Niall Scott
 */
interface AppNotificationChannels {

    companion object {

        /**
         * This is the [String] constant for the foreground tasks notification channel.
         */
        const val CHANNEL_FOREGROUND_TASKS = "foregroundTasks"
        /**
         * This is the [String] constant for the arrival alerts notification channel.
         */
        const val CHANNEL_ARRIVAL_ALERTS = "arrivalAlerts"
        /**
         * This is the [String] constant for the proximity alerts notification channel.
         */
        const val CHANNEL_PROXIMITY_ALERTS = "proximityAlerts"
    }

    /**
     * Create the application's notification channels.
     */
    fun createNotificationChannels()
}