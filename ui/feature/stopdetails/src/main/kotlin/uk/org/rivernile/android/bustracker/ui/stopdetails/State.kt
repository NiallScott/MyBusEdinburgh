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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

/**
 * This provides the way to read and write any transient state for the stop details feature.
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
     * This emits the current [PermissionsState].
     */
    val permissionsStateFlow: Flow<PermissionsState?>

    /**
     * The current [PermissionsState].
     */
    var permissionsState: PermissionsState?
}

@ViewModelScoped
internal class RealState @Inject constructor() : State {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val actionFlow get() = _actionFlow.asFlow()

    override var action: UiAction?
        get() = _actionFlow.value
        set(value) {
            _actionFlow.value = value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val permissionsStateFlow get() = _permissionsFlow.asFlow()

    override var permissionsState: PermissionsState?
        get() = _permissionsFlow.value
        set(value) {
            _permissionsFlow.value = value
        }

    private val _actionFlow = MutableStateFlow<UiAction?>(null)

    private val _permissionsFlow = MutableStateFlow<PermissionsState?>(null)
}
