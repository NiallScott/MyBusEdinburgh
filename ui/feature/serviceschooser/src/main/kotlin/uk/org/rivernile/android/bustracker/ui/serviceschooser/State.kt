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

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toParcelableServiceDescriptor
import javax.inject.Inject

/**
 * This manages the state for the services chooser.
 *
 * @author Niall Scott
 */
internal interface State {

    /**
     * A [Flow] which emits a [Set] of the currently selected services.
     */
    val selectedServicesFlow: Flow<Set<ServiceDescriptor>>

    /**
     * A [Set] containing the currently selected services.
     */
    val selectedServices: Set<ServiceDescriptor>

    /**
     * A [Flow] which emits whether there are any selected services or not.
     */
    val hasSelectedServicesFlow: Flow<Boolean>

    /**
     * Toggle the selected state of a service, where an unselected service will become selected and
     * a selected service will become unselected.
     */
    fun toggleServiceSelectedState(serviceDescriptor: ServiceDescriptor)

    /**
     * Clear all selected services.
     */
    fun clearAllSelectedServices()
}

internal const val STATE_SELECTED_SERVICES = "selectedServices"

@ViewModelScoped
internal class RealState @Inject constructor(
    private val savedState: SavedStateHandle
) : State {

    private val _selectedServicesFlow = savedState
        .getMutableStateFlow(STATE_SELECTED_SERVICES, defaultSelectedServices)

    override val selectedServicesFlow get() = _selectedServicesFlow
        .map { it?.toSet() ?: emptySet() }

    override val selectedServices get() = _selectedServicesFlow.value?.toSet() ?: emptySet()

    override val hasSelectedServicesFlow get() = _selectedServicesFlow
        .map { !it.isNullOrEmpty() }
        .distinctUntilChanged()

    override fun toggleServiceSelectedState(serviceDescriptor: ServiceDescriptor) {
        _selectedServicesFlow.update {
            val selectedServices = it?.toMutableSet() ?: mutableSetOf()
            val parcelableServiceDescriptor = serviceDescriptor.toParcelableServiceDescriptor()

            if (!selectedServices.add(parcelableServiceDescriptor)) {
                selectedServices -= parcelableServiceDescriptor
            }

            selectedServices
                .ifEmpty { null }
                ?.let(::ArrayList)
        }
    }

    override fun clearAllSelectedServices() {
        _selectedServicesFlow.value = null
    }

    private val defaultSelectedServices get() = savedState
        .get<ServicesChooserParams?>(ARG_PARAMS)
        ?.selectedServices
        ?.takeIf { it.isNotEmpty() }
        ?.let {
            ArrayList(it)
        }
}
