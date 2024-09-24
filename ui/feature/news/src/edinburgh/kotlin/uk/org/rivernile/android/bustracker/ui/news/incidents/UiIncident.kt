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

package uk.org.rivernile.android.bustracker.ui.news.incidents

import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.updates.IncidentServiceUpdate
import uk.org.rivernile.android.bustracker.ui.news.UiAffectedService
import uk.org.rivernile.android.bustracker.ui.news.toUiAffectedServicesOrNull

/**
 * This represents the data for an incident which is shown on the UI.
 *
 * @property id The ID of the incident.
 * @property lastUpdated The [Instant] that this incident was last updated at.
 * @property title A short title for the incident.
 * @property summary A summary describing the incident.
 * @property affectedServices A listing of [UiAffectedService]s, if any.
 * @property url An optional URL to the incident on the web.
 * @property showMoreDetailsButton Whether the 'Show more details' button should be shown.
 * @author Niall Scott
 */
internal data class UiIncident(
    val id: String,
    val lastUpdated: Instant,
    val title: String,
    val summary: String,
    val affectedServices: List<UiAffectedService>?,
    val url: String?,
    val showMoreDetailsButton: Boolean
)

/**
 * Given a [List] of [IncidentServiceUpdate]s, map this to a [List] of [UiIncident]s.
 *
 * @param coloursForServices A [Map] of service names to [ServiceColours]. May be `null` or services
 * may be missing.
 * @return The [List] of [IncidentServiceUpdate]s as a [List] of [UiIncident]s.
 */
internal fun List<IncidentServiceUpdate>.toUiIncidents(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): List<UiIncident> {
    return map {
        it.toUiIncident(coloursForServices, serviceNamesComparator)
    }
}

/**
 * Map a [IncidentServiceUpdate] to a [UiIncident].
 *
 * @param coloursForServices A [Map] of service names to [ServiceColours] to populate service
 * colours.
 * @param serviceNamesComparator
 */
private fun IncidentServiceUpdate.toUiIncident(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): UiIncident {
    val mappedAffectedServices = toUiAffectedServicesOrNull(
        affectedServices,
        coloursForServices,
        serviceNamesComparator
    )

    return UiIncident(
        id = id,
        lastUpdated = lastUpdated,
        title = title,
        summary = summary,
        affectedServices = mappedAffectedServices,
        url = url,
        showMoreDetailsButton = !url.isNullOrBlank()
    )
}