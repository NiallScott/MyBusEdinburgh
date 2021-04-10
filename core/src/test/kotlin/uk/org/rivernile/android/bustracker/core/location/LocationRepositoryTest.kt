/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.location

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [LocationRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LocationRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var hasLocationFeatureDetector: HasLocationFeatureDetector
    @Mock
    private lateinit var isLocationEnabledDetector: IsLocationEnabledDetector

    private lateinit var locationRepository: LocationRepository

    @Before
    fun setUp() {
        locationRepository = LocationRepository(
                hasLocationFeatureDetector,
                isLocationEnabledDetector)
    }

    @Test
    fun hasLocationFeatureIsFalseWhenHasLocationFeatureDetectorReturnsFalse() {
        whenever(hasLocationFeatureDetector.hasLocationFeature())
                .thenReturn(false)

        val result = locationRepository.hasLocationFeature

        assertFalse(result)
    }

    @Test
    fun hasLocationFeatureIsTrueWhenHasLocationFeatureDetectorReturnsTrue() {
        whenever(hasLocationFeatureDetector.hasLocationFeature())
                .thenReturn(true)

        val result = locationRepository.hasLocationFeature

        assertTrue(result)
    }

    @Test
    fun hasLocationFeatureCallsHasLocationFeatureDetectorOnlyOnce() {
        whenever(hasLocationFeatureDetector.hasLocationFeature())
                .thenReturn(true)

        locationRepository.hasLocationFeature
        locationRepository.hasLocationFeature
        val result = locationRepository.hasLocationFeature

        assertTrue(result)
        verify(hasLocationFeatureDetector, times(1))
                .hasLocationFeature()
    }

    @Test
    fun getIsLocationEnabledFlowReturnsFlowFromIsLocationEnabledDetector() =
            coroutineRule.runBlockingTest {
        whenever(isLocationEnabledDetector.getIsLocationEnabledFlow())
                .thenReturn(flowOf(false, true, false))

        val observer = locationRepository.getIsLocationEnabledFlow().test(this)
        observer.finish()

        observer.assertValues(false, true, false)
    }
}