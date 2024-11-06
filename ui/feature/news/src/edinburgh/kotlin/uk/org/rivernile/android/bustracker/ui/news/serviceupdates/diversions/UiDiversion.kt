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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.updates.PlannedServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdate
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiServiceUpdate
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.toUiServiceNamesOrNull
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * This represents the data for a diversion which is shown on the UI.
 *
 * @property id The ID of the diversion.
 * @property lastUpdated The [Instant] that this diversion was last updated at.
 * @property title A short title for the diversion.
 * @property summary A summary describing the diversion.
 * @property affectedServices A listing of affected services, if any.
 * @property url An optional URL to the diversion on the web.
 * @property showMoreDetailsButton Whether the 'Show more details' button should be shown.
 * @author Niall Scott
 */
@Immutable
internal data class UiDiversion(
    override val id: String,
    val lastUpdated: Instant,
    val title: String,
    val summary: String,
    val affectedServices: List<UiServiceName>?,
    val url: String?,
    val showMoreDetailsButton: Boolean
) : UiServiceUpdate

/**
 * Given a [List] of [ServiceUpdate]s, map this to a [List] of [UiDiversion]s if it contains any
 * [PlannedServiceUpdate]s. If there are no [PlannedServiceUpdate]s then `null` will be returned.
 *
 * @param coloursForServices A [Map] of service names to [ServiceColours]. May be `null` or services
 * may be missing.
 * @param serviceNamesComparator A [Comparator] used to sort the service names.
 * @return The [List] of [PlannedServiceUpdate]s as a [List] of [UiDiversion]s.
 */
internal fun List<ServiceUpdate>.toUiDiversionsOrNull(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): List<UiDiversion>? {
    return mapNotNull {
        if (it is PlannedServiceUpdate) {
            it.toUiDiversion(coloursForServices, serviceNamesComparator)
        } else {
            null
        }
    }.ifEmpty { null }
}

/**
 * Map a [PlannedServiceUpdate] to a [UiDiversion].
 *
 * @param coloursForServices A [Map] of service names to [ServiceColours] to populate service
 * colours.
 * @param serviceNamesComparator A [Comparator] used to sort the affected service names.
 * @return The given [PlannedServiceUpdate] as a [UiDiversion].
 */
private fun PlannedServiceUpdate.toUiDiversion(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): UiDiversion {
    val mappedAffectedServices = toUiServiceNamesOrNull(
        affectedServices,
        coloursForServices,
        serviceNamesComparator
    )

    return UiDiversion(
        id = id,
        lastUpdated = lastUpdated,
        title = title,
        summary = summary,
        affectedServices = mappedAffectedServices,
        url = url?.takeIf { it.isNotBlank() },
        showMoreDetailsButton = !url.isNullOrBlank()
    )
}