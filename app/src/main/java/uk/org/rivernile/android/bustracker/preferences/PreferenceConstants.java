/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

/**
 * This class contains {@code static final} fields related to application
 * preferences.
 * 
 * @author Niall Scott
 */
public final class PreferenceConstants {
    
    /** The name of the preferences file. */
    public static final String PREF_FILE = "preferences";
    
    /*
     ********************
     * User preferences *
     ********************
     */
    
    /** The Preference for showing the favourites list on app startup. */
    public static final String PREF_STARTUP_SHOW_FAVS =
            "pref_startupshowfavs_state";
    /** The Preference for updating the bus stop database over Wi-Fi only. */
    public static final String PREF_BUS_STOP_DATABASE_WIFI_ONLY =
            "pref_bus_stop_database_wifi_only";
    /** The Preference for backing up favourites. */
    public static final String PREF_BACKUP_FAVOURITES =
            "pref_backup_favourites";
    /** The Preference for restoring favourites. */
    public static final String PREF_RESTORE_FAVOURITES =
            "pref_restore_favourites";
    /** The Preference for alert sounds. */
    public static final String PREF_ALERT_SOUND = "pref_alertsound_state";
    /** The Preference for alert vibration. */
    public static final String PREF_ALERT_VIBRATE = "pref_alertvibrate_state";
    /** The Preference for alert lights (LED flash). */
    public static final String PREF_ALERT_LED = "pref_alertled_state";
    /** The Preference for auto refresh. */
    public static final String PREF_AUTO_REFRESH = "pref_autorefresh_state";
    /** The Preference for showing night bus services. */
    public static final String PREF_SHOW_NIGHT_BUSES =
            "pref_nightservices_state";
    /** The Preference for service sorting. */
    public static final String PREF_SERVICE_SORTING =
            "pref_servicessorting_state";
    /** The Preference for number of shown departures per service. */
    public static final String PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE =
            "pref_numberOfShownDeparturesPerService";
    /** The Preference for automatically showing device location. */
    public static final String PREF_AUTO_LOCATION = "pref_autolocation_state";
    /** The Preference for showing zoom buttons on the map. */
    public static final String PREF_ZOOM_BUTTONS =
            "pref_map_zoom_buttons_state";
    /** The Preference for clearing the map search history. */
    public static final String PREF_CLEAR_MAP_SEARCH_HISTORY =
            "pref_clear_search_history";
    /** The Preference for disabling the GPS prompt. */
    public static final String PREF_DISABLE_GPS_PROMPT =
            "neareststops_gps_prompt_disable";
    
    /*
     *********************
     * State preferences *
     *********************
     */
    
    /** The Preference for the last known map latitude (not shown). */
    public static final String PREF_MAP_LAST_LATITUDE =
            "pref_map_last_latitude";
    /** The Preference for the last known map longitude (not shown). */
    public static final String PREF_MAP_LAST_LONGITUDE =
            "pref_map_last_longitude";
    /** The Preference for the last known map zoom level (not shown). */
    public static final String PREF_MAP_LAST_ZOOM = "pref_map_last_zoom";
    /** The Preference for the last known map type (not shown). */
    public static final String PREF_MAP_LAST_MAP_TYPE =
            "pref_map_last_map_type";
    public static final String PREF_DATABASE_UPDATE_LAST_CHECK =
            "pref_database_update_last_check";
    
    /**
     * This private constructor exists to prevent instantiation of this class.
     */
    private PreferenceConstants() {
        // Intentionally left blank.
    }
}