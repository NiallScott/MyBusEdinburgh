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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes

/**
 * `LiveTimes` maps unique stop codes to [Stop] instances. It also returns data that is global to
 * the live times request.
 *
 * @property stops A [Map] of stop code to the stop result.
 * @property receiveTime The time this data was received at.
 * @property hasGlobalDisruption `true` if there is a global disruption on the network.
 * @author Niall Scott
 */
data class LiveTimes(
        val stops: Map<String, Stop>,
        val receiveTime: Long,
        val hasGlobalDisruption: Boolean)