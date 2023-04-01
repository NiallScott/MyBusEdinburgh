/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [ExpandedServicesTracker].
 *
 * @author Niall Scott
 */
class ExpandedServicesTrackerTest {

    companion object {

        private const val STATE_KEY_EXPANDED_SERVICES = "expandedServices"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun initialStateWithNoPreviousStateIsEmpty() {
        val handle = SavedStateHandle()
        val tracker = ExpandedServicesTracker(handle)

        val observer = tracker.expandedServicesLiveData.test()

        observer.assertValues(emptySet())
    }

    @Test
    fun initialStateWithEmptyPreviousStateIsEmpty() {
        val handle = SavedStateHandle(
                mapOf(
                        STATE_KEY_EXPANDED_SERVICES to arrayListOf<String>()))
        val tracker = ExpandedServicesTracker(handle)

        val observer = tracker.expandedServicesLiveData.test()

        observer.assertValues(emptySet())
    }

    @Test
    fun initialStateWithPopulatedServicesIsNonEmpty() {
        val handle = SavedStateHandle(
                mapOf(
                        STATE_KEY_EXPANDED_SERVICES to arrayListOf("1", "2", "3")))
        val tracker = ExpandedServicesTracker(handle)

        val observer = tracker.expandedServicesLiveData.test()

        observer.assertValues(setOf("1", "2", "3"))
    }

    @Test
    fun onServiceClickedAfterEmptyInitialStateAddsService() {
        val handle = SavedStateHandle()
        val tracker = ExpandedServicesTracker(handle)

        val observer = tracker.expandedServicesLiveData.test()
        tracker.onServiceClicked("1")

        observer.assertValues(
                emptySet(),
                setOf("1"))
        assertEquals(listOf("1"), handle[STATE_KEY_EXPANDED_SERVICES])
    }

    @Test
    fun onServiceClickedAfterPopulatedInitialStateRemovesService() {
        val handle = SavedStateHandle(
                mapOf(
                        STATE_KEY_EXPANDED_SERVICES to arrayListOf("1")))
        val tracker = ExpandedServicesTracker(handle)

        val observer = tracker.expandedServicesLiveData.test()
        tracker.onServiceClicked("1")

        observer.assertValues(
                setOf("1"),
                emptySet())
        assertEquals(arrayListOf<String>(), handle[STATE_KEY_EXPANDED_SERVICES])
    }

    @Test
    fun onServiceClickedWithMultipleRandomInteractionsYieldsCorrectState() {
        val handle = SavedStateHandle(
                mapOf(
                        STATE_KEY_EXPANDED_SERVICES to arrayListOf("1")))
        val tracker = ExpandedServicesTracker(handle)

        val observer = tracker.expandedServicesLiveData.test()
        tracker.onServiceClicked("2")
        tracker.onServiceClicked("3")
        tracker.onServiceClicked("1")
        tracker.onServiceClicked("4")
        tracker.onServiceClicked("4")
        tracker.onServiceClicked("4")
        tracker.onServiceClicked("2")

        observer.assertValues(
                setOf("1"),
                setOf("1", "2"),
                setOf("1", "2", "3"),
                setOf("2", "3"),
                setOf("2", "3", "4"),
                setOf("2", "3"),
                setOf("2", "3", "4"),
                setOf("3", "4"))
        assertEquals(listOf("3", "4"), handle[STATE_KEY_EXPANDED_SERVICES])
    }
}