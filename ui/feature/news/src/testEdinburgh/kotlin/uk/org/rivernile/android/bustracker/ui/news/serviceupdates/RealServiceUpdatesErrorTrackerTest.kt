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
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val KEY_LAST_ERROR_TIMESTAMP = "lastErrorTimestamp"

/**
 * Tests for [RealServiceUpdatesErrorTracker].
 *
 * @author Niall Scott
 */
class RealServiceUpdatesErrorTrackerTest {

    @Test
    fun lastErrorTimestampShownFlowEmitsZeroByDefault() = runTest {
        val tracker = createTracker()

        tracker.lastErrorTimestampShownFlow.test {
            assertEquals(0L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun lastErrorTimestampShownFlowEmitsPreviouslySavedValue() = runTest {
        val tracker = createTracker(
            SavedStateHandle(
                mapOf(
                    KEY_LAST_ERROR_TIMESTAMP to 123L
                )
            )
        )

        tracker.lastErrorTimestampShownFlow.test {
            assertEquals(123L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onServiceUpdatesTransientErrorShownTimestampUpdatesValue() = runTest {
        val tracker = createTracker()

        tracker.onServiceUpdatesTransientErrorShown(123L)

        tracker.lastErrorTimestampShownFlow.test {
            assertEquals(123L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    private fun createTracker(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        RealServiceUpdatesErrorTracker(savedStateHandle)
}