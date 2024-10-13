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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdateRepository
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.toUiDiversionsOrNull
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.toUiIncidentsOrNull
import javax.inject.Inject

/**
 * A class with the responsibility of fetching Service Updates data streaming this data to
 * interested callers.
 *
 * @param serviceUpdateRepository The source of the Service Update data.
 * @param servicesRepository Used to get service colours.
 * @param serviceNamesComparator Used to sort services by name.
 * @param viewModelCoroutineScope The parent [CoroutineScope] to launch any coroutines under.
 * @author Niall Scott
 */
@ViewModelScoped
internal class UiServiceUpdatesFetcher @Inject constructor(
    private val serviceUpdateRepository: ServiceUpdateRepository,
    servicesRepository: ServicesRepository,
    private val serviceNamesComparator: Comparator<String>,
    @ForViewModelCoroutineScope private val viewModelCoroutineScope: CoroutineScope
) : AutoCloseable {

    override fun close() {
        refreshChannel.close()
    }

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits [UiIncident] [UiContent] items. The initial
     * subscription to this may already have loaded data or may cause a new load to happen. To
     * cause new data loads (i.e. to refresh the data), call [refresh].
     */
    val incidentsContentFlow get() = serviceUpdatesFlow
        .combine(serviceColoursFlow, ::FetchedDataHolder)
        .scan<FetchedDataHolder, UiContent<UiIncident>?>(
            initial = null,
            operation = { accumulator, value ->
                calculateUiContent(accumulator, value) {
                    it.toUiIncidentsOrNull(value.coloursForServices)
                }
            }
        )
        .filterNotNull()

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits [UiDiversion] [UiContent] items. The initial
     * subscription to this may already have loaded data or may cause a new load to happen. To
     * cause new data loads (i.e. to refresh the data), call [refresh].
     */
    val diversionsContentFlow get() = serviceUpdatesFlow
        .combine(serviceColoursFlow, ::FetchedDataHolder)
        .scan<FetchedDataHolder, UiContent<UiDiversion>?>(
            initial = null,
            operation = { accumulator, value ->
                calculateUiContent(accumulator, value) {
                    it.toUiDiversionsOrNull(value.coloursForServices)
                }
            }
        )
        .filterNotNull()

    /**
     * Trigger a refresh on the Service Updates data. The new data will be emitted from
     * [incidentsContentFlow] and [diversionsContentFlow]. If this method is called while a load
     * is taking place, it will cause the current load to be aborted a new fresh loading attempt
     * is performed.
     */
    fun refresh() {
        refreshChannel.trySend(Unit)
    }

    private val refreshChannel = Channel<Unit>(capacity = 1).apply { trySend(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val serviceUpdatesFlow = refreshChannel
        .receiveAsFlow()
        .flatMapLatest { serviceUpdateRepository.serviceUpdatesFlow }
        .shareIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.Lazily,
            replay = 1
        )

    private val serviceColoursFlow = servicesRepository
        .getColoursForServicesFlow(null)
        .shareIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0L),
            replay = 1
        )

    /**
     * Map a [List] of [ServiceUpdate]s to a [List] of [UiIncident]s. This will return `null` when
     * this [List] does not contain any incident items.
     *
     * @param coloursForServices A loaded [Map] of service names to [ServiceColours]s. This may be
     * `null` if this data could not be loaded.
     * @return This [List] of [ServiceUpdate]s as a [List] of [UiIncident]s, or `null` when this
     * [List] does not contain any incident items.
     */
    private fun List<ServiceUpdate>.toUiIncidentsOrNull(
        coloursForServices: Map<String, ServiceColours>?
    ): List<UiIncident>? {
        return toUiIncidentsOrNull(coloursForServices, serviceNamesComparator)
    }

    /**
     * Map a [List] of [ServiceUpdate]s to a [List] of [UiDiversion]s. This will return `null` when
     * this [List] does not contain any diversion items.
     *
     * @param coloursForServices A loaded [Map] of service names to [ServiceColours]s. This may be
     * `null` if this data could not be loaded.
     * @return This [List] of [ServiceUpdate]s as a [List] of [UiDiversion]s, or `null` when this
     * [List] does not contain any diversion items.
     */
    private fun List<ServiceUpdate>.toUiDiversionsOrNull(
        coloursForServices: Map<String, ServiceColours>?
    ): List<UiDiversion>? {
        return toUiDiversionsOrNull(coloursForServices, serviceNamesComparator)
    }
}

