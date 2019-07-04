/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import android.content.SharedPreferences
import java.lang.NumberFormatException

/**
 * This is the Android-specific implementation of [PreferenceManager].
 *
 * @author Niall Scott
 */
internal class AndroidPreferenceManager(private val preferences: SharedPreferences)
    : PreferenceManager, SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {

        private const val PREF_BUS_STOP_DATABASE_WIFI_ONLY = "pref_bus_stop_database_wifi_only"
        private const val PREF_ALERT_SOUND = "pref_alertsound_state"
        private const val PREF_ALERT_VIBRATE = "pref_alertvibrate_state"
        private const val PREF_ALERT_LED = "pref_alertled_state"
        private const val PREF_AUTO_REFRESH = "pref_autorefresh_state"
        private const val PREF_SHOW_NIGHT_BUSES = "pref_nightservices_state"
        private const val PREF_SERVICE_SORTING = "pref_servicessorting_state"
        private const val PREF_ZOOM_BUTTONS = "pref_map_zoom_buttons_state"
        private const val PREF_DISABLE_GPS_PROMPT = "neareststops_gps_prompt_disable"
        private const val PREF_MAP_LAST_LATITUDE = "pref_map_last_latitude"
        private const val PREF_MAP_LAST_LONGITUDE = "pref_map_last_longitude"
        private const val PREF_MAP_LAST_ZOOM = "pref_map_last_zoom"
        private const val PREF_MAP_LAST_MAP_TYPE = "pref_map_last_map_type"
        private const val PREF_DATABASE_UPDATE_LAST_CHECK = "pref_database_update_last_check"

        private const val DEFAULT_WIFI_ONLY = false
        private const val DEFAULT_ALERT_SOUND = true
        private const val DEFAULT_ALERT_VIBRATE = true
        private const val DEFAULT_ALERT_LED = true
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
    }

    internal var wifiOnlyChangedListener: (() -> Unit)? = null

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun isBusStopDatabaseUpdateWifiOnly(): Boolean =
        preferences.getBoolean(PREF_BUS_STOP_DATABASE_WIFI_ONLY, DEFAULT_WIFI_ONLY)

    override fun isNotificationWithSound(): Boolean =
            preferences.getBoolean(PREF_ALERT_SOUND, DEFAULT_ALERT_SOUND)

    override fun isNotificationWithVibration(): Boolean =
            preferences.getBoolean(PREF_ALERT_VIBRATE, DEFAULT_ALERT_VIBRATE)

    override fun isNotificationWithLed(): Boolean =
            preferences.getBoolean(PREF_ALERT_LED, DEFAULT_ALERT_LED)

    override fun isBusTimesAutoRefreshEnabled(): Boolean =
            preferences.getBoolean(PREF_AUTO_REFRESH, DEFAULT_AUTO_REFRESH)

    override fun isBusTimesShowingNightServices(): Boolean =
            preferences.getBoolean(PREF_SHOW_NIGHT_BUSES, DEFAULT_SHOW_NIGHT_BUSES)

    override fun isBusTimesSortedByTime(): Boolean =
            preferences.getBoolean(PREF_SERVICE_SORTING, DEFAULT_SERVICE_SORTING)

    override fun setBusTimesSortedByTime(sortedByTime: Boolean) {
        preferences.edit()
                .putBoolean(PREF_SERVICE_SORTING, sortedByTime)
                .apply()
    }

    override fun getBusTimesNumberOfDeparturesToShowPerService(): Int {
        val asString = preferences.getString(
                PreferenceManager.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE,
                DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE.toString())

        return try {
            asString?.toInt() ?: DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE
        } catch (ignored: NumberFormatException) {
            DEFAULT_NUMBER_OF_DEPARTURES_PER_SERVICE
        }
    }

    override fun isMapZoomButtonsShown(): Boolean =
            preferences.getBoolean(PREF_ZOOM_BUTTONS, DEFAULT_MAP_ZOOM_BUTTONS)

    override fun isGpsPromptDisabled(): Boolean =
            preferences.getBoolean(PREF_DISABLE_GPS_PROMPT, DEFAULT_DISABLE_GPS_PROMPT)

    override fun setGpsPromptDisabled(disabled: Boolean) {
        preferences.edit()
                .putBoolean(PREF_DISABLE_GPS_PROMPT, disabled)
                .apply()
    }

    override fun getLastMapLatitude(): Double {
        val asString = preferences.getString(PREF_MAP_LAST_LATITUDE, DEFAULT_LATITUDE.toString())

        return try {
            asString?.toDouble() ?: DEFAULT_LATITUDE
        } catch (ignored: NumberFormatException) {
            DEFAULT_LATITUDE
        }
    }

    override fun setLastMapLatitude(latitude: Double) {
        preferences.edit()
                .putString(PREF_MAP_LAST_LATITUDE, latitude.toString())
                .apply()
    }

    override fun getLastMapLongitude(): Double {
        val asString = preferences.getString(PREF_MAP_LAST_LONGITUDE, DEFAULT_LONGITUDE.toString())

        return try {
            asString?.toDouble() ?: DEFAULT_LONGITUDE
        } catch (ignored: NumberFormatException) {
            DEFAULT_LONGITUDE
        }
    }

    override fun setLastMapLongitude(longitude: Double) {
        preferences.edit()
                .putString(PREF_MAP_LAST_LONGITUDE, longitude.toString())
                .apply()
    }

    override fun getLastMapZoomLevel(): Float =
            preferences.getFloat(PREF_MAP_LAST_ZOOM, DEFAULT_MAP_LAST_ZOOM)

    override fun setLastMapZoomLevel(zoomLevel: Float) {
        preferences.edit()
                .putFloat(PREF_MAP_LAST_ZOOM, zoomLevel)
                .apply()
    }

    override fun getLastMapType(): Int =
            preferences.getInt(PREF_MAP_LAST_MAP_TYPE, DEFAULT_MAP_LAST_TYPE)

    override fun setLastMapType(mapType: Int) {
        preferences.edit()
                .putInt(PREF_MAP_LAST_MAP_TYPE, mapType)
                .apply()
    }

    override fun getBusStopDatabaseUpdateLastCheckTimestamp(): Long =
            preferences.getLong(PREF_DATABASE_UPDATE_LAST_CHECK, 0L)

    override fun setBusStopDatabaseUpdateLastCheckTimestamp(timestamp: Long) {
        preferences.edit()
                .putLong(PREF_DATABASE_UPDATE_LAST_CHECK, timestamp)
                .apply()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == PREF_BUS_STOP_DATABASE_WIFI_ONLY) {
            wifiOnlyChangedListener?.invoke()
        }
    }
}