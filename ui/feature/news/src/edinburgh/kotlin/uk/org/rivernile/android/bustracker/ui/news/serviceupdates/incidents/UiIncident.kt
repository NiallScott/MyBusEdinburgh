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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.updates.IncidentServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdate
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiMoreDetails
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiServiceUpdate
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.toUiServiceNamesOrNull
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * This represents the data for an incident which is shown on the UI.
 *
 * @property id The ID of the incident.
 * @property lastUpdated The [Instant] that this incident was last updated at.
 * @property title A short title for the incident.
 * @property summary A summary describing the incident.
 * @property affectedServices A listing of affected services, if any.
 * @property moreDetails How to find more details regarding this incident, if available.
 * @author Niall Scott
 */
@Immutable
internal data class UiIncident(
    override val id: String,
    val lastUpdated: Instant,
    val title: String,
    val summary: String,
    val affectedServices: List<UiServiceName>?,
    val moreDetails: UiMoreDetails?
) : UiServiceUpdate

/**
 * Given a [List] of [ServiceUpdate]s, map this to a [List] of [UiIncident]s if it contains any
 * [IncidentServiceUpdate]s. If there are no [IncidentServiceUpdate]s then `null` will be returned.
 *
 * @param coloursForServices A [Map] of service names to [ServiceColours]. May be `null` or services
 * may be missing.
 * @param serviceNamesComparator A [Comparator] used to sort the service names.
 * @return The [List] of [IncidentServiceUpdate]s as a [List] of [UiIncident]s.
 */
internal fun List<ServiceUpdate>.toUiIncidentsOrNull(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): List<UiIncident>? {
    return mapNotNull {
        if (it is IncidentServiceUpdate) {
            it.toUiIncident(coloursForServices, serviceNamesComparator)
        } else {
            null
        }
    }.ifEmpty { null }
}

/**
 * Map a [IncidentServiceUpdate] to a [UiIncident].
 *
 * @param coloursForServices A [Map] of service names to [ServiceColours] to populate service
 * colours.
 * @param serviceNamesComparator A [Comparator] used to sort the affected service names.
 * @return The given [IncidentServiceUpdate] as a [UiIncident].
 */
private fun IncidentServiceUpdate.toUiIncident(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): UiIncident {
    val mappedAffectedServices = toUiServiceNamesOrNull(
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
        moreDetails = url?.takeIf { it.isNotBlank() }?.let(::UiMoreDetails)
    )
}