/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ParcelableServiceDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ExpandedServicesTracker].
 *
 * @author Niall Scott
 */
class ExpandedServicesTrackerTest {

    companion object {

        private const val STATE_KEY_EXPANDED_SERVICES = "expandedServices"
    }

    @Test
    fun initialStateWithNoPreviousStateIsEmpty() = runTest {
        val handle = SavedStateHandle()
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            assertEquals(emptySet(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun initialStateWithEmptyPreviousStateIsEmpty() = runTest {
        val handle = SavedStateHandle(
            mapOf(STATE_KEY_EXPANDED_SERVICES to arrayListOf<ParcelableServiceDescriptor>())
        )
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            assertEquals(emptySet(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun initialStateWithPopulatedServicesIsNonEmpty() = runTest {
        val handle = SavedStateHandle(
            mapOf(
                STATE_KEY_EXPANDED_SERVICES to arrayListOf(
                    parcelableService1,
                    parcelableService2,
                    parcelableService3
                )
            )
        )
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            assertEquals(setOf(service1, service2, service3), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onServiceClickedAfterEmptyInitialStateAddsService() = runTest {
        val handle = SavedStateHandle()
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            tracker.onServiceClicked(service1)

            assertEquals(emptySet(), awaitItem())
            assertEquals(setOf(service1), awaitItem())
            ensureAllEventsConsumed()
        }
        assertEquals(listOf(parcelableService1), handle[STATE_KEY_EXPANDED_SERVICES])
    }

    @Test
    fun onServiceClickedAfterPopulatedInitialStateRemovesService() = runTest {
        val handle = SavedStateHandle(
                mapOf(STATE_KEY_EXPANDED_SERVICES to arrayListOf(parcelableService1))
        )
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            tracker.onServiceClicked(service1)

            assertEquals(setOf(service1), awaitItem())
            assertEquals(emptySet(), awaitItem())
            ensureAllEventsConsumed()
        }
        assertEquals(
            arrayListOf<ParcelableServiceDescriptor>(),
            handle[STATE_KEY_EXPANDED_SERVICES]
        )
    }

    @Test
    fun onServiceClickedWithMultipleRandomInteractionsYieldsCorrectState() = runTest {
        val handle = SavedStateHandle(
            mapOf(STATE_KEY_EXPANDED_SERVICES to arrayListOf(parcelableService1))
        )
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            tracker.onServiceClicked(service2)
            tracker.onServiceClicked(service3)
            tracker.onServiceClicked(service1)
            tracker.onServiceClicked(service4)
            tracker.onServiceClicked(service4)
            tracker.onServiceClicked(service4)
            tracker.onServiceClicked(service2)

            assertEquals(setOf(service1), awaitItem())
            assertEquals(setOf(service1, service2), awaitItem())
            assertEquals(setOf(service1, service2, service3), awaitItem())
            assertEquals(setOf(service2, service3), awaitItem())
            assertEquals(setOf(service2, service3, service4), awaitItem())
            assertEquals(setOf(service2, service3), awaitItem())
            assertEquals(setOf(service2, service3, service4), awaitItem())
            assertEquals(setOf(service3, service4), awaitItem())
            ensureAllEventsConsumed()
        }
        assertEquals(
            listOf(parcelableService3, parcelableService4),
            handle[STATE_KEY_EXPANDED_SERVICES]
        )
    }

    private val service1 get() = FakeServiceDescriptor(
        serviceName = "1",
        operatorCode = "TEST1"
    )

    private val service2 get() = FakeServiceDescriptor(
        serviceName = "2",
        operatorCode = "TEST2"
    )

    private val service3 get() = FakeServiceDescriptor(
        serviceName = "3",
        operatorCode = "TEST3"
    )

    private val service4 get() = FakeServiceDescriptor(
        serviceName = "4",
        operatorCode = "TEST4"
    )

    private val parcelableService1 get() = ParcelableServiceDescriptor(
        serviceName = "1",
        operatorCode = "TEST1"
    )

    private val parcelableService2 get() = ParcelableServiceDescriptor(
        serviceName = "2",
        operatorCode = "TEST2"
    )

    private val parcelableService3 get() = ParcelableServiceDescriptor(
        serviceName = "3",
        operatorCode = "TEST3"
    )

    private val parcelableService4 get() = ParcelableServiceDescriptor(
        serviceName = "4",
        operatorCode = "TEST4"
    )
}
