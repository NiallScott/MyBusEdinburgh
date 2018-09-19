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

/**
 * This interface should be used to access application preferences.
 *
 * @author Niall Scott
 */
public interface PreferenceManager {

    /** The name of the preferences file. */
    String PREF_FILE = "preferences";

    /** The preference for backing up favourites. */
    String PREF_BACKUP_FAVOURITES = "pref_backup_favourites";
    /** The preference for restoring favourites. */
    String PREF_RESTORE_FAVOURITES = "pref_restore_favourites";
    /** The Preference for clearing the map search history. */
    String PREF_CLEAR_MAP_SEARCH_HISTORY = "pref_clear_search_history";

    /** The Preference for number of shown departures per service. */
    String PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE = "pref_numberOfShownDeparturesPerService";

    /**
     * Should the bus stop database only be updated over Wi-Fi?
     *
     * @return {@code true} if the bus stop database should only be updated over Wi-Fi,
     * {@code false} if not.
     */
    boolean isBusStopDatabaseUpdateWifiOnly();

    /**
     * Should system notifications include sound?
     *
     * @return {@code true} if system notifications should include sound, {@code false} if not.
     */
    boolean isNotificationWithSound();

    /**
     * Should system notifications include vibration?
     *
     * @return {@code true} if system notifications should include vibration, {@code false} if not.
     */
    boolean isNotificationWithVibration();

    /**
     * Should system notification include LED lights?
     *
     * @return {@code true} if system notifications should include LED lights, {@code false} if not.
     */
    boolean isNotificationWithLed();

    /**
     * Is bus times auto refresh enabled?
     *
     * @return {@code true} if bus times auto refresh is enabled, {@code false} if not.
     */
    boolean isBusTimesAutoRefreshEnabled();

    /**
     * Should the bus times display show night services?
     *
     * @return {@code true} if the bus times display should show night services, {@code false} if
     * not.
     */
    boolean isBusTimesShowingNightServices();

    /**
     * Are bus times sorted by time?
     *
     * @return {@code true} if bus times are sorted by time, {@code false} if by service.
     */
    boolean isBusTimesSortedByTime();

    /**
     * Set whether bus times should be sorted by time or not.
     *
     * @param sortedByTime {@code true} if bus times should be sorted by time, {@code false} if by
     * service.
     */
    void setBusTimesSortedByTime(boolean sortedByTime);

    /**
     * Get the number of departures to show per service on the bus times display.
     *
     * @return The number of departures to show per service on the bus times display.
     */
    int getBusTimesNumberOfDeparturesToShowPerService();

    /**
     * Is the map zoom buttons shown?
     *
     * @return {@code true} if the map zoom buttons are shown, {@code false} if not.
     */
    boolean isMapZoomButtonsShown();

    /**
     * Is the GPS prompt disabled?
     *
     * @return {@code true} if the GPS prompt is disabled, {@code false} if not.
     */
    boolean isGpsPromptDisabled();

    /**
     * Set whether the GPS prompt is disabled or not.
     *
     * @param disabled {@code true} if the GPS prompt should be disabled, {@code false} if not.
     */
    void setGpsPromptDisabled(boolean disabled);

    /**
     * Get the last known latitude the user saw on the map.
     *
     * @return The last known latitude the user saw on the map.
     */
    double getLastMapLatitude();

    /**
     * Set the last known latitude the user saw on the map.
     *
     * @param latitude The last known latitude the user saw on the map.
     */
    void setLastMapLatitude(double latitude);

    /**
     * Get the last known longitude the user saw on the map.
     *
     * @return The last known longitude the user saw on the map.
     */
    double getLastMapLongitude();

    /**
     * Set the last known longitude the user saw on the map.
     *
     * @param longitude The last known longitude the user saw on the map.
     */
    void setLastMapLongitude(double longitude);

    /**
     * Get the last known zoom level the user saw on the map.
     *
     * @return The last known zoom level the user saw on the map.
     */
    float getLastMapZoomLevel();

    /**
     * Set the last known zoom level the user saw on the map.
     *
     * @param zoomLevel The last known zoom level the user saw on the map.
     */
    void setLastMapZoomLevel(float zoomLevel);

    /**
     * Get the last known map type.
     *
     * @return The last known map type.
     */
    int getLastMapType();

    /**
     * Set the last known map type.
     *
     * @param mapType The last known map type.
     */
    void setLastMapType(int mapType);

    /**
     * Get the timestamp of when the last check was for a bus stop database update.
     *
     * @return The timestamp of when the last check was for a bus stop database update.
     */
    long getBusStopDatabaseUpdateLastCheckTimestamp();

    /**
     * Set the timestamp of when the last check was for a bus stop database update.
     *
     * @param timestamp The timestamp of when the last check was for a bus stop database update.
     */
    void setBusStopDatabaseUpdateLastCheckTimestamp(long timestamp);
}
