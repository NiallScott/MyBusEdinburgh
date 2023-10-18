/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

/**
 * This is the base type for user alerts.
 *
 * @author Niall Scott
 */
sealed interface Alert {

    /**
     * The ID of this alert.
     */
    val id: Int

    /**
     * The UNIX timestamp, in milliseconds, that the alert was created at.
     */
    val timeAdded: Long

    /**
     * What stop code does the alert concern?
     */
    val stopCode: String
}

/**
 * This data class describes an arrival alert that is persisted in the settings database.
 *
 * @property id The ID of this alert.
 * @property timeAdded The UNIX timestamp, in milliseconds, that the alert was created at.
 * @property stopCode What stop code does the alert concern?
 * @property serviceNames A non-empty [List] of service names to trigger the alert for.
 * @property timeTrigger The alert should be fired when any of the named services is due at the
 * named stop at this value or less.
 * @author Niall Scott
 */
data class ArrivalAlert(
    override val id: Int,
    override val timeAdded: Long,
    override val stopCode: String,
    val serviceNames: List<String>,
    val timeTrigger: Int) : Alert

/**
 * This data class describes a proximity alert that is persisted in the settings database.
 *
 * @property id The ID of this alert.
 * @property timeAdded The UNIX timestamp, in milliseconds, that the alert was created at.
 * @property stopCode What stop code does the alert concern?
 * @property distanceFrom At what maximum distance from the stop should the alert fire at? Or, what
 * is the radius of the proximity area.
 * @author Niall Scott
 */
data class ProximityAlert(
    override val id: Int,
    override val timeAdded: Long,
    override val stopCode: String,
    val distanceFrom: Int) : Alert