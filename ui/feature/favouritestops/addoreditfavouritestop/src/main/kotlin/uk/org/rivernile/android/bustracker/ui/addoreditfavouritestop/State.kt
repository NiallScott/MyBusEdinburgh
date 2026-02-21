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

package uk.org.rivernile.android.bustracker.ui.addoreditfavouritestop

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * This exposes any held state.
 *
 * @author Niall Scott
 */
internal interface State {

    /**
     * The current value of the user-supplied name for the stop.
     */
    var stopNameText: String?

    /**
     * A [Flow] which emits the current value of the user-supplied name for the stop.
     */
    val stopNameTextFlow: Flow<String?>

    /**
     * The current [UiAction] to be performed.
     */
    var action: UiAction?

    /**
     * A [Flow] which emits the current [UiAction] to be performed.
     */
    val actionFlow: Flow<UiAction?>
}

@ViewModelScoped
internal class RealState @Inject constructor() : State {

    private val _stopNameTextFlow = MutableStateFlow<String?>(null)
    private val _actionFlow = MutableStateFlow<UiAction?>(null)

    override var stopNameText: String?
        get() = _stopNameTextFlow.value
        set(value) {
            _stopNameTextFlow.value = value
        }

    override val stopNameTextFlow get() = _stopNameTextFlow.asStateFlow()

    override var action: UiAction?
        get() = _actionFlow.value
        set(value) {
            _actionFlow.value = value
        }

    override val actionFlow get() = _actionFlow.asStateFlow()
}
