/*
 * Copyright (C) 2019 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes

import kotlin.time.Instant

/**
 * A `Vehicle` is a vehicle that is part of a service which has real-time tracking information
 * associated in it, described by this class.
 *
 * @property destination The destination of the vehicle.
 * @property departureTime The expected time of departure of the vehicle from the departure point.
 * @property departureMinutes At the instant the data was loaded, how many minutes are left until
 * the vehicle's departure?
 * @property isEstimatedTime `true` if the time is an estimate` or `false` if the vehicle is being
 * live tracked.
 * @property isDiverted `true` if the vehicle has been diverted from its published route.
 * @author Niall Scott
 */
public data class Vehicle(
    val destination: String?,
    val departureTime: Instant,
    val departureMinutes: Int,
    val isEstimatedTime: Boolean,
    val isDiverted: Boolean
): Comparable<Vehicle> {

    override fun compareTo(other: Vehicle): Int = departureTime.compareTo(other.departureTime)
}
