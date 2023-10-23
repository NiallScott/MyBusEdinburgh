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

package uk.org.rivernile.android.bustracker.core.alerts

import android.content.Intent

/**
 * This class is used to create [Intent]s used for deeplinking in to various parts of the app, e.g.
 * from notifications.
 *
 * @author Niall Scott
 */
interface DeeplinkIntentFactory {

    /**
     * Create an [Intent] used for deeplinking the user in to the bus times display for a bus stop.
     *
     * @param stopCode The stop code to show times for.
     * @return The [Intent] to launch the bus times display.
     */
    fun createShowBusTimesIntent(stopCode: String): Intent

    /**
     * Create an [Intent] used for deeplinking the user in to the stop map with the given `stopCode`
     * as the selected item.
     *
     * @param stopCode The stop code to center the map upon.
     * @return The [Intent] to launch the map, or `null` if the map is not available.
     */
    fun createShowStopOnMapIntent(stopCode: String): Intent?

    /**
     * Create an [Intent] used for deeplinking the user in to managing their alerts.
     *
     * @return The [Intent] which deeplinks the user in to managing their alerts.
     */
    fun createManageAlertsIntent(): Intent
}