/**
 * Calculate the new [UiContent] based on [previousContent] (which may be `null` on the first
 * occasion) and the newly [fetchedData].
 *
 * The initial state assumes data is loading. The following table encapsulates the calculation
 * logic;
 *
 * | Old state  | Loaded ServiceUpdateResult | Calculation result       |
 * |------------|----------------------------|--------------------------|
 * | null       | InProgress                 | InProgress               |
 * | null       | Error                      | Error                    |
 * | null       | Success                    | Populated                |
 * | InProgress | InProgress                 | InProgress               |
 * | InProgress | Error                      | Error                    |
 * | InProgress | Success                    | Populated                |
 * | Error      | InProgress                 | InProgress               |
 * | Error      | Error                      | Error                    |
 * | Error      | Success                    | Populated                |
 * | Populated  | InProgress                 | Populated (with refresh) |
 * | Populated  | Error                      | Populated (with error)   |
 * | Populated  | Populated                  | Populated                |
 *
 * (When the ServiceUpdateResult is Success but there are no items to display, i.e. when there are
 * not Service Updates, then the calculation result becomes Error, containing [UiError.EMPTY])
 *
 * The general design of the logic is to always prioritise showing the user some sort of loaded
 * data. So if there was a previous success case but on this loading attempt there was a transient
 * error, we don't want to scrub the data which was loaded before. Instead, we show the Populated
 * content, but we add the error to the Populated object so a more discreet error UI can be shown
 * instead of entirely replacing the content.
 *
 * @param previousContent The previously loaded [UiContent] to use as a context for calculating the
 * new [UiContent]. This may be `null` the first time this method is called.
 * @param fetchedData The newly fetched data to be displayed.
 * @param itemsMapper This lambda is called to convert a [List] of [ServiceUpdate]s to a [List]
 * of the concrete [UiServiceUpdate] type. This is only called in the success scenario.
 * @return The newly calculated [UiContent].
 */
private fun <T : UiServiceUpdate> calculateUiContent(
    previousContent: UiContent<T>?,
    fetchedData: FetchedDataHolder,
    itemsMapper: (List<ServiceUpdate>) -> List<T>?
): UiContent<T> {
    return when (val serviceUpdateResult = fetchedData.serviceUpdateResult) {
        is ServiceUpdatesResult.InProgress -> {
            when (previousContent) {
                null, is UiContent.InProgress, is UiContent.Error -> UiContent.InProgress
                is UiContent.Populated -> previousContent.copy(isRefreshing = true)
            }
        }
        is ServiceUpdatesResult.Success -> {
            serviceUpdateResult
                .serviceUpdates
                ?.let {
                    itemsMapper(it)
                }
                ?.let {
                    UiContent.Populated(
                        isRefreshing = false,
                        items = it,
                        error = null
                    )
                }
                ?: UiContent.Error(
                    isRefreshing = false,
                    error = UiError.EMPTY
                )
        }
        is ServiceUpdatesResult.Error -> {
            val uiError = serviceUpdateResult.toUiError()

            if (previousContent is UiContent.Populated) {
                previousContent.copy(
                    isRefreshing = false,
                    error = uiError
                )
            } else {
                UiContent.Error(
                    isRefreshing = false,
                    error = uiError
                )
            }
        }
    }
}

private data class FetchedDataHolder(
    val serviceUpdateResult: ServiceUpdatesResult,
    val coloursForServices: Map<String, ServiceColours>?
)