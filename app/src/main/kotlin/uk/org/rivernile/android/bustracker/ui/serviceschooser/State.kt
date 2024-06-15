/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
/**
 * This class manages stateful items within [ServicesChooserDialogFragmentViewModel].
 *
 * @param savedState The saved instance state.
 * @author Niall Scott
 */
@ViewModelScoped
class State @Inject constructor(
    private val savedState: SavedStateHandle
) {

    companion object {

        private const val STATE_SELECTED_SERVICES = "selectedServices"
    }

    private val selectedServicesFlowInternal: Flow<ArrayList<String>?>

    /**
     * This [Flow] emits the current [List] of selected service names.
     */
    val selectedServicesFlow get() = selectedServicesFlowInternal
        .map { it?.toSet() ?: emptySet() }

    /**
     * This [Flow] emits whether we have any selected services or not.
     */
    val hasSelectedServicesFlow get() = selectedServicesFlowInternal
        .map { !it.isNullOrEmpty() }
        .distinctUntilChanged()

    /**
     * This field exposes the selected services.
     */
    val selectedServices: ArrayList<String>? get() =
        savedState.get<ArrayList<String>?>(STATE_SELECTED_SERVICES)
            ?.ifEmpty { null }

    init {
        val defaultSelectedServices = savedState
            .get<ServicesChooserParams?>(Arguments.STATE_PARAMS)
            ?.selectedServices
            ?.takeIf { it.isNotEmpty() }
            ?.let(::ArrayList)

        selectedServicesFlowInternal = savedState
            .getStateFlow<ArrayList<String>?>(
                STATE_SELECTED_SERVICES,
                defaultSelectedServices
            )
    }

    /**
     * This is called when a service has been clicked. If the service is already marked as selected,
     * it will be removed. If it's not marked as selected, it will be added.
     *
     * @param serviceName The service which was clicked.
     */
    fun onServiceClicked(serviceName: String) {
        val selectedServices = savedState
            .get<ArrayList<String>?>(STATE_SELECTED_SERVICES)
            ?.toMutableSet()
            ?: mutableSetOf()

        if (!selectedServices.add(serviceName)) {
            selectedServices -= serviceName
        }

        savedState[STATE_SELECTED_SERVICES] = selectedServices
            .ifEmpty { null }
            ?.let(::ArrayList)
    }

    /**
     * This is called when the 'Clear all' button has been clicked.
     */
    fun onClearAllClicked() {
        savedState[STATE_SELECTED_SERVICES] = null
    }
}

