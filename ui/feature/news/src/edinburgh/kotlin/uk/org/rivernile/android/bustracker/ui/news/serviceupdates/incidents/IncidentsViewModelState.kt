/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * This holds ViewModel state for incidents.
 *
 * @author Niall Scott
 */
internal interface IncidentsViewModelState {

    /**
     * This [Flow] emits [UiIncidentAction]s to be performed, usually in response to the user's
     * actions.
     */
    val actionFlow: Flow<UiIncidentAction?>

    /**
     * The current [UiIncidentAction] in progress.
     */
    var action: UiIncidentAction?
}

@ViewModelScoped
internal class RealIncidentsViewModelState @Inject constructor() : IncidentsViewModelState {

    override val actionFlow: Flow<UiIncidentAction?> get() = _actionFlow.asStateFlow()
    private val _actionFlow = MutableStateFlow<UiIncidentAction?>(null)

    override var action: UiIncidentAction?
        get() = _actionFlow.value
        set(value) {
            _actionFlow.value = value
        }
}