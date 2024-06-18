/*
 * Copyright (C) 2019 - 2024 Niall 'Rivernile' Scott
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

/**
 * A `Service` represents a single service that stops at a single stop. It holds a collection of
 * [Vehicle]s (or rather, live vehicle departures).
 *
 * @property serviceName The display name of the service.
 * @property vehicles A [List] of [Vehicle]s for this service.
 * @property operator An optional operator name for the service.
 * @property routeDescription An optional textual description of the route.
 * @property isDisrupted `true` if the service is currently disrupted.
 * @property isDiverted `true` if the service is currently diverted from its published route.
 * @author Niall Scott
 */
data class Service(
    val serviceName: String,
    val vehicles: List<Vehicle>,
    val operator: String?,
    val routeDescription: String?,
    val isDisrupted: Boolean,
    val isDiverted: Boolean
)