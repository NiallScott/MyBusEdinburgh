/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.toUiDiversionsOrNull
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.toUiIncidentsOrNull

/**
 * This sealed interface is a mapped version of [ServiceUpdatesResult]
 *
 * @author Niall Scott
 */
internal sealed interface UiServiceUpdatesResult<out T : UiServiceUpdate> {

    /**
     * The request is in progress.
     */
    data object InProgress : UiServiceUpdatesResult<Nothing>

    /**
     * The result is successful.
     *
     * @property serviceUpdates The service update data.
     * @property loadTimeMillis The time this data was loaded at, in milliseconds since the UNIX
     * epoch.
     */
    data class Success<out T : UiServiceUpdate>(
        val serviceUpdates: List<T>?,
        val loadTimeMillis: Long
    ) : UiServiceUpdatesResult<T>

    /**
     * This describes an error which has occurred while getting service updates.
     *
     * @property error The error which has occurred.
     */
    data class Error(
        val error: UiError
    ) : UiServiceUpdatesResult<Nothing>
}

internal fun ServiceUpdatesResult.toUiServiceUpdatesResultOfDiversions(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): UiServiceUpdatesResult<UiDiversion> {
    return toUiServiceUpdatesResult {
        it?.toUiDiversionsOrNull(coloursForServices, serviceNamesComparator)
    }
}

internal fun ServiceUpdatesResult.toUiServiceUpdatesResultOfIncidents(
    coloursForServices: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): UiServiceUpdatesResult<UiIncident> {
    return toUiServiceUpdatesResult {
        it?.toUiIncidentsOrNull(coloursForServices, serviceNamesComparator)
    }
}

private inline fun <T : UiServiceUpdate> ServiceUpdatesResult.toUiServiceUpdatesResult(
    itemsMapper: (List<ServiceUpdate>?) -> List<T>?
): UiServiceUpdatesResult<T> {
    return when (this) {
        is ServiceUpdatesResult.InProgress -> UiServiceUpdatesResult.InProgress
        is ServiceUpdatesResult.Success -> {
            UiServiceUpdatesResult.Success(
                serviceUpdates = itemsMapper(serviceUpdates),
                loadTimeMillis = loadTimeMillis
            )
        }
        is ServiceUpdatesResult.Error -> UiServiceUpdatesResult.Error(toUiError())
    }
}