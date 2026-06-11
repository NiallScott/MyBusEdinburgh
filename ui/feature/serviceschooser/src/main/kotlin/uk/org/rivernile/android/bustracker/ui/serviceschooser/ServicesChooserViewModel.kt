/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import javax.inject.Inject

/**
 * This is the [ViewModel] for the services chooser.
 *
 * @param state Where state for this screen is held.
 * @param uiContentFetcher Used to fetch [UiContent].
 * @param defaultCoroutineDispatcher The default [CoroutineDispatcher].
 * @param viewModelCoroutineScope The [ViewModel] [CoroutineScope].
 * @author Niall Scott
 */
@HiltViewModel
internal class ServicesChooserViewModel @Inject constructor(
    private val state: State,
    uiContentFetcher: UiContentFetcher,
    @param:ForDefaultDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    /**
     * This emits the current state of the UI.
     */
    val uiStateFlow = uiContentFetcher
        .uiContentFlow
        .combine(state.hasSelectedServicesFlow, ::UiState)
        .flowOn(defaultCoroutineDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState()
        )

    /**
     * The currently selected services.
     */
    val selectedServices get() = state.selectedServices

    /**
     * This should be called when a service has been clicked.
     *
     * @param serviceDescriptor The descriptor of the service which has been clicked.
     */
    fun onServiceClicked(serviceDescriptor: ServiceDescriptor) {
        state.toggleServiceSelectedState(serviceDescriptor)
    }

    /**
     * This should be called when the clear all button has been clicked.
     */
    fun onClearAllClicked() {
        state.clearAllSelectedServices()
    }
}
