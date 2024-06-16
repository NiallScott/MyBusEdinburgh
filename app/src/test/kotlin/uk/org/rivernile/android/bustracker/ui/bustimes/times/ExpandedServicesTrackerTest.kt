/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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
            mapOf(STATE_KEY_EXPANDED_SERVICES to arrayListOf<String>())
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
            mapOf(STATE_KEY_EXPANDED_SERVICES to arrayListOf("1", "2", "3"))
        )
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            assertEquals(setOf("1", "2", "3"), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onServiceClickedAfterEmptyInitialStateAddsService() = runTest {
        val handle = SavedStateHandle()
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            tracker.onServiceClicked("1")

            assertEquals(emptySet(), awaitItem())
            assertEquals(setOf("1"), awaitItem())
            ensureAllEventsConsumed()
        }
        assertEquals(listOf("1"), handle[STATE_KEY_EXPANDED_SERVICES])
    }

    @Test
    fun onServiceClickedAfterPopulatedInitialStateRemovesService() = runTest {
        val handle = SavedStateHandle(
                mapOf(STATE_KEY_EXPANDED_SERVICES to arrayListOf("1"))
        )
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            tracker.onServiceClicked("1")

            assertEquals(setOf("1"), awaitItem())
            assertEquals(emptySet(), awaitItem())
            ensureAllEventsConsumed()
        }
        assertEquals(arrayListOf<String>(), handle[STATE_KEY_EXPANDED_SERVICES])
    }

    @Test
    fun onServiceClickedWithMultipleRandomInteractionsYieldsCorrectState() = runTest {
        val handle = SavedStateHandle(
            mapOf(STATE_KEY_EXPANDED_SERVICES to arrayListOf("1"))
        )
        val tracker = ExpandedServicesTracker(handle)

        tracker.expandedServicesFlow.test {
            tracker.onServiceClicked("2")
            tracker.onServiceClicked("3")
            tracker.onServiceClicked("1")
            tracker.onServiceClicked("4")
            tracker.onServiceClicked("4")
            tracker.onServiceClicked("4")
            tracker.onServiceClicked("2")

            assertEquals(setOf("1"), awaitItem())
            assertEquals(setOf("1", "2"), awaitItem())
            assertEquals(setOf("1", "2", "3"), awaitItem())
            assertEquals(setOf("2", "3"), awaitItem())
            assertEquals(setOf("2", "3", "4"), awaitItem())
            assertEquals(setOf("2", "3"), awaitItem())
            assertEquals(setOf("2", "3", "4"), awaitItem())
            assertEquals(setOf("3", "4"), awaitItem())
            ensureAllEventsConsumed()
        }
        assertEquals(listOf("3", "4"), handle[STATE_KEY_EXPANDED_SERVICES])
    }
}