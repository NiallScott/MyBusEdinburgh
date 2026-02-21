/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.ParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toStopIdentifier
import javax.inject.Inject

/**
 * This provides way to read and write any transient state for the favourite stops feature.
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
     * A [Flow] which emits the currently selected stop identifier.
     */
    val selectedStopIdentifierFlow: Flow<StopIdentifier?>

    /**
     * A property which gets and sets the currently selected stop identifier.
     */
    var selectedStopIdentifier: StopIdentifier?
}

internal const val STATE_SELECTED_STOP_IDENTIFIER = "selectedStopIdentifier"

@ViewModelScoped
internal class RealState @Inject constructor(
    private val savedState: SavedStateHandle
) : State {

    private val _actionFlow = MutableStateFlow<UiAction?>(null)

    override val actionFlow get() = _actionFlow.asStateFlow()

    override var action: UiAction?
        get() = _actionFlow.value
        set(value) {
            _actionFlow.value = value
        }

    override val selectedStopIdentifierFlow get() = _selectedStopIdentifierFlow
        .map { it?.toStopIdentifier() }

    override var selectedStopIdentifier: StopIdentifier?
        get() = savedState
            .get<ParcelableStopIdentifier>(STATE_SELECTED_STOP_IDENTIFIER)
            ?.toStopIdentifier()
        set(value) {
            savedState[STATE_SELECTED_STOP_IDENTIFIER] = value?.toParcelableStopIdentifier()
        }

    private val _selectedStopIdentifierFlow = savedState
        .getStateFlow<ParcelableStopIdentifier?>(
            key = STATE_SELECTED_STOP_IDENTIFIER,
            initialValue = null
        )
}
