/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.updates.service

import kotlinx.datetime.Instant

/**
 * This class contains details for a service update.
 *
 * @property id The unique ID of the update.
 * @property lastUpdated The time that this update was last updated at.
 * @property serviceUpdateType The type of this service update.
 * @property summary Text which summarises the service update.
 * @property affectedServices A [Set] of services this affects. `null` is equivalent to empty.
 * @property url An optional URL which provides a web link for this disruption.
 * @author Niall Scott
 */
data class ServiceUpdate(
    val id: String,
    val lastUpdated: Instant,
    val serviceUpdateType: ServiceUpdateType,
    val summary: String,
    val affectedServices: Set<String>?,
    val url: String?
)