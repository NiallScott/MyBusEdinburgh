/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import kotlin.time.Instant

/**
 * This represents a departure/live time for a service.
 *
 * @property destination Where this departure is heading to.
 * @property isDiverted Is this departure diverted via another stop?
 * @property departureTime The time the departure is expected to occur.
 * @property departureMinutes The expected number of minutes until departure.
 * @property isEstimatedTime Is the time an estimate or a real-time prediction?
 * @author Niall Scott
 */
data class UiVehicle(
    val destination: String?,
    val isDiverted: Boolean,
    val departureTime: Instant,
    val departureMinutes: Int,
    val isEstimatedTime: Boolean
)
