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

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.combine
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.updates.IncidentServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdateRepository
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult
import javax.inject.Inject

/**
 * This implementation fetches the incident data from the [ServiceUpdateRepository] and applies
 * the necessary transformation to make this data suitable for display.
 *
 * @param serviceUpdateRepository The source of the incident data.
 * @param servicesRepository Used to fetch service colour data.
 * @param serviceNamesComparator Used to sort the displayed services.
 * @author Niall Scott
 */
@ViewModelScoped
internal class UiContentFetcher @Inject constructor(
    private val serviceUpdateRepository: ServiceUpdateRepository,
    private val servicesRepository: ServicesRepository,
    private val serviceNamesComparator: Comparator<String>
) {

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits the latest [UiContent] to present to the user.
     */
    val incidentsContentFlow get() = serviceUpdateRepository
        .incidentServiceUpdatesFlow
        .combine(servicesRepository.getColoursForServicesFlow(null)) { su, cfs ->
            su.toUiContent(cfs)
        }

    /**
     * Map a [ServiceUpdatesResult] containing [IncidentServiceUpdate]s to [UiContent] - while also
     * merging in any service colours for display.
     *
     * @param coloursForServices The loaded colours for services. May be `null` if this could not
     * be loaded.
     * @return [UiContent] for display on the UI.
     */
    private fun ServiceUpdatesResult<IncidentServiceUpdate>.toUiContent(
        coloursForServices: Map<String, ServiceColours>?
    ): UiContent {
        return when (this) {
            is ServiceUpdatesResult.InProgress -> UiContent.InProgress
            is ServiceUpdatesResult.Success -> {
                serviceUpdates
                    ?.ifEmpty { null }
                    ?.toUiIncidents(coloursForServices, serviceNamesComparator)
                    ?.let {
                        UiContent.Success(it)
                    }
                    ?: UiContent.Error.Empty
            }
            is ServiceUpdatesResult.Error.NoConnectivity -> UiContent.Error.NoConnectivity
            is ServiceUpdatesResult.Error.Io -> UiContent.Error.Io
            is ServiceUpdatesResult.Error.Server -> UiContent.Error.Server
        }
    }
}