/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.preferences

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [PreferenceRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class PreferenceRepositoryTest {

    @Rule
    @JvmField
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var repository: PreferenceRepository

    @Before
    fun setUp() {
        repository = PreferenceRepository(preferenceManager, coroutineRule,
                coroutineRule.testDispatcher)
    }

    @Test
    fun isLiveTimesAutoRefreshEnabledFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.isBusTimesAutoRefreshEnabled())
                .thenReturn(true)

        val observer = repository.isLiveTimesAutoRefreshEnabledFlow().test(this)
        observer.finish()

        observer.assertValues(true)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun isLiveTimesAutoRefreshEnabledFlowResponsdsToPreferenceChange() =
            coroutineRule.runBlockingTest {
        doAnswer {
            val prefListener = it.getArgument<PreferenceListener>(0)
            val expectedKeys = setOf(PreferenceKey.LIVE_TIMES_AUTO_REFRESH_ENABLED)
            assertEquals(expectedKeys, prefListener.keys)
            prefListener.listener.apply {
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_AUTO_REFRESH_ENABLED)
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_AUTO_REFRESH_ENABLED)
            }
        }.whenever(preferenceManager).addOnPreferenceChangedListener(any())
        whenever(preferenceManager.isBusTimesAutoRefreshEnabled())
                .thenReturn(true, false, true)

        val observer = repository.isLiveTimesAutoRefreshEnabledFlow().test(this)
        observer.finish()

        observer.assertValues(true, false, true)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun isLiveTimesShowNightServicesEnabledFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.isBusTimesShowingNightServices())
                .thenReturn(true)

        val observer = repository.isLiveTimesShowNightServicesEnabledFlow().test(this)
        observer.finish()

        observer.assertValues(true)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun isLiveTimesShowNightServicesEnabledFlowRespondsToPreferenceChange() =
            coroutineRule.runBlockingTest {
        doAnswer {
            val prefListener = it.getArgument<PreferenceListener>(0)
            val expectedKeys = setOf(PreferenceKey.LIVE_TIMES_SHOW_NIGHT_SERVICES)
            assertEquals(expectedKeys, prefListener.keys)
            prefListener.listener.apply {
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_SHOW_NIGHT_SERVICES)
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_SHOW_NIGHT_SERVICES)
            }
        }.whenever(preferenceManager).addOnPreferenceChangedListener(any())
        whenever(preferenceManager.isBusTimesShowingNightServices())
                .thenReturn(true, false, true)

        val observer = repository.isLiveTimesShowNightServicesEnabledFlow().test(this)
        observer.finish()

        observer.assertValues(true, false, true)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun isLiveTimesSortByTimeFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.isBusTimesSortedByTime())
                .thenReturn(true)

        val observer = repository.isLiveTimesSortByTimeFlow().test(this)
        observer.finish()

        observer.assertValues(true)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun isLiveTimesSortByTimeFlowRespondsToPreferenceChange() = coroutineRule.runBlockingTest {
        doAnswer {
            val prefListener = it.getArgument<PreferenceListener>(0)
            val expectedKeys = setOf(PreferenceKey.LIVE_TIMES_SORT_BY_TIME)
            assertEquals(expectedKeys, prefListener.keys)
            prefListener.listener.apply {
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_SORT_BY_TIME)
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_SORT_BY_TIME)
            }
        }.whenever(preferenceManager).addOnPreferenceChangedListener(any())
        whenever(preferenceManager.isBusTimesSortedByTime())
                .thenReturn(true, false, true)

        val observer = repository.isLiveTimesSortByTimeFlow().test(this)
        observer.finish()

        observer.assertValues(true, false, true)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun getLiveTimesNumberOfDeparturesFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.getBusTimesNumberOfDeparturesToShowPerService())
                .thenReturn(1)

        val observer = repository.getLiveTimesNumberOfDeparturesFlow().test(this)
        observer.finish()

        observer.assertValues(1)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun getLiveTimesNumberOfDeparturesFlowRespondsToPreferenceChange() =
            coroutineRule.runBlockingTest {
        doAnswer {
            val prefListener = it.getArgument<PreferenceListener>(0)
            val expectedKeys = setOf(PreferenceKey.LIVE_TIMES_NUMBER_OF_DEPARTURES)
            assertEquals(expectedKeys, prefListener.keys)
            prefListener.listener.apply {
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_NUMBER_OF_DEPARTURES)
                onPreferenceChanged(PreferenceKey.LIVE_TIMES_NUMBER_OF_DEPARTURES)
            }
        }.whenever(preferenceManager).addOnPreferenceChangedListener(any())
        whenever(preferenceManager.getBusTimesNumberOfDeparturesToShowPerService())
                .thenReturn(1, 2, 3)

        val observer = repository.getLiveTimesNumberOfDeparturesFlow().test(this)
        observer.finish()

        observer.assertValues(1, 2, 3)
        verify(preferenceManager)
                .removeOnPreferenceChangedListener(any())
    }

    @Test
    fun toggleSortByTimeTogglesFalseToTrue() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.isBusTimesSortedByTime())
                .thenReturn(false)

        repository.toggleSortByTime()

        verify(preferenceManager)
                .setBusTimesSortedByTime(true)
    }

    @Test
    fun toggleSortByTimeTogglesTrueToFalse() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.isBusTimesSortedByTime())
                .thenReturn(true)

        repository.toggleSortByTime()

        verify(preferenceManager)
                .setBusTimesSortedByTime(false)
    }

    @Test
    fun toggleAutoRefreshTogglesFalseToTrue() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.isBusTimesAutoRefreshEnabled())
                .thenReturn(false)

        repository.toggleAutoRefresh()

        verify(preferenceManager)
                .setBusTimesAutoRefreshEnabled(true)
    }

    @Test
    fun toggleAutoRefreshTogglesTrueToFalse() = coroutineRule.runBlockingTest {
        whenever(preferenceManager.isBusTimesAutoRefreshEnabled())
                .thenReturn(true)

        repository.toggleAutoRefresh()

        verify(preferenceManager)
                .setBusTimesAutoRefreshEnabled(false)
    }
}