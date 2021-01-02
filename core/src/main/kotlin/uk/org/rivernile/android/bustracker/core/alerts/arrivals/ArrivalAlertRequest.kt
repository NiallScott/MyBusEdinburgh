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

package uk.org.rivernile.android.bustracker.core.alerts.arrivals

/**
 * This class holds data for an arrival alert that the user has requested.
 *
 * @property stopCode The stop code this arrival alert is for.
 * @property serviceNames A non-empty [List] of service names to trigger the alert for.
 * @property timeTrigger The alert should be fired when any of the named services is due at the
 * named stop at this value or less.
 * @author Niall Scott
 */
data class ArrivalAlertRequest(
        val stopCode: String,
        val serviceNames: List<String>,
        val timeTrigger: Int)