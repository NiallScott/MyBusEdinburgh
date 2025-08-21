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

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [AndroidPreferenceDataStorage].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AndroidPreferenceDataStorageTest {

    companion object {

        private const val DEFAULT_WIFI_ONLY = false
        private const val DEFAULT_AUTO_REFRESH = false
        private const val DEFAULT_SHOW_NIGHT_BUSES = true
        private const val DEFAULT_SERVICE_SORTING = false
        private const val DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE = 4
        private const val DEFAULT_MAP_ZOOM_BUTTONS = true
        private const val DEFAULT_DISABLE_GPS_PROMPT = false
        private const val DEFAULT_LATITUDE = 55.953
        private const val DEFAULT_LONGITUDE = -3.189
        private const val DEFAULT_MAP_LAST_ZOOM = 11f
        private const val DEFAULT_MAP_LAST_TYPE = 1

        private const val APP_THEME_LIGHT = "light"
        private const val APP_THEME_DARK = "dark"
    }

    @Test
    fun isDatabaseUpdateWifiOnlyFlowEmitsValues() = runTest {
        val key = booleanPreferencesKey(PREF_BUS_STOP_DATABASE_WIFI_ONLY)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(key to DEFAULT_WIFI_ONLY),
            preferencesOf(key to true),
            preferencesOf(key to true),
            preferencesOf(key to false)
        )
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.isDatabaseUpdateWifiOnlyFlow.test {
            assertEquals(DEFAULT_WIFI_ONLY, awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun appThemeFlowEmitsValues() = runTest {
        val key = stringPreferencesKey(PREF_APP_THEME)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(),
            preferencesOf(key to APP_THEME_LIGHT),
            preferencesOf(key to APP_THEME_LIGHT),
            preferencesOf(key to APP_THEME_DARK),
            preferencesOf(key to APP_THEME_DARK),
            preferencesOf()
        )
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.appThemeFlow.test {
            assertEquals(AppTheme.SYSTEM_DEFAULT, awaitItem())
            assertEquals(AppTheme.LIGHT, awaitItem())
            assertEquals(AppTheme.DARK, awaitItem())
            assertEquals(AppTheme.SYSTEM_DEFAULT, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isLiveTimesAutoRefreshEnabledFlowEmitsValues() = runTest {
        val key = booleanPreferencesKey(PREF_AUTO_REFRESH)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(key to DEFAULT_AUTO_REFRESH),
            preferencesOf(key to true),
            preferencesOf(key to true),
            preferencesOf(key to false)
        )
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.isLiveTimesAutoRefreshEnabledFlow.test {
            assertEquals(DEFAULT_AUTO_REFRESH, awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isLiveTimesShowNightServicesEnabledFlowEmitsValues() = runTest {
        val key = booleanPreferencesKey(PREF_SHOW_NIGHT_BUSES)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(key to DEFAULT_SHOW_NIGHT_BUSES),
            preferencesOf(key to false),
            preferencesOf(key to false),
            preferencesOf(key to true))
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.isLiveTimesShowNightServicesEnabledFlow.test {
            assertEquals(DEFAULT_SHOW_NIGHT_BUSES, awaitItem())
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isLiveTimesSortByTimeFlowEmitsValues() = runTest {
        val key = booleanPreferencesKey(PREF_SERVICE_SORTING)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(key to DEFAULT_SERVICE_SORTING),
            preferencesOf(key to true),
            preferencesOf(key to true),
            preferencesOf(key to false)
        )
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.isLiveTimesSortByTimeFlow.test {
            assertEquals(DEFAULT_SERVICE_SORTING, awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
        }
    }

    @Test
    fun liveTimesNumberOfDeparturesFlowEmitsValue() = runTest {
        val key = stringPreferencesKey(PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(key to DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE.toString()),
            preferencesOf(key to "9"),
            preferencesOf(key to "9"),
            preferencesOf(key to "5")
        )
        val exceptionLogger = FakeExceptionLogger()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            ),
            exceptionLogger = exceptionLogger
        )

        dataStorage.liveTimesNumberOfDeparturesFlow.test {
            assertEquals(4, awaitItem())
            assertEquals(9, awaitItem())
            assertEquals(5, awaitItem())
            awaitComplete()
        }
        assertTrue(exceptionLogger.loggedThrowables.isEmpty())
    }

    @Test
    fun liveTimesNumberOfDeparturesFlowCopesWithPoorlyFormattedNumber() = runTest {
        val key = stringPreferencesKey(PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE)
        val flow = flowOf(preferencesOf(key to "not a number"))
        val exceptionLogger = FakeExceptionLogger()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = dataStorage.liveTimesNumberOfDeparturesFlow.first()

        assertEquals(DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE, result)
        assertIs<NumberFormatException>(exceptionLogger.loggedThrowables.single())
    }

    @Test
    fun isMapZoomControlsVisibleFlowEmitsValues() = runTest {
        val key = booleanPreferencesKey(PREF_ZOOM_BUTTONS)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(key to DEFAULT_MAP_ZOOM_BUTTONS),
            preferencesOf(key to false),
            preferencesOf(key to false),
            preferencesOf(key to true)
        )
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.isMapZoomControlsVisibleFlow.test {
            assertEquals(DEFAULT_MAP_ZOOM_BUTTONS, awaitItem())
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isGpsPromptDisabledFlowEmitsValues() = runTest {
        val key = booleanPreferencesKey(PREF_DISABLE_GPS_PROMPT)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(key to DEFAULT_DISABLE_GPS_PROMPT),
            preferencesOf(key to true),
            preferencesOf(key to true),
            preferencesOf(key to false)
        )
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.isGpsPromptDisabledFlow.test {
            assertEquals(DEFAULT_DISABLE_GPS_PROMPT, awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun lastMapCameraLocationFlowEmitsValues() = runTest {
        val keyMapLastLatitude = stringPreferencesKey(PREF_MAP_LAST_LATITUDE)
        val keyMapLastLongitude = stringPreferencesKey(PREF_MAP_LAST_LONGITUDE)
        val keyMapLastZoom = floatPreferencesKey(PREF_MAP_LAST_ZOOM)
        val flow = intervalFlowOf(
            0L,
            10L,
            preferencesOf(),
            preferencesOf(
                keyMapLastLatitude to DEFAULT_LATITUDE.toString(),
                keyMapLastLongitude to DEFAULT_LONGITUDE.toString(),
                keyMapLastZoom to DEFAULT_MAP_LAST_ZOOM
            ),
            preferencesOf(
                keyMapLastLatitude to "1.1",
                keyMapLastLongitude to "2.2",
                keyMapLastZoom to 3.14f
            ),
            preferencesOf(
                keyMapLastLatitude to "1.1",
                keyMapLastLongitude to "2.2",
                keyMapLastZoom to 3.14f
            ),
            preferencesOf()
        )
        val exceptionLogger = FakeExceptionLogger()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            ),
            exceptionLogger = exceptionLogger
        )

        dataStorage.lastMapCameraLocationFlow.test {
            assertEquals(
                LastMapCameraLocation(
                    DEFAULT_LATITUDE,
                    DEFAULT_LONGITUDE,
                    DEFAULT_MAP_LAST_ZOOM
                ),
                awaitItem()
            )
            assertEquals(
                LastMapCameraLocation(
                    1.1,
                    2.2,
                    3.14f
                ),
                awaitItem()
            )
            assertEquals(
                LastMapCameraLocation(
                    DEFAULT_LATITUDE,
                    DEFAULT_LONGITUDE,
                    DEFAULT_MAP_LAST_ZOOM
                ),
                awaitItem()
            )
            awaitComplete()
        }
        assertTrue(exceptionLogger.loggedThrowables.isEmpty())
    }

    @Test
    fun lastMapCameraLocationFlowCopesWithPoorlyFormattedLatitude() = runTest {
        val keyMapLastLatitude = stringPreferencesKey(PREF_MAP_LAST_LATITUDE)
        val keyMapLastLongitude = stringPreferencesKey(PREF_MAP_LAST_LONGITUDE)
        val keyMapLastZoom = floatPreferencesKey(PREF_MAP_LAST_ZOOM)
        val flow = flowOf(
            preferencesOf(
                keyMapLastLatitude to "not a number",
                keyMapLastLongitude to "2.2",
                keyMapLastZoom to 3.14f
            )
        )
        val expected = LastMapCameraLocation(
            DEFAULT_LATITUDE,
            DEFAULT_LONGITUDE,
            3.14f
        )
        val exceptionLogger = FakeExceptionLogger()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = dataStorage.lastMapCameraLocationFlow.first()

        assertEquals(expected, result)
        assertIs<NumberFormatException>(exceptionLogger.loggedThrowables.single())
    }

    @Test
    fun lastMapCameraLocationFlowCopesWithPoorlyFormattedLongitude() = runTest {
        val keyMapLastLatitude = stringPreferencesKey(PREF_MAP_LAST_LATITUDE)
        val keyMapLastLongitude = stringPreferencesKey(PREF_MAP_LAST_LONGITUDE)
        val keyMapLastZoom = floatPreferencesKey(PREF_MAP_LAST_ZOOM)
        val flow = flowOf(
            preferencesOf(
                keyMapLastLatitude to "1.1",
                keyMapLastLongitude to "not a number",
                keyMapLastZoom to 3.14f
            )
        )
        val expected = LastMapCameraLocation(
            DEFAULT_LATITUDE,
            DEFAULT_LONGITUDE,
            3.14f
        )
        val exceptionLogger = FakeExceptionLogger()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = dataStorage.lastMapCameraLocationFlow.first()

        assertEquals(expected, result)
        assertIs<NumberFormatException>(exceptionLogger.loggedThrowables.single())
    }

    @Test
    fun mapTypeFlowEmitsValues() = runTest {
        val key = intPreferencesKey(PREF_MAP_LAST_MAP_TYPE)
        val flow = intervalFlowOf(
            0L,
            10,
            preferencesOf(),
            preferencesOf(key to DEFAULT_MAP_LAST_TYPE),
            preferencesOf(key to 2),
            preferencesOf(key to 2),
            preferencesOf(key to 3)
        )

        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onPreferencesFlow = { flow }
            )
        )

        dataStorage.mapTypeFlow.test {
            assertEquals(DEFAULT_MAP_LAST_TYPE, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun toggleSortByTimeTogglesDefaultValue() = runTest {
        val key = booleanPreferencesKey(PREF_SERVICE_SORTING)
        val preferences = mutablePreferencesOf()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onEdit = {
                    launch {
                        it(preferences)
                    }
                }
            )
        )

        dataStorage.toggleSortByTime()
        advanceUntilIdle()

        @Suppress("KotlinConstantConditions")
        assertEquals(!DEFAULT_SERVICE_SORTING, preferences[key])
    }

    @Test
    fun toggleSortByTimeTogglesExistingValue() = runTest {
        val key = booleanPreferencesKey(PREF_SERVICE_SORTING)
        val preferences = mutablePreferencesOf(key to true)
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onEdit = {
                    launch {
                        it(preferences)
                    }
                }
            )
        )

        dataStorage.toggleSortByTime()
        advanceUntilIdle()

        assertFalse(preferences[key] ?: throw IllegalStateException())
    }

    @Test
    fun toggleAutoRefreshTogglesDefaultValue() = runTest {
        val key = booleanPreferencesKey(PREF_AUTO_REFRESH)
        val preferences = mutablePreferencesOf()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onEdit = {
                    launch {
                        it(preferences)
                    }
                }
            )
        )

        dataStorage.toggleAutoRefresh()
        advanceUntilIdle()

        @Suppress("KotlinConstantConditions")
        assertEquals(!DEFAULT_AUTO_REFRESH, preferences[key])
    }

    @Test
    fun toggleAutoRefreshTogglesExistingValue() = runTest {
        val key = booleanPreferencesKey(PREF_AUTO_REFRESH)
        val preferences = mutablePreferencesOf(key to true)
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onEdit = {
                    launch {
                        it(preferences)
                    }
                }
            )
        )

        dataStorage.toggleAutoRefresh()
        advanceUntilIdle()

        assertFalse(preferences[key] ?: throw IllegalStateException())
    }

    @Test
    fun setIsGpsPromptDisabledSetsValue() = runTest {
        val key = booleanPreferencesKey(PREF_DISABLE_GPS_PROMPT)
        val preferences = mutablePreferencesOf()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onEdit = {
                    launch {
                        it(preferences)
                    }
                }
            )
        )

        dataStorage.setIsGpsPromptDisabled(true)
        advanceUntilIdle()

        assertTrue(preferences[key] ?: throw IllegalStateException())
    }

    @Test
    fun setLastMapCameraLocationSetsValue() = runTest {
        val keyMapLastLatitude = stringPreferencesKey(PREF_MAP_LAST_LATITUDE)
        val keyMapLastLongitude = stringPreferencesKey(PREF_MAP_LAST_LONGITUDE)
        val keyMapLastZoom = floatPreferencesKey(PREF_MAP_LAST_ZOOM)
        val preferences = mutablePreferencesOf()
        val newValue = LastMapCameraLocation(
            latitude = 1.1,
            longitude = 2.2,
            zoomLevel = 3.14f
        )
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onEdit = {
                    launch {
                        it(preferences)
                    }
                }
            )
        )

        dataStorage.setLastMapCameraLocation(newValue)
        advanceUntilIdle()

        assertEquals(newValue.latitude.toString(), preferences[keyMapLastLatitude])
        assertEquals(newValue.longitude.toString(), preferences[keyMapLastLongitude])
        assertEquals(newValue.zoomLevel, preferences[keyMapLastZoom])
    }

    @Test
    fun setMapTypeSetsValue() = runTest {
        val key = intPreferencesKey(PREF_MAP_LAST_MAP_TYPE)
        val preferences = mutablePreferencesOf()
        val dataStorage = createAndroidPreferenceDataStorage(
            dataStoreSource = FakePreferenceDataStoreSource(
                onEdit = {
                    launch {
                        it(preferences)
                    }
                }
            )
        )

        dataStorage.setMapType(42)
        advanceUntilIdle()

        assertEquals(42, preferences[key])
    }

    private fun createAndroidPreferenceDataStorage(
        dataStoreSource: PreferenceDataStoreSource = FakePreferenceDataStoreSource(),
        exceptionLogger: ExceptionLogger = FakeExceptionLogger()
    ): AndroidPreferenceDataStorage {
        return AndroidPreferenceDataStorage(
            dataStoreSource,
            exceptionLogger
        )
    }
}
