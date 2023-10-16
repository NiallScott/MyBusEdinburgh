/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import javax.inject.Inject

/**
 * This is the [ViewModel] for [ServicesChooserDialogFragment].
 *
 * @param arguments The arguments sent in to [ServicesChooserDialogFragment].
 * @param state State tracking for this instance.
 * @param servicesLoader Used to load services and combine them with their current selected state.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class ServicesChooserDialogFragmentViewModel @Inject constructor(
    arguments: Arguments,
    private val state: State,
    servicesLoader: ServicesLoader,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        /**
         * State key for the parameters.
         */
        const val STATE_PARAMS = Arguments.STATE_PARAMS
    }

    private val servicesFlow = servicesLoader
        .servicesFlow
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .onStart<List<UiService>?> { emit(null) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This emits the services to be displayed in the chooser along with their state.
     */
    val servicesLiveData = servicesFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This emits the current [UiState].
     */
    val uiStateLiveData =
        combine(
            arguments.paramsFlow,
            servicesFlow,
            this::mapToUiState)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This emits whether the clear all button should be enabled or not.
     */
    val isClearAllButtonEnabledLiveData = state
        .hasSelectedServicesFlow
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This field exposes the current selected services.
     */
    val selectedServices get() = state.selectedServices

    /**
     * This is called when a service has been clicked.
     *
     * @param serviceName The name of the service which was clicked.
     */
    fun onServiceClicked(serviceName: String) {
        state.onServiceClicked(serviceName)
    }

    /**
     * This is called when the 'Clear all' button has been clicked.
     */
    fun onClearAllClicked() {
        state.onClearAllClicked()
    }

    /**
     * Given the supplied [params] and [services], map this to the current [UiState].
     *
     * @param params The params sent to to this [ViewModel].
     * @param services The [List] of [UiService]s.
     * @return The mapped [UiState].
     */
    private fun mapToUiState(
        params: ServicesChooserParams?,
        services: List<UiService>?): UiState {
        return when {
            services == null -> UiState.PROGRESS
            services.isEmpty() -> {
                if (params is ServicesChooserParams.Stop) {
                    UiState.ERROR_NO_SERVICES_STOP
                } else {
                    UiState.ERROR_NO_SERVICES_GLOBAL
                }
            }
            else -> UiState.CONTENT
        }
    }
}