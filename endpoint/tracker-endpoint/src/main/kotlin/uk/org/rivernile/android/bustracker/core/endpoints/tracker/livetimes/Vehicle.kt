/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

import java.util.Date

/**
 * A `Vehicle` is a vehicle that is part of a service which has real-time tracking information
 * associated in it, described by this class.
 *
 * @property destination The destination of the vehicle.
 * @property departureTime The expected time of departure of the vehicle from the departure point.
 * @property departureMinutes At the instant the data was loaded, how many minutes are left until
 * the vehicle's departure?
 * @property terminus The terminus of the vehicle, if known.
 * @property journeyId An ID that represents the journey of the vehicle, if known.
 * @property isEstimatedTime `true` if the time is an estimate` or `false` if the vehicle is being
 * live tracked.
 * @property isDelayed `true` if the vehicle has a delay marker on it.
 * @property isDiverted `true` if the vehicle has been diverted from its published route.
 * @property isTerminus `true` if the current departure point is the route terminus.
 * @property isPartRoute `true` if the vehicle is only travelling along part of its published route.
 * @author Niall Scott
 */
data class Vehicle(
    val destination: String?,
    val departureTime: Date,
    val departureMinutes: Int,
    val terminus: String?,
    val journeyId: String?,
    val isEstimatedTime: Boolean,
    val isDelayed: Boolean,
    val isDiverted: Boolean,
    val isTerminus: Boolean,
    val isPartRoute: Boolean): Comparable<Vehicle> {

    override fun compareTo(other: Vehicle) = departureTime.compareTo(other.departureTime)
}