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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.time.ElapsedTimeCalculator
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import javax.inject.Inject

/**
 * Fetches content to be displayed and presents this as an [UiContent] instance.
 *
 * @author Niall Scott
 */
internal interface UiContentFetcher : AutoCloseable {

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits [UiDiversion] [UiContent] items. The initial
     * subscription to this may already have loaded data or may cause a new load to happen. To
     * cause new data loads (i.e. to refresh the data), call [refresh].
     */
    val diversionsContentFlow: Flow<UiContent<UiDiversion>>

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits [UiIncident] [UiContent] items. The initial
     * subscription to this may already have loaded data or may cause a new load to happen. To
     * cause new data loads (i.e. to refresh the data), call [refresh].
     */
    val incidentsContentFlow: Flow<UiContent<UiIncident>>

    /**
     * Trigger a refresh on the Service Updates data. The new data will be emitted from
     * [diversionsContentFlow] and [incidentsContentFlow]. If this method is called while a load
     * is taking place, it will cause the current load to be aborted a new fresh loading attempt
     * is performed.
     */
    fun refresh()
}

@ViewModelScoped
internal class RealUiContentFetcher @Inject constructor(
    private val serviceUpdatesDisplayFetcher: ServiceUpdatesDisplayFetcher,
    private val connectivityRepository: ConnectivityRepository,
    private val elapsedTimeCalculator: ElapsedTimeCalculator
) : UiContentFetcher {

    override val diversionsContentFlow get() = serviceUpdatesDisplayFetcher
        .diversionsDisplayFlow
        .combine(connectivityRepository.hasInternetConnectivityFlow, ::FetchedDataHolder)
        .produceUiState()
        .distinctUntilChanged()

    override val incidentsContentFlow get() = serviceUpdatesDisplayFetcher
        .incidentsDisplayFlow
        .combine(connectivityRepository.hasInternetConnectivityFlow, ::FetchedDataHolder)
        .produceUiState()
        .distinctUntilChanged()

    override fun refresh() {
        serviceUpdatesDisplayFetcher.refresh()
    }

    override fun close() {
        serviceUpdatesDisplayFetcher.close()
    }

    private fun <T : UiServiceUpdate> Flow<FetchedDataHolder<T>>.produceUiState() = channelFlow {
        collectLatest { fetchedData ->
            when (val serviceUpdatesDisplay = fetchedData.serviceUpdatesDisplay) {
                is ServiceUpdatesDisplay.InProgress ->
                    send(toUiContentInProgress())
                is ServiceUpdatesDisplay.Populated -> {
                    elapsedTimeCalculator
                        .getElapsedTimeMinutesFlow(serviceUpdatesDisplay.loadTimeMillis)
                        .collectLatest {
                            val uiContent = serviceUpdatesDisplay.toUiContentPopulated(
                                hasInternetConnectivity = fetchedData.hasInternetConnectivity,
                                lastRefreshTime = it.toUiLastRefreshed()
                            )

                            send(uiContent)
                        }
                }
                is ServiceUpdatesDisplay.Error ->
                    send(serviceUpdatesDisplay.toUiContentError())
            }
        }
    }

    private data class FetchedDataHolder<out T : UiServiceUpdate>(
        val serviceUpdatesDisplay: ServiceUpdatesDisplay<T>,
        val hasInternetConnectivity: Boolean
    )
}