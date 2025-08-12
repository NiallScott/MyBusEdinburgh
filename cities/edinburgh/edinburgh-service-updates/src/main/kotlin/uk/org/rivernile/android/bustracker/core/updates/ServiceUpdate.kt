/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

@file:OptIn(ExperimentalTime::class)

package uk.org.rivernile.android.bustracker.core.updates

import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate as EndpointServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * This interface defines a service update. Please see the sub-types for specific types of service
 * updates.
 *
 * @author Niall Scott
 */
public sealed interface ServiceUpdate {

    /** The ID of the service update. */
    public val id: String
    /** The time the update was last updated. */
    public val lastUpdated: Instant
    /** A title for the update */
    public val title: String
    /** A summary for the update. */
    public val summary: String
    /** The affected services, if there are any. */
    public val affectedServices: Set<String>?
    /** A URL which describes the update in more detail. */
    public val url: String?
}

/**
 * This class describes an incident service update.
 *
 * @property id The ID of the service update.
 * @property lastUpdated The time the update was last updated.
 * @property title A title for the update.
 * @property summary A summary for the update.
 * @property affectedServices The affected services, if there are any.
 * @property url A URL which describes the update in more detail.
 */
public data class IncidentServiceUpdate(
    override val id: String,
    override val lastUpdated: Instant,
    override val title: String,
    override val summary: String,
    override val affectedServices: Set<String>?,
    override val url: String?,
) : ServiceUpdate

/**
 * This class describes a planned service update.
 *
 * @property id The ID of the service update.
 * @property lastUpdated The time the update was last updated.
 * @property title A title for the update.
 * @property summary A summary for the update.
 * @property affectedServices The affected services, if there are any.
 * @property url A URL which describes the update in more detail.
 */
public data class PlannedServiceUpdate(
    override val id: String,
    override val lastUpdated: Instant,
    override val title: String,
    override val summary: String,
    override val affectedServices: Set<String>?,
    override val url: String?,
) : ServiceUpdate

/**
 * Given a [List] of [EndpointServiceUpdate]s, map this to a [List] of [ServiceUpdate]s. If the
 * [List] is empty, `null` will be returned instead.
 *
 * @return The [List] of [EndpointServiceUpdate]s as a [List] of [ServiceUpdate], or `null` if
 * empty.
 */
internal fun Collection<EndpointServiceUpdate>.toServiceUpdatesOrNull(): List<ServiceUpdate>? {
    return map {
        when (it.serviceUpdateType) {
            ServiceUpdateType.INCIDENT -> it.toIncidentServiceUpdate()
            ServiceUpdateType.PLANNED -> it.toPlannedServiceUpdate()
        }
    }.ifEmpty { null }
}

/**
 * Given an [EndpointServiceUpdate], map this to an [IncidentServiceUpdate].
 *
 * @return This [EndpointServiceUpdate] as a [IncidentServiceUpdate].
 */
private fun EndpointServiceUpdate.toIncidentServiceUpdate(): IncidentServiceUpdate {
    return IncidentServiceUpdate(
        id = id,
        lastUpdated = lastUpdated,
        title = title,
        summary = summary,
        affectedServices = affectedServices,
        url = url
    )
}

/**
 * Given an [EndpointServiceUpdate], map this to a [PlannedServiceUpdate].
 *
 * @return This [EndpointServiceUpdate] as a [PlannedServiceUpdate].
 */
private fun EndpointServiceUpdate.toPlannedServiceUpdate(): PlannedServiceUpdate {
    return PlannedServiceUpdate(
        id = id,
        lastUpdated = lastUpdated,
        title = title,
        summary = summary,
        affectedServices = affectedServices,
        url = url
    )
}