/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.networking

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ConnectivityRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ConnectivityRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var connectivityChecker: ConnectivityChecker

    private lateinit var repository: ConnectivityRepository

    @Before
    fun setUp() {
        repository = ConnectivityRepository(connectivityChecker, coroutineRule.scope)
    }

    @Test
    fun hasInternetConnectivityReturnsFalseWhenConnectivityCheckerReturnsFalse() {
        whenever(connectivityChecker.hasInternetConnectivity)
                .thenReturn(false)

        val result = repository.hasInternetConnectivity

        assertFalse(result)
    }

    @Test
    fun hasInternetConnectivityReturnsTrueWhenConnectivityCheckerReturnsTrue() {
        whenever(connectivityChecker.hasInternetConnectivity)
                .thenReturn(true)

        val result = repository.hasInternetConnectivity

        assertTrue(result)
    }

    @Test
    fun hasInternetConnectivityFlowEmitsValuesFromConnectivityChecker() = runTest {
        whenever(connectivityChecker.hasInternetConnectivityFlow)
                .thenReturn(intervalFlowOf(0L, 10L, false, true, true, false, false, true))

        val observer = repository.hasInternetConnectivityFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(false, true, false, true)
    }
}