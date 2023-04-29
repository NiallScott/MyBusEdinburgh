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

package uk.org.rivernile.android.bustracker.ui.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceDataStoreSource
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [SettingsPreferenceDataStore].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SettingsPreferenceDataStoreTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var dataStoreSource: PreferenceDataStoreSource

    private lateinit var dataStore: SettingsPreferenceDataStore

    @Before
    fun setUp() {
        dataStore = SettingsPreferenceDataStore(
            dataStoreSource,
            coroutineRule.scope,
            coroutineRule.testDispatcher)
    }

    @Test
    fun putStringWithNullValueRemovesValue() = runTest {
        val key = stringPreferencesKey("key1")
        val preferences = mutablePreferencesOf(key to "foobar")
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())

        dataStore.putString("key1", null)
        advanceUntilIdle()

        assertNull(preferences[key])
    }

    @Test
    fun putStringWithNonNullValueAddsValue() = runTest {
        val key = stringPreferencesKey("key1")
        val preferences = mutablePreferencesOf()
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())

        dataStore.putString("key1", "foobar")
        advanceUntilIdle()

        assertEquals("foobar", preferences[key])
    }

    @Test
    fun putStringSetWithNullValueRemovesValue() = runTest {
        val key = stringSetPreferencesKey("key1")
        val preferences = mutablePreferencesOf(key to setOf("a", "b", "c"))
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())

        dataStore.putStringSet("key1", null)
        advanceUntilIdle()

        assertNull(preferences[key])
    }

    @Test
    fun putStringSetWithNonNullValueAddsValue() = runTest {
        val key = stringSetPreferencesKey("key1")
        val preferences = mutablePreferencesOf()
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())
        val expected = setOf("a", "b", "c")

        dataStore.putStringSet("key1", expected)
        advanceUntilIdle()

        assertEquals(expected, preferences[key])
    }

    @Test
    fun putIntAddsValue() = runTest {
        val key = intPreferencesKey("key1")
        val preferences = mutablePreferencesOf()
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())

        dataStore.putInt("key1", 42)
        advanceUntilIdle()

        assertEquals(42, preferences[key])
    }

    @Test
    fun putLongAddsValue() = runTest {
        val key = longPreferencesKey("key1")
        val preferences = mutablePreferencesOf()
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())

        dataStore.putLong("key1", 42L)
        advanceUntilIdle()

        assertEquals(42L, preferences[key])
    }

    @Test
    fun putFloatAddsValue() = runTest {
        val key = floatPreferencesKey("key1")
        val preferences = mutablePreferencesOf()
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())

        dataStore.putFloat("key1", 3.14f)
        advanceUntilIdle()

        assertEquals(3.14f, preferences[key])
    }

    @Test
    fun putBooleanAddsValue() = runTest {
        val key = booleanPreferencesKey("key1")
        val preferences = mutablePreferencesOf()
        doAnswer {
            val transform = it.getArgument<suspend (MutablePreferences) -> Unit>(0)
            launch {
                transform(preferences)
            }
        }.whenever(dataStoreSource).edit(any())

        dataStore.putBoolean("key1", true)
        advanceUntilIdle()

        assertTrue(preferences[key] ?: throw IllegalStateException())
    }

    @Test
    fun getStringWithKeyNotAddedReturnsDefaultValue() {
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferencesOf()))

        val result = dataStore.getString("key1", "default")

        assertEquals("default", result)
    }

    @Test
    fun getStringWithKeyAddedReturnsValue() {
        val preferences = preferencesOf(
            stringPreferencesKey("key1") to "value")
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferences))

        val result = dataStore.getString("key1", "default")

        assertEquals("value", result)
    }

    @Test
    fun getStringSetWithKeyNotAddedReturnsDefaultValue() {
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferencesOf()))
        val expected = setOf("default")

        val result = dataStore.getStringSet("key1", expected)

        assertEquals(expected, result)
    }

    @Test
    fun getStringSetWithKeyAddedReturnsValue() {
        val expected = setOf("1", "2", "3")
        val preferences = preferencesOf(
            stringSetPreferencesKey("key1") to expected)
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferences))

        val result = dataStore.getStringSet("key1", setOf("default"))

        assertEquals(expected, result)
    }

    @Test
    fun getIntWithKeyNotAddedReturnsDefaultValue() {
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferencesOf()))

        val result = dataStore.getInt("key1", 100)

        assertEquals(100, result)
    }

    @Test
    fun getIntWithKeyAddedReturnsValue() {
        val preferences = preferencesOf(
            intPreferencesKey("key1") to 42)
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferences))

        val result = dataStore.getInt("key1", 100)

        assertEquals(42, result)
    }

    @Test
    fun getLongWithKeyNotAddedReturnsDefaultValue() {
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferencesOf()))

        val result = dataStore.getLong("key1", 100L)

        assertEquals(100L, result)
    }

    @Test
    fun getLongWithKeyAddedReturnsValue() {
        val preferences = preferencesOf(
            longPreferencesKey("key1") to 42)
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferences))

        val result = dataStore.getLong("key1", 100L)

        assertEquals(42L, result)
    }

    @Test
    fun getFloatWithKeyNotAddedReturnsDefaultValue() {
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferencesOf()))

        val result = dataStore.getFloat("key1", 1.23f)

        assertEquals(1.23f, result)
    }

    @Test
    fun getFloatWithKeyAddedReturnsValue() {
        val preferences = preferencesOf(
            floatPreferencesKey("key1") to 3.14f)
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferences))

        val result = dataStore.getFloat("key1", 1.23f)

        assertEquals(3.14f, result)
    }

    @Test
    fun getBooleanWithKeyNotAddedReturnsDefaultValue() {
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferencesOf()))

        val result = dataStore.getBoolean("key1", false)

        assertFalse(result)
    }

    @Test
    fun getBooleanWithKeyAddedReturnsValue() {
        val preferences = preferencesOf(
            booleanPreferencesKey("key1") to true)
        whenever(dataStoreSource.preferencesFlow)
            .thenReturn(flowOf(preferences))

        val result = dataStore.getBoolean("key1", false)

        assertTrue(result)
    }
}