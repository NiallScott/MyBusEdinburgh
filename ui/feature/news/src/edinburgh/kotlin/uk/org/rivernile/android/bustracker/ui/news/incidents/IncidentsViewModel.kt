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

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import javax.inject.Inject

/**
 * The [ViewModel] for displaying current service incident data to users.
 *
 * @param state A holder for the [ViewModel] state.
 * @param uiContentFetcher An implementation used to fetch [UiContent].
 * @param viewModelCoroutineScope The [CoroutineScope] to use in this [ViewModel].
 * @author Niall Scott
 */
@HiltViewModel
internal class IncidentsViewModel @Inject constructor(
    private val state: IncidentsViewModelState,
    private val uiContentFetcher: UiContentFetcher,
    @ForDefaultDispatcher defaultCoroutineDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    /**
     * This [kotlinx.coroutines.flow.Flow] emits the current [UiState].
     */
    val uiStateFlow = combinedUiStateFlow
        .flowOn(defaultCoroutineDispatcher)
        .stateIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState(content = UiContent.InProgress)
        )

    /**
     * This is called when the more details button has been clicked.
     *
     * @param item The item which has been clicked.
     */
    fun onMoreDetailsButtonClicked(item: UiIncident) {
        item.url?.ifBlank { null }?.let {
            state.action = UiAction.ShowUrl(it)
        }
    }

    /**
     * This is called when an action has been launched.
     */
    fun onActionLaunched() {
        state.action = null
    }

    private val combinedUiStateFlow get() =
        combine(
            uiContentFetcher.incidentsContentFlow,
            state.actionFlow,
            ::UiState
        )
}