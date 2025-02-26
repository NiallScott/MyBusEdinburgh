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

package uk.org.rivernile.android.bustracker.ui.about

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val STATE_IS_CREDITS_SHOWN = "isCreditsShown"
private const val STATE_IS_OPEN_SOURCE_LICENCES_SHOWN = "isOpenSourceLicencesShown"

/**
 * Tests for [RealAboutViewModelState].
 *
 * @author Niall Scott
 */
class RealAboutViewModelStateTest {

    @Test
    fun isCreditsShownFlowEmitsFalseByDefault() = runTest {
        val state = createState()

        state.isCreditsShownFlow.test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isCreditsShownFlowEmitsTrueWhenSetToTrueInSavedState() = runTest {
        val state = createState(
            SavedStateHandle(
                mapOf(STATE_IS_CREDITS_SHOWN to true)
            )
        )

        state.isCreditsShownFlow.test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isCreditsShownFlowEmitsNewValuesAsPropertyIsChanged() = runTest {
        val state = createState()

        state.isCreditsShownFlow.test {
            assertFalse(awaitItem())
            assertFalse(state.isCreditsShown)
            state.isCreditsShown = true
            assertTrue(awaitItem())
            assertTrue(state.isCreditsShown)
            state.isCreditsShown = false
            assertFalse(awaitItem())
            assertFalse(state.isCreditsShown)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isOpenSourceLicencesFlowEmitsFalseByDefault() = runTest {
        val state = createState()

        state.isOpenSourceLicencesShownFlow.test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isOpenSourceLicencesFlowEmitsTrueWhenSetToTrueInSavedState() = runTest {
        val state = createState(
            SavedStateHandle(
                mapOf(STATE_IS_OPEN_SOURCE_LICENCES_SHOWN to true)
            )
        )

        state.isOpenSourceLicencesShownFlow.test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isOpenSourceLicencesFlowEmitsNewValuesAsPropertyIsChanged() = runTest {
        val state = createState()

        state.isOpenSourceLicencesShownFlow.test {
            assertFalse(awaitItem())
            assertFalse(state.isOpenSourceLicencesShown)
            state.isOpenSourceLicencesShown = true
            assertTrue(awaitItem())
            assertTrue(state.isOpenSourceLicencesShown)
            state.isOpenSourceLicencesShown = false
            assertFalse(awaitItem())
            assertFalse(state.isOpenSourceLicencesShown)
            ensureAllEventsConsumed()
        }
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
    fun actionFlowEmitsNewValuesAsPropertyIsChanged() = runTest {
        val state = createState()

        state.actionFlow.test {
            assertNull(awaitItem())
            assertNull(state.action)
            state.action = UiAction.ShowAppWebsite
            assertEquals(UiAction.ShowAppWebsite, awaitItem())
            assertEquals(UiAction.ShowAppWebsite, state.action)
            state.action = UiAction.ShowPrivacyPolicy
            assertEquals(UiAction.ShowPrivacyPolicy, awaitItem())
            assertEquals(UiAction.ShowPrivacyPolicy, state.action)
            ensureAllEventsConsumed()
        }
    }

    private fun createState(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): RealAboutViewModelState {
        return RealAboutViewModelState(savedStateHandle)
    }
}