/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.turbineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [State].
 *
 * @author Niall Scott
 */
class StateTest {

    @Test
    fun selectedServicesFlowInitiallyEmitsEmptySetWhenArgumentsNotSet() = runTest {
        val state = State(SavedStateHandle())

        val result = state.selectedServicesFlow.first()

        assertEquals(emptySet(), result)
    }

    @Test
    fun selectedServicesFlowInitiallyEmitsEmptySetWhenArgumentsHaveEmptySelectedServices() =
        runTest {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        emptyList())
                )
            )
        )

        val result = state.selectedServicesFlow.first()

        assertEquals(emptySet(), result)
    }

    @Test
    fun selectedServicesFlowInitiallyEmitsSelectedServicesWhenSetInArguments() = runTest {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        listOf("1", "2", "3"))
                )
            )
        )
        val expected = setOf("1", "2", "3")

        val result = state.selectedServicesFlow.first()

        assertEquals(expected, result)
    }

    @Test
    fun hasSelectedServicesFlowInitiallyEmitsFalseWhenArgumentsNotSet() = runTest {
        val state = State(SavedStateHandle())

        val result = state.hasSelectedServicesFlow.first()

        assertFalse(result)
    }

    @Test
    fun hasSelectedServicesFlowInitiallyEmitsFalseWhenArgumentsHaveEmptySelectedServices() =
        runTest {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        emptyList())
                )
            )
        )

        val result = state.hasSelectedServicesFlow.first()

        assertFalse(result)
    }

    @Test
    fun hasSelectedServicesFlowInitiallyEmitsTrueWhenSetInArguments() = runTest {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        listOf("1", "2", "3"))
                )
            )
        )

        val result = state.hasSelectedServicesFlow.first()

        assertTrue(result)
    }

    @Test
    fun selectedServicesReturnsNullWhenArgumentsNotSet() {
        val state = State(SavedStateHandle())

        val result = state.selectedServices

        assertNull(result)
    }

    @Test
    fun selectedServicesReturnsNullWhenArgumentsHaveEmptySelectedServices() {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        emptyList())
                )
            )
        )

        val result = state.selectedServices

        assertNull(result)
    }

    @Test
    fun selectedServicesReturnsSelectedServicesWhenSetInArguments() {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        listOf("1", "2", "3"))
                )
            )
        )
        val expected = arrayListOf("1", "2", "3")

        val result = state.selectedServices

        assertEquals(expected, result)
    }

    @Test
    fun onServiceClickedWithServiceAddsService() {
        val state = State(SavedStateHandle())
        val expected = arrayListOf("1")

        state.onServiceClicked("1")

        assertEquals(expected, state.selectedServices)
    }

    @Test
    fun onServiceClickedWhenServiceAlreadyAddedRemovesService() {
        val state = State(SavedStateHandle())

        state.onServiceClicked("1")
        state.onServiceClicked("1")

        assertNull(state.selectedServices)
    }

    @Test
    fun onServiceClickedWhenHasServicesInArgsAddsServiceWhenServiceNotAlreadyAdded() {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        listOf("1", "2", "3"))
                )
            )
        )
        val expected = arrayListOf("1", "2", "3", "4")

        state.onServiceClicked("4")

        assertEquals(expected, state.selectedServices)
    }

    @Test
    fun onServiceClickedWhenHasServicesInArgsRemovesServiceWhenServiceAlreadyAdded() {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        listOf("1", "2", "3"))
                )
            )
        )
        val expected = arrayListOf("1", "3")

        state.onServiceClicked("2")

        assertEquals(expected, state.selectedServices)
    }

    @Test
    fun onClearAllClickedClearsAllServicesAfterServicesClicked() {
        val state = State(SavedStateHandle())

        state.onServiceClicked("1")
        state.onClearAllClicked()

        assertNull(state.selectedServices)
    }

    @Test
    fun onClearAllClickedClearsAllServicesFromArgs() {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        listOf("1", "2", "3"))
                )
            )
        )

        state.onClearAllClicked()

        assertNull(state.selectedServices)
    }

    @Test
    fun representativeExample1() = runTest {
        val state = State(SavedStateHandle())

        turbineScope {
            val selectedServicesTurbine = state.selectedServicesFlow.testIn(backgroundScope)
            val hasSelectedServicesTurbine = state.hasSelectedServicesFlow.testIn(backgroundScope)
            state.onServiceClicked("1")
            state.onServiceClicked("2")
            state.onServiceClicked("3")
            state.onServiceClicked("2")
            state.onClearAllClicked()
            state.onServiceClicked("3")

            assertEquals(emptySet(), selectedServicesTurbine.awaitItem())
            assertEquals(setOf("1"), selectedServicesTurbine.awaitItem())
            assertEquals(setOf("1", "2"), selectedServicesTurbine.awaitItem())
            assertEquals(setOf("1", "2", "3"), selectedServicesTurbine.awaitItem())
            assertEquals(setOf("1", "3"), selectedServicesTurbine.awaitItem())
            assertEquals(emptySet(), selectedServicesTurbine.awaitItem())
            assertEquals(setOf("3"), selectedServicesTurbine.awaitItem())
            selectedServicesTurbine.ensureAllEventsConsumed()

            assertFalse(hasSelectedServicesTurbine.awaitItem())
            assertTrue(hasSelectedServicesTurbine.awaitItem())
            assertFalse(hasSelectedServicesTurbine.awaitItem())
            assertTrue(hasSelectedServicesTurbine.awaitItem())
            hasSelectedServicesTurbine.ensureAllEventsConsumed()
        }
    }

    @Test
    fun representativeExample2() = runTest {
        val state = State(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to ServicesChooserParams.AllServices(
                        0,
                        listOf("1", "2", "3"))
                )
            )
        )

        turbineScope {
            val selectedServicesTurbine = state.selectedServicesFlow.testIn(backgroundScope)
            val hasSelectedServicesTurbine = state.hasSelectedServicesFlow.testIn(backgroundScope)
            state.onServiceClicked("2")
            state.onClearAllClicked()
            state.onServiceClicked("3")

            assertEquals(setOf("1", "2", "3"), selectedServicesTurbine.awaitItem())
            assertEquals(setOf("1", "3"), selectedServicesTurbine.awaitItem())
            assertEquals(emptySet(), selectedServicesTurbine.awaitItem())
            assertEquals(setOf("3"), selectedServicesTurbine.awaitItem())
            selectedServicesTurbine.ensureAllEventsConsumed()

            assertTrue(hasSelectedServicesTurbine.awaitItem())
            assertFalse(hasSelectedServicesTurbine.awaitItem())
            assertTrue(hasSelectedServicesTurbine.awaitItem())
            hasSelectedServicesTurbine.ensureAllEventsConsumed()
        }
    }
}