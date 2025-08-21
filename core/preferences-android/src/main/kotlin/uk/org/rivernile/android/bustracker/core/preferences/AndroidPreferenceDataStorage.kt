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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject

/**
 * The Android-specific implementation of [PreferenceDataStorage] which uses the [DataStore] API
 * to store data.
 *
 * @param dataStoreSource Used to access the actual data store.
 * @param exceptionLogger Used to report exceptions.
 * @author Niall Scott
 */
internal class AndroidPreferenceDataStorage @Inject constructor(
    private val dataStoreSource: PreferenceDataStoreSource,
    private val exceptionLogger: ExceptionLogger
) : PreferenceDataStorage {

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

    private val keyBusStopDatabaseWifiOnly = booleanPreferencesKey(PREF_BUS_STOP_DATABASE_WIFI_ONLY)
    private val keyAppTheme = stringPreferencesKey(PREF_APP_THEME)
    private val keyAutoRefresh = booleanPreferencesKey(PREF_AUTO_REFRESH)
    private val keyShowNightBuses = booleanPreferencesKey(PREF_SHOW_NIGHT_BUSES)
    private val keyServiceSorting = booleanPreferencesKey(PREF_SERVICE_SORTING)
    private val keyNumberOfShownDeparturesPerService =
        stringPreferencesKey(PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE)
    private val keyZoomButtons = booleanPreferencesKey(PREF_ZOOM_BUTTONS)
    private val keyDisableGpsPrompt = booleanPreferencesKey(PREF_DISABLE_GPS_PROMPT)
    private val keyMapLastLatitude = stringPreferencesKey(PREF_MAP_LAST_LATITUDE)
    private val keyMapLastLongitude = stringPreferencesKey(PREF_MAP_LAST_LONGITUDE)
    private val keyMapLastZoom = floatPreferencesKey(PREF_MAP_LAST_ZOOM)
    private val keyMapLastType = intPreferencesKey(PREF_MAP_LAST_MAP_TYPE)

    override val isDatabaseUpdateWifiOnlyFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            it[keyBusStopDatabaseWifiOnly] ?: DEFAULT_WIFI_ONLY
        }
        .distinctUntilChanged()

    override val appThemeFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            when (it[keyAppTheme]) {
                APP_THEME_LIGHT -> AppTheme.LIGHT
                APP_THEME_DARK -> AppTheme.DARK
                else -> AppTheme.SYSTEM_DEFAULT
            }
        }
        .distinctUntilChanged()

    override val isLiveTimesAutoRefreshEnabledFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            it[keyAutoRefresh] ?: DEFAULT_AUTO_REFRESH
        }
        .distinctUntilChanged()

    override val isLiveTimesShowNightServicesEnabledFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            it[keyShowNightBuses] ?: DEFAULT_SHOW_NIGHT_BUSES
        }
        .distinctUntilChanged()

    override val isLiveTimesSortByTimeFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            it[keyServiceSorting] ?: DEFAULT_SERVICE_SORTING
        }
        .distinctUntilChanged()

    override val liveTimesNumberOfDeparturesFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            val asString = it[keyNumberOfShownDeparturesPerService]
                ?: DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE.toString()

            try {
                asString.toInt()
            } catch (e: NumberFormatException) {
                exceptionLogger.log(e)
                DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE
            }
        }
        .distinctUntilChanged()

    override val isMapZoomControlsVisibleFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            it[keyZoomButtons] ?: DEFAULT_MAP_ZOOM_BUTTONS
        }
        .distinctUntilChanged()

    override val isGpsPromptDisabledFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            it[keyDisableGpsPrompt] ?: DEFAULT_DISABLE_GPS_PROMPT
        }
        .distinctUntilChanged()

    override val lastMapCameraLocationFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            val zoom = it[keyMapLastZoom] ?: DEFAULT_MAP_LAST_ZOOM

            try {
                LastMapCameraLocation(
                    latitude = it[keyMapLastLatitude]?.toDouble() ?: DEFAULT_LATITUDE,
                    longitude = it[keyMapLastLongitude]?.toDouble() ?: DEFAULT_LONGITUDE,
                    zoomLevel = zoom
                )
            } catch (e: NumberFormatException) {
                exceptionLogger.log(e)

                LastMapCameraLocation(
                    latitude = DEFAULT_LATITUDE,
                    longitude = DEFAULT_LONGITUDE,
                    zoomLevel = zoom
                )
            }
        }
        .distinctUntilChanged()

    override val mapTypeFlow get() = dataStoreSource
        .preferencesFlow
        .map {
            it[keyMapLastType] ?: DEFAULT_MAP_LAST_TYPE
        }
        .distinctUntilChanged()

    override suspend fun toggleSortByTime() {
        dataStoreSource.edit {
            it[keyServiceSorting] = !(it[keyServiceSorting] ?: DEFAULT_SERVICE_SORTING)
        }
    }

    override suspend fun toggleAutoRefresh() {
        dataStoreSource.edit {
            it[keyAutoRefresh] = !(it[keyAutoRefresh] ?: DEFAULT_AUTO_REFRESH)
        }
    }

    override suspend fun setIsGpsPromptDisabled(isDisabled: Boolean) {
        dataStoreSource.edit {
            it[keyDisableGpsPrompt] = isDisabled
        }
    }

    override suspend fun setLastMapCameraLocation(cameraLocation: LastMapCameraLocation) {
        dataStoreSource.edit {
            it[keyMapLastLatitude] = cameraLocation.latitude.toString()
            it[keyMapLastLongitude] = cameraLocation.longitude.toString()
            it[keyMapLastZoom] = cameraLocation.zoomLevel
        }
    }

    override suspend fun setMapType(mapType: Int) {
        dataStoreSource.edit {
            it[keyMapLastType] = mapType
        }
    }
}
