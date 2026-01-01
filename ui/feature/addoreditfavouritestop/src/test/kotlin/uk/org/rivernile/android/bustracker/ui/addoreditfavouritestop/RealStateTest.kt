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

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
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
    fun stopNameTextIsNullByDefault() {
        val state = createState()

        assertNull(state.stopNameText)
    }

    @Test
    fun stopNameTextValueIsSetValue() {
        val state = createState()

        state.stopNameText = "Stop Name"

        assertEquals("Stop Name", state.stopNameText)
    }

    @Test
    fun stopNameTextFlowEmitsNullByDefault() = runTest {
        val state = createState()

        state.stopNameTextFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopNameTextFlowEmitsSetValue() = runTest {
        val state = createState()

        state.stopNameTextFlow.test {
            assertNull(awaitItem())

            state.stopNameText = "Stop Name"
            assertEquals("Stop Name", awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun actionIsNullByDefault() {
        val state = createState()

        assertNull(state.action)
    }

    @Test
    fun actionIsSetValue() {
        val state = createState()

        state.action = UiAction.DismissDialog

        assertEquals(UiAction.DismissDialog, state.action)
    }

    @Test
    fun actionFlowEmitsNullByDefault() = runTest {
        val state = createState()

        state.actionFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun actionFlowEmitsSetValue() = runTest {
        val state = createState()

        state.actionFlow.test {
            assertNull(awaitItem())

            state.action = UiAction.DismissDialog
            assertEquals(UiAction.DismissDialog, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    private fun createState(): RealState = RealState()
}
