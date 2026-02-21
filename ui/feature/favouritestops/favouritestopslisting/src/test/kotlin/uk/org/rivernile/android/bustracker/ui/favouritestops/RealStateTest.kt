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
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealState].
 *
 * @author Niall Scott
 */
class RealStateTest {

    @Test
    fun actionIsNullByDefault() = runTest {
        val state = createState()

        state.actionFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
        assertNull(state.action)
    }

    @Test
    fun actionIsMutatedToTheCorrectValue() = runTest {
        val state = createState()

        state.actionFlow.test {
            assertNull(awaitItem())
            state.action = UiAction.ShowStopData(
                stopIdentifier = "123456".toNaptanStopIdentifier()
            )
            assertEquals(
                UiAction.ShowStopData(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                ),
                awaitItem()
            )
            assertEquals(
                UiAction.ShowStopData(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                ),
                state.action
            )
            state.action = null
            assertNull(awaitItem())
            assertNull(state.action)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun selectedStopIdentifierIsNullByDefault() = runTest {
        val state = createState()

        state.selectedStopIdentifierFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
        assertNull(state.selectedStopIdentifier)
    }

    @Test
    fun selectedStopIdentifierIsMutatedToTheCorrectValue() = runTest {
        val state = createState()

        state.selectedStopIdentifierFlow.test {
            assertNull(awaitItem())
            state.selectedStopIdentifier = "123456".toNaptanStopIdentifier()
            assertEquals("123456".toNaptanStopIdentifier(), awaitItem())
            assertEquals("123456".toNaptanStopIdentifier(), state.selectedStopIdentifier)
            state.selectedStopIdentifier = null
            assertNull(awaitItem())
            assertNull(state.selectedStopIdentifier)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun selectedStopIdentifierIsInstantiatedToValueOfSavedState() = runTest {
        val state = createState(
            savedState = SavedStateHandle(
                initialState = mapOf(
                    STATE_SELECTED_STOP_IDENTIFIER to "123456".toParcelableNaptanStopIdentifier()
                )
            )
        )

        state.selectedStopIdentifierFlow.test {
            assertEquals("123456".toNaptanStopIdentifier(), awaitItem())
            ensureAllEventsConsumed()
        }
        assertEquals("123456".toNaptanStopIdentifier(), state.selectedStopIdentifier)
    }

    private fun createState(
        savedState: SavedStateHandle = SavedStateHandle()
    ): RealState {
        return RealState(
            savedState = savedState
        )
    }
}
