/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts

import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails

/**
 * The base class for a UI alert.
 *
 * @author Niall Scott
 */
sealed class UiAlert {

    /**
     * The ID of the alert.
     */
    abstract val id: Int

    /**
     * An arrival alert.
     *
     * @property id The ID of the alert.
     * @property stopCode The stop code of the alert.
     * @property stopDetails Details of the stop this alert is for.
     * @property services The [List] of service names this alert triggers for.
     * @property timeTrigger The time trigger this alert triggers for.
     */
    data class ArrivalAlert(
            override val id: Int,
            val stopCode: String,
            val stopDetails: StopDetails?,
            val services: List<String>,
            val timeTrigger: Int) : UiAlert()

    /**
     * A proximity alert.
     *
     * @property id The ID of the alert.
     * @property stopCode The stop code of the alert.
     * @property stopDetails Details of the stop this alert is for.
     * @property distanceFrom The distance trigger this alert triggers for.
     */
    data class ProximityAlert(
            override val id: Int,
            val stopCode: String,
            val stopDetails: StopDetails?,
            val distanceFrom: Int) : UiAlert()
}