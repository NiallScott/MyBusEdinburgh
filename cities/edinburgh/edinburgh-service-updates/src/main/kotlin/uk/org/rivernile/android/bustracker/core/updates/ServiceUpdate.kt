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

package uk.org.rivernile.android.bustracker.core.updates

import kotlinx.datetime.Instant

/**
 * This interface defines a service update. Please see the sub-types for specific types of service
 * updates.
 *
 * @author Niall Scott
 */
sealed interface ServiceUpdate {

    /** The ID of the service update. */
    val id: String
    /** The time the update was last updated. */
    val lastUpdated: Instant
    /** A summary for the update. */
    val summary: String
    /** The affected services, if there are any. */
    val affectedServices: Set<String>?
    /** A URL which describes the update in more detail. */
    val url: String?
}

/**
 * This class describes an incident service update.
 *
 * @property id The ID of the service update.
 * @property lastUpdated The time the update was last updated.
 * @property summary A summary for the update.
 * @property affectedServices The affected services, if there are any.
 * @property url A URL which describes the update in more detail.
 */
data class IncidentServiceUpdate(
    override val id: String,
    override val lastUpdated: Instant,
    override val summary: String,
    override val affectedServices: Set<String>?,
    override val url: String?,
) : ServiceUpdate

/**
 * This class describes a planned service update.
 *
 * @property id The ID of the service update.
 * @property lastUpdated The time the update was last updated.
 * @property summary A summary for the update.
 * @property affectedServices The affected services, if there are any.
 * @property url A URL which describes the update in more detail.
 */
data class PlannedServiceUpdate(
    override val id: String,
    override val lastUpdated: Instant,
    override val summary: String,
    override val affectedServices: Set<String>?,
    override val url: String?,
) : ServiceUpdate