/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This tracks when transient errors have been shown to the user. This is to ensure they are not
 * shown again until a new reload event has occurred.
 *
 * @author Niall Scott
 */
internal interface ServiceUpdatesErrorTracker {

    /**
     * A [Flow] which emits the timestamp of when the last error was shown at.
     */
    val lastErrorTimestampShownFlow: Flow<Long>

    /**
     * Update the timestamp of the last shown error.
     *
     * @param timestamp The timestamp the last error was shown at.
     */
    fun onServiceUpdatesTransientErrorShown(timestamp: Long)
}

private const val KEY_LAST_ERROR_TIMESTAMP = "lastErrorTimestamp"

@ViewModelScoped
internal class RealServiceUpdatesErrorTracker @Inject constructor(
    private val savedState: SavedStateHandle
) : ServiceUpdatesErrorTracker {

    override val lastErrorTimestampShownFlow = savedState
        .getStateFlow(KEY_LAST_ERROR_TIMESTAMP, 0L)

    override fun onServiceUpdatesTransientErrorShown(timestamp: Long) {
        savedState[KEY_LAST_ERROR_TIMESTAMP] = timestamp
    }
}