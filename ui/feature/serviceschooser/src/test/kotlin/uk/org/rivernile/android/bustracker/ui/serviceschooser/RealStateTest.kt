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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.ParcelableServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import kotlin.collections.emptySet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [RealState].
 *
 * @author Niall Scott
 */
class RealStateTest {

    @Test
    fun selectedServicesFlowEmitsEmptySetByDefault() = runTest {
        val state = createState()

        state.selectedServicesFlow.test {
            assertEquals(emptySet(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun selectedServicesFlowEmitsEmptySetWhenItemsAreEmpty() = runTest {
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList<ParcelableServiceDescriptor>()
                )
            )
        )

        state.selectedServicesFlow.test {
            assertEquals(emptySet(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun selectedServicesFlowEmitsItemsFromSavedState() = runTest {
        val items = setOf(
            ParcelableServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            ParcelableServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            )
        )
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList(items)
                )
            )
        )

        state.selectedServicesFlow.test {
            assertEquals(items, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun selectedServicesReturnsEmptySetByDefault() {
        val state = createState()

        val result = state.selectedServices

        assertEquals(emptySet(), result)
    }

    @Test
    fun selectedServicesReturnsEmptySetWhenItemsAreEmpty() {
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList<ParcelableServiceDescriptor>()
                )
            )
        )

        val result = state.selectedServices

        assertEquals(emptySet(), result)
    }

    @Test
    fun selectedServicesReturnsItemsFromSavedState() = runTest {
        val items = setOf(
            ParcelableServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            ParcelableServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            )
        )
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList(items)
                )
            )
        )

        val result = state.selectedServices

        assertEquals(items, result)
    }

    @Test
    fun hasSelectedServicesFlowEmitsFalseByDefault() = runTest {
        val state = createState()

        state.hasSelectedServicesFlow.test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun hasSelectedServicesFlowEmitsFalseWhenItemsAreEmpty() = runTest {
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList<ParcelableServiceDescriptor>()
                )
            )
        )

        state.hasSelectedServicesFlow.test {
            assertFalse(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun hasSelectedServicesFlowEmitsTrueWhenSavedStateHasItems() = runTest {
        val items = setOf(
            ParcelableServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            )
        )
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList(items)
                )
            )
        )

        state.hasSelectedServicesFlow.test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun toggleServiceSelectedStateCausesStateToBeAsExpected() = runTest {
        val state = createState()
        val service1 = ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val service2 = ServiceDescriptor(
            serviceName = "2",
            operatorCode = "TEST2"
        )

        state.selectedServicesFlow.test {
            assertEquals(emptySet(), awaitItem())

            state.toggleServiceSelectedState(service1)
            assertEquals(
                setOf(service1),
                awaitItem()
            )

            state.toggleServiceSelectedState(service2)
            assertEquals(
                setOf(service1, service2),
                awaitItem()
            )

            state.toggleServiceSelectedState(service1)
            assertEquals(
                setOf(service2),
                awaitItem()
            )

            state.toggleServiceSelectedState(service2)
            assertEquals(emptySet(), awaitItem())

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun toggleServiceSelectedStateWithDefaultStateCausesStateToBeAsExpected() = runTest {
        val service1 = ParcelableServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val service2 = ParcelableServiceDescriptor(
            serviceName = "2",
            operatorCode = "TEST2"
        )
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList(setOf(service1, service2))
                )
            )
        )

        state.selectedServicesFlow.test {
            assertEquals(
                setOf(service1, service2),
                awaitItem()
            )

            state.toggleServiceSelectedState(service1)
            assertEquals(
                setOf(service2),
                awaitItem()
            )

            state.toggleServiceSelectedState(service2)
            assertEquals(emptySet(), awaitItem())

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun clearAllSelectedServicesHasNoEffectWhenServicesIsEmpty() = runTest {
        val state = createState()

        state.selectedServicesFlow.test {
            assertEquals(emptySet(), awaitItem())

            state.clearAllSelectedServices()
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun clearAllSelectedServicesClearsServicesWhenHasDefaultServices() = runTest {
        val service1 = ParcelableServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val state = createState(
            savedState = SavedStateHandle(
                mapOf(
                    STATE_SELECTED_SERVICES to ArrayList(setOf(service1))
                )
            )
        )

        state.selectedServicesFlow.test {
            assertEquals(
                setOf(service1),
                awaitItem()
            )

            state.clearAllSelectedServices()
            assertEquals(emptySet(), awaitItem())

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun clearAllSelectedServicesClearsAllAddedServices() = runTest {
        val service1 = ParcelableServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val state = createState()
        state.toggleServiceSelectedState(service1)

        state.selectedServicesFlow.test {
            assertEquals(
                setOf(service1),
                awaitItem()
            )

            state.clearAllSelectedServices()
            assertEquals(emptySet(), awaitItem())

            ensureAllEventsConsumed()
        }
    }

    private fun createState(
        savedState: SavedStateHandle = SavedStateHandle()
    ): RealState {
        return RealState(
            savedState = savedState
        )
    }
}
