/*
 * Copyright (C) 2017 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * This is a concrete implementation of the {@link PreferenceManager} that uses Android
 * {@link SharedPreferences} as the storage mechanism.
 *
 * @author Niall Scott
 */
public class PreferenceManagerImpl implements PreferenceManager {

    private static final String PREF_BUS_STOP_DATABASE_WIFI_ONLY =
            "pref_bus_stop_database_wifi_only";
    private static final String PREF_ALERT_SOUND = "pref_alertsound_state";
    private static final String PREF_ALERT_VIBRATE = "pref_alertvibrate_state";
    private static final String PREF_ALERT_LED = "pref_alertled_state";
    private static final String PREF_AUTO_REFRESH = "pref_autorefresh_state";
    private static final String PREF_SHOW_NIGHT_BUSES = "pref_nightservices_state";
    private static final String PREF_SERVICE_SORTING = "pref_servicessorting_state";
    private static final String PREF_ZOOM_BUTTONS = "pref_map_zoom_buttons_state";
    private static final String PREF_DISABLE_GPS_PROMPT = "neareststops_gps_prompt_disable";
    private static final String PREF_MAP_LAST_LATITUDE = "pref_map_last_latitude";
    private static final String PREF_MAP_LAST_LONGITUDE = "pref_map_last_longitude";
    private static final String PREF_MAP_LAST_ZOOM = "pref_map_last_zoom";
    private static final String PREF_MAP_LAST_MAP_TYPE = "pref_map_last_map_type";
    private static final String PREF_DATABASE_UPDATE_LAST_CHECK = "pref_database_update_last_check";

    private static final String DEFAULT_LATITUDE = "55.953";
    private static final String DEFAULT_LONGITUDE = "-3.189";

    private final SharedPreferences preferences;

    /**
     * Create a new {@code PreferenceManagerImpl}.
     *
     * @param context An application {@link Context} instance.
     */
    public PreferenceManagerImpl(@NonNull final Context context) {
        preferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isBusStopDatabaseUpdateWifiOnly() {
        return preferences.getBoolean(PREF_BUS_STOP_DATABASE_WIFI_ONLY, false);
    }

    @Override
    public boolean isNotificationWithSound() {
        return preferences.getBoolean(PREF_ALERT_SOUND, true);
    }

    @Override
    public boolean isNotificationWithVibration() {
        return preferences.getBoolean(PREF_ALERT_VIBRATE, true);
    }

    @Override
    public boolean isNotificationWithLed() {
        return preferences.getBoolean(PREF_ALERT_LED, true);
    }

    @Override
    public boolean isBusTimesAutoRefreshEnabled() {
        return preferences.getBoolean(PREF_AUTO_REFRESH, false);
    }

    @Override
    public boolean isBusTimesShowingNightServices() {
        return preferences.getBoolean(PREF_SHOW_NIGHT_BUSES, true);
    }

    @Override
    public boolean isBusTimesSortedByTime() {
        return preferences.getBoolean(PREF_SERVICE_SORTING, false);
    }

    @Override
    public void setBusTimesSortedByTime(final boolean sortedByTime) {
        preferences.edit()
                .putBoolean(PREF_SERVICE_SORTING, sortedByTime)
                .apply();
    }

    @Override
    public int getBusTimesNumberOfDeparturesToShowPerService() {
        try {
            return Integer.parseInt(
                    preferences.getString(PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE, "4"));
        } catch (NumberFormatException ignored) {
            return 4;
        }
    }

    @Override
    public boolean isMapZoomButtonsShown() {
        return preferences.getBoolean(PREF_ZOOM_BUTTONS, true);
    }

    @Override
    public boolean isGpsPromptDisabled() {
        return preferences.getBoolean(PREF_DISABLE_GPS_PROMPT, false);
    }

    @Override
    public void setGpsPromptDisabled(final boolean disabled) {
        preferences.edit()
                .putBoolean(PREF_DISABLE_GPS_PROMPT, disabled)
                .apply();
    }

    @Override
    public double getLastMapLatitude() {
        try {
            return Double.parseDouble(preferences.getString(PREF_MAP_LAST_LATITUDE,
                    DEFAULT_LATITUDE));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public void setLastMapLatitude(final double latitude) {
        preferences.edit()
                .putString(PREF_MAP_LAST_LATITUDE, String.valueOf(latitude))
                .apply();
    }

    @Override
    public double getLastMapLongitude() {
        try {
            return Double.parseDouble(preferences.getString(PREF_MAP_LAST_LONGITUDE,
                    DEFAULT_LONGITUDE));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public void setLastMapLongitude(final double longitude) {
        preferences.edit()
                .putString(PREF_MAP_LAST_LONGITUDE, String.valueOf(longitude))
                .apply();
    }

    @Override
    public float getLastMapZoomLevel() {
        return preferences.getFloat(PREF_MAP_LAST_ZOOM, 11f);
    }

    @Override
    public void setLastMapZoomLevel(final float zoomLevel) {
        preferences.edit()
                .putFloat(PREF_MAP_LAST_ZOOM, zoomLevel)
                .apply();
    }

    @Override
    public int getLastMapType() {
        return preferences.getInt(PREF_MAP_LAST_MAP_TYPE, 1);
    }

    @Override
    public void setLastMapType(final int mapType) {
        preferences.edit()
                .putInt(PREF_MAP_LAST_MAP_TYPE, mapType)
                .apply();
    }

    @Override
    public long getBusStopDatabaseUpdateLastCheckTimestamp() {
        return preferences.getLong(PREF_DATABASE_UPDATE_LAST_CHECK, 0L);
    }

    @Override
    public void setBusStopDatabaseUpdateLastCheckTimestamp(final long timestamp) {
        preferences.edit()
                .putLong(PREF_DATABASE_UPDATE_LAST_CHECK, timestamp)
                .apply();
    }
}
