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

package uk.org.rivernile.android.bustracker.ui.search

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import uk.org.rivernile.android.bustracker.core.domain.ParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toStopIdentifier
import javax.inject.Inject

/**
 * This provides the way to read and write any transient state for the search feature.
 *
 * @author Niall Scott
 */
internal interface State {

    /**
     * This emits the current [UiAction] to be performed. `null` will be emitted when no action is
     * to be performed.
     */
    val actionFlow: Flow<UiAction?>

    /**
     * The current [UiAction] in progress, if any.
     */
    var action: UiAction?

    /**
     * This emits the current search term.
     */
    val searchTermFlow: Flow<String?>

    /**
     * The current search term.
     */
    var searchTerm: String?

    /**
     * A [Flow] which emits the currently selected stop identifier.
     */
    val selectedStopIdentifierFlow: Flow<StopIdentifier?>

    /**
     * A property which gets and sets the currently selected stop identifier.
     */
    var selectedStopIdentifier: StopIdentifier?

    /**
     * Atomically update the selected stop identifier based on the current value. The supplied
     * lambda may be called more than once if the update is attempted while another write is taking
     * place.
     *
     * @param function The update function. The input is the existing set value for the
     * [StopIdentifier] and the value which is returned from the lambda is the new value.
     */
    fun updateSelectedStopIdentifier(function: (StopIdentifier?) -> StopIdentifier?)
}

internal const val STATE_SEARCH_TERM = "searchTerm"
internal const val STATE_SELECTED_STOP_IDENTIFIER = "selectedStopIdentifier"

@ViewModelScoped
internal class RealState @Inject constructor(
    savedState: SavedStateHandle
) : State {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val actionFlow get() = _actionFlow.asFlow()

    override var action: UiAction?
        get() = _actionFlow.value
        set(value) {
            _actionFlow.value = value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchTermFlow get() = _searchTermFlow.asFlow()

    override var searchTerm: String?
        get() = _searchTermFlow.value
        set(value) {
            _searchTermFlow.value = value
        }

    override val selectedStopIdentifierFlow get() = _selectedStopIdentifierFlow
        .map { it?.toStopIdentifier() }

    override var selectedStopIdentifier: StopIdentifier?
        get() = _selectedStopIdentifierFlow.value?.toStopIdentifier()
        set(value) {
            _selectedStopIdentifierFlow.value = value?.toParcelableStopIdentifier()
        }

    override fun updateSelectedStopIdentifier(function: (StopIdentifier?) -> StopIdentifier?) {
        _selectedStopIdentifierFlow.update {
            function(it?.toStopIdentifier())
                ?.toParcelableStopIdentifier()
        }
    }

    private val _actionFlow = MutableStateFlow<UiAction?>(null)

    private val _searchTermFlow = savedState
        .getMutableStateFlow<String?>(
            key = STATE_SEARCH_TERM,
            initialValue = null
        )

    private val _selectedStopIdentifierFlow = savedState
        .getMutableStateFlow<ParcelableStopIdentifier?>(
            key = STATE_SELECTED_STOP_IDENTIFIER,
            initialValue = null
        )
}
