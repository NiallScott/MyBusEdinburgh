/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for [RealPreferenceDataStoreSource].
 *
 * @author Niall Scott
 */
class RealPreferenceDataStoreSourceTest {

    @Test
    fun preferencesFlowRethrowsExceptionWhenNotIoException() {
        val exception = IllegalStateException()
        val exceptionLogger = FakeExceptionLogger()
        val dataSource = createPreferenceDataStoreSource(
            dataStore = FakeDataStore(
                onData = {
                    flow {
                        throw exception
                    }
                }
            ),
            exceptionLogger = exceptionLogger
        )

        try {
            runTest {
                dataSource.preferencesFlow.first()
            }

            fail("This part of the test should not be reached.")
        } catch (e: IllegalStateException) {
            assertEquals(listOf(exception), exceptionLogger.loggedThrowables)
        }
    }

    @Test
    fun preferencesFlowCatchesIoExceptionAndIssuesEmptyPreferences() = runTest {
        val exception = IOException()
        val exceptionLogger = FakeExceptionLogger()
        val dataSource = createPreferenceDataStoreSource(
            dataStore = FakeDataStore(
                onData = {
                    flow {
                        throw exception
                    }
                }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = dataSource.preferencesFlow.first()

        assertTrue(result.asMap().isEmpty())
        assertEquals(listOf(exception), exceptionLogger.loggedThrowables)
    }

    @Test
    fun preferencesFlowWithNoExceptionEmitsPreferences() = runTest {
        val preferenceKey = stringPreferencesKey("foo")
        val preferences = preferencesOf(preferenceKey to "bar")
        val exceptionLogger = FakeExceptionLogger()
        val dataSource = createPreferenceDataStoreSource(
            dataStore = FakeDataStore(
                onData = { flowOf(preferences) }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = dataSource.preferencesFlow.first()

        assertEquals("bar", result[preferenceKey])
        assertTrue(exceptionLogger.loggedThrowables.isEmpty())
    }

    @Test
    fun editWithNoExceptionDoesNotLogException() = runTest {
        val exceptionLogger = FakeExceptionLogger()
        val dataSource = createPreferenceDataStoreSource(
            dataStore = FakeDataStore(
                onUpdateData = { emptyPreferences() }
            ),
            exceptionLogger = exceptionLogger
        )

        dataSource.edit { }

        assertTrue(exceptionLogger.loggedThrowables.isEmpty())
    }

    @Test
    fun editWithExceptionLogsException() = runTest {
        val exceptionLogger = FakeExceptionLogger()
        val exception = IOException()
        val dataSource = createPreferenceDataStoreSource(
            dataStore = FakeDataStore(
                onUpdateData = { throw exception }
            ),
            exceptionLogger = exceptionLogger
        )

        dataSource.edit { }

        assertEquals(listOf(exception), exceptionLogger.loggedThrowables)
    }

    private fun createPreferenceDataStoreSource(
        dataStore: DataStore<Preferences> = FakeDataStore(),
        exceptionLogger: ExceptionLogger = FakeExceptionLogger()
    ): PreferenceDataStoreSource {
        return RealPreferenceDataStoreSource(
            dataStore,
            exceptionLogger
        )
    }
}