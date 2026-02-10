/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.ParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toStopIdentifier
import javax.inject.Inject

/**
 * This class contains the bus times arguments.
 *
 * @param savedState The saved instance state.
 * @author Niall Scott
 */
@ViewModelScoped
class Arguments @Inject constructor(
    savedState: SavedStateHandle
) {

    companion object {

        /**
         * The state key for stop code.
         */
        const val STATE_STOP_IDENTIFIER = "stopIdentifier"
    }

    /**
     * This [kotlinx.coroutines.flow.Flow] emits the stop code argument.
     */
    val stopIdentifierFlow: Flow<StopIdentifier?> = savedState
        .getStateFlow<ParcelableStopIdentifier?>(STATE_STOP_IDENTIFIER, null)
        .map { it?.toStopIdentifier() }
}
