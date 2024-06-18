/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceDataStoreSource
import javax.inject.Inject

/**
 * This is a settings-specific implementation of [PreferenceDataStore]. This allows
 * [SettingsFragment] to use [DataStore] as the storage implementation instead of Shared
 * Preferences.
 *
 * All the 'put' methods will safely store data on a non-main thread Coroutine. However,
 * unfortunately the get methods are blocking on the main thread. Until Google update the
 * `androidx.preference` library to support [DataStore] properly, we are stuck with this, as there
 * is no asynchronous way to update the values for preferences.
 *
 * This implementation was inspired by
 * https://stackoverflow.com/questions/65396998/androidx-preferences-library-vs-datastore-preferences
 * and adapted to suit this codebase.
 *
 * @param dataStoreSource Where the preferences are stored.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
class SettingsPreferenceDataStore @Inject constructor(
    private val dataStoreSource: PreferenceDataStoreSource,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {
        putPreference(stringPreferencesKey(key), value)
    }

    override fun putStringSet(key: String, values: Set<String>?) {
        putPreference(stringSetPreferencesKey(key), values)
    }

    override fun putInt(key: String, value: Int) {
        putPreference(intPreferencesKey(key), value)
    }

    override fun putLong(key: String, value: Long) {
        putPreference(longPreferencesKey(key), value)
    }

    override fun putFloat(key: String, value: Float) {
        putPreference(floatPreferencesKey(key), value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        putPreference(booleanPreferencesKey(key), value)
    }

    override fun getString(key: String, defValue: String?) = runBlocking {
        dataStoreSource
            .preferencesFlow
            .map {
                it[stringPreferencesKey(key)] ?: defValue
            }
            .first()
    }

    override fun getStringSet(key: String, defValues: Set<String>?) = runBlocking {
        dataStoreSource
            .preferencesFlow
            .map {
                it[stringSetPreferencesKey(key)] ?: defValues
            }
            .first()
    }

    override fun getInt(key: String, defValue: Int) = runBlocking {
        dataStoreSource
            .preferencesFlow
            .map {
                it[intPreferencesKey(key)] ?: defValue
            }
            .first()
    }

    override fun getLong(key: String, defValue: Long) = runBlocking {
        dataStoreSource
            .preferencesFlow
            .map {
                it[longPreferencesKey(key)] ?: defValue
            }
            .first()
    }

    override fun getFloat(key: String, defValue: Float) = runBlocking {
        dataStoreSource
            .preferencesFlow
            .map {
                it[floatPreferencesKey(key)] ?: defValue
            }
            .first()
    }

    override fun getBoolean(key: String, defValue: Boolean) = runBlocking {
        dataStoreSource
            .preferencesFlow
            .map {
                it[booleanPreferencesKey(key)] ?: defValue
            }
            .first()
    }

    /**
     * Stored a preference.
     *
     * @param key The preference key.
     * @param value The new value of the preference.
     */
    private fun <T> putPreference(key: Preferences.Key<T>, value: T?) {
        applicationCoroutineScope.launch(defaultDispatcher) {
            dataStoreSource.edit {
                if (value != null) {
                    it[key] = value
                } else {
                    it.remove(key)
                }
            }
        }
    }
}