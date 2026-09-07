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

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
            state.action = UiAction.ShowLocationSettings
            assertEquals(
                UiAction.ShowLocationSettings,
                awaitItem()
            )
            assertEquals(
                UiAction.ShowLocationSettings,
                state.action
            )
            state.action = null
            assertNull(awaitItem())
            assertNull(state.action)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isResumedHasDefaultValue() = runTest {
        val state = createState()

        state.isResumedFlow.test {
            assertFalse(awaitItem())
            assertFalse(state.isResumed)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isResumedIsMutatedToTheCorrectValue() = runTest {
        val state = createState()

        state.isResumedFlow.test {
            assertFalse(awaitItem())
            assertFalse(state.isResumed)
            state.isResumed = true
            assertTrue(awaitItem())
            assertTrue(state.isResumed)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateHasDefaultValue() = runTest {
        val state = createState()

        state.permissionsStateFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateIsMutatedToTheCorrectValue() = runTest {
        val state = createState()
        val grantedState = PermissionsState(
            fineLocationPermission = PermissionState.GRANTED,
            coarseLocationPermission = PermissionState.GRANTED
        )

        state.permissionsStateFlow.test {
            assertNull(awaitItem())
            state.permissionsState = grantedState
            assertEquals(grantedState, awaitItem())
            assertEquals(grantedState, state.permissionsState)
            state.permissionsState = PermissionsState()
            assertEquals(PermissionsState(), awaitItem())
            assertEquals(PermissionsState(), state.permissionsState)
            ensureAllEventsConsumed()
        }
    }

    private fun createState(): RealState {
        return RealState()
    }
}
