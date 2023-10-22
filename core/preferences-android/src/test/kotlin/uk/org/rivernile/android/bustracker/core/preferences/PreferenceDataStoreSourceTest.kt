/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test
import java.io.IOException

/**
 * Tests for [PreferenceDataStoreSource].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class PreferenceDataStoreSourceTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var dataStore: DataStore<Preferences>
    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    private lateinit var dataSource: PreferenceDataStoreSource

    @Before
    fun setUp() {
        dataSource = PreferenceDataStoreSource(
            dataStore,
            exceptionLogger)
    }

    @Test
    fun preferencesFlowRethrowsExceptionWhenNotIoException() {
        val exception = IllegalStateException()
        val flow = flow<Preferences> {
            throw exception
        }
        whenever(dataStore.data)
            .thenReturn(flow)

        try {
            runTest {
                dataSource.preferencesFlow.test(this)
                advanceUntilIdle()
            }

            fail("This part of the test should not be reached.")
        } catch (e: IllegalStateException) {
            verify(exceptionLogger)
                .log(exception)
        }
    }

    @Test
    fun preferencesFlowCatchesIoExceptionAndIssuesEmptyPreferences() = runTest {
        val exception = IOException()
        val flow = flow<Preferences> {
            throw exception
        }
        whenever(dataStore.data)
            .thenReturn(flow)

        val result = dataSource.preferencesFlow.first()

        assertTrue(result.asMap().isEmpty())
        verify(exceptionLogger)
            .log(exception)
    }

    @Test
    fun preferencesFlowWithNoExceptionEmitsPreferences() = runTest {
        val preferenceKey = stringPreferencesKey("foo")
        val preferences = preferencesOf(preferenceKey to "bar")
        whenever(dataStore.data)
            .thenReturn(flowOf(preferences))

        val result = dataSource.preferencesFlow.first()

        assertEquals("bar", result[preferenceKey])
        verify(exceptionLogger, never())
            .log(any())
    }
}