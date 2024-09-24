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

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * This class holds state for [IncidentsViewModel].
 *
 * @author Niall Scott
 */
@ViewModelScoped
internal class IncidentsViewModelState @Inject constructor() {

    /**
     * This [Flow] emits [UiAction]s to be performed, usually in response to the user's actions.
     */
    val actionFlow: Flow<UiAction?> get() = _actionFlow
    private val _actionFlow = MutableStateFlow<UiAction?>(null)

    /**
     * The current [UiAction] in progress.
     */
    var action: UiAction?
        get() = _actionFlow.value
        set(value) {
            _actionFlow.value = value
        }
}