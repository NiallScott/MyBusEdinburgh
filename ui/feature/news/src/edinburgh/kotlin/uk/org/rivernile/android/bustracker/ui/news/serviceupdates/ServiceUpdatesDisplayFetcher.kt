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

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import javax.inject.Inject

/**
 * The mechanism used to get [UiDiversion]s and [UiIncident]s and the ability to refresh these
 * streams of data.
 *
 * @author Niall Scott
 */
internal interface ServiceUpdatesDisplayFetcher : AutoCloseable {

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits [UiServiceUpdatesResult] items containing
     * [UiDiversion]s. The initial subscription to this may already have loaded data or may cause a
     * new load to happen.
     */
    val diversionsDisplayFlow: Flow<ServiceUpdatesDisplay<UiDiversion>>

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits [UiServiceUpdatesResult] items containing
     * [UiIncident]s. The initial subscription to this may already have loaded data or may cause a
     * new load to happen.
     */
    val incidentsDisplayFlow: Flow<ServiceUpdatesDisplay<UiIncident>>
}

@ViewModelScoped
internal class RealServiceUpdatesDisplayFetcher @Inject constructor(
    private val serviceUpdatesFetcher: ServiceUpdatesFetcher,
    servicesRepository: ServicesRepository,
    private val serviceNamesComparator: Comparator<String>,
    private val serviceUpdatesDisplayCalculator: ServiceUpdatesDisplayCalculator,
    @param:ForViewModelCoroutineScope private val viewModelCoroutineScope: CoroutineScope
) : ServiceUpdatesDisplayFetcher, AutoCloseable by serviceUpdatesFetcher {

    override val diversionsDisplayFlow get() = serviceUpdatesFetcher
        .serviceUpdatesFlow
        .combine(serviceColoursFlow, this::toUiServiceUpdatesResultOfDiversions)
        .scan(
            initial = ServiceUpdatesDisplay.InProgress,
            operation = serviceUpdatesDisplayCalculator::calculateServiceUpdatesDisplayForDiversions
        )
        .distinctUntilChanged()

    override val incidentsDisplayFlow get() = serviceUpdatesFetcher
        .serviceUpdatesFlow
        .combine(serviceColoursFlow, this::toUiServiceUpdatesResultOfIncidents)
        .scan(
            initial = ServiceUpdatesDisplay.InProgress,
            operation = serviceUpdatesDisplayCalculator::calculateServiceUpdatesDisplayForIncidents
        )
        .distinctUntilChanged()

    private val serviceColoursFlow = servicesRepository
        .getColoursForServicesFlow()
        .shareIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0L),
            replay = 1
        )

    private fun toUiServiceUpdatesResultOfDiversions(
        serviceUpdatesResult: ServiceUpdatesResult,
        serviceColours: Map<String, ServiceColours>?
    ): UiServiceUpdatesResult<UiDiversion> {
        return serviceUpdatesResult.toUiServiceUpdatesResultOfDiversions(
            serviceColours,
            serviceNamesComparator
        )
    }

    private fun toUiServiceUpdatesResultOfIncidents(
        serviceUpdatesResult: ServiceUpdatesResult,
        serviceColours: Map<String, ServiceColours>?
    ): UiServiceUpdatesResult<UiIncident> {
        return serviceUpdatesResult.toUiServiceUpdatesResultOfIncidents(
            serviceColours,
            serviceNamesComparator
        )
    }
}