/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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

/**
 * This interface should be used to access application preferences.
 *
 * @author Niall Scott
 */
interface PreferenceManager {

    companion object {

        /** The name of the preferences file. */
        const val PREF_FILE = "preferences"

        /** The Preference for clearing the map search history. */
        const val PREF_CLEAR_SEARCH_HISTORY = "pref_clear_search_history"

        /** The Preference for number of shown departures per service. */
        const val PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE =
                "pref_numberOfShownDeparturesPerService"
    }

    /**
     * Add a new [OnPreferenceChangedListener]. This goes via a [PreferenceListener] object so that
     * further listening properties are specified.
     *
     * @param listener The listener to add.
     */
    fun addOnPreferenceChangedListener(listener: PreferenceListener)

    /**
     * Remove a [OnPreferenceChangedListener].
     *
     * @param listener The listener to remove.
     */
    fun removeOnPreferenceChangedListener(listener: OnPreferenceChangedListener)

    /**
     * Should the bus stop database only be updated over Wi-Fi?
     *
     * @return `true` if the bus stop database should only be updated over Wi-Fi, `false` if not.
     */
    fun isBusStopDatabaseUpdateWifiOnly(): Boolean

    /**
     * Should system notifications include sound?
     *
     * @return `true` if system notifications should include sound, `false` if not.
     */
    fun isNotificationWithSound(): Boolean

    /**
     * Should system notifications include vibration?
     *
     * @return `true` if system notifications should include vibration, `false` if not.
     */
    fun isNotificationWithVibration(): Boolean

    /**
     * Should system notification include LED lights?
     *
     * @return `true` if system notifications should include LED lights, `false` if not.
     */
    fun isNotificationWithLed(): Boolean

    /**
     * Is bus times auto refresh enabled?
     *
     * @return `true` if bus times auto refresh is enabled, `false` if not.
     */
    fun isBusTimesAutoRefreshEnabled(): Boolean

    /**
     * Set whether auto-refresh should be enabled or not.
     *
     * @param autoRefresh `true` if auto-refresh should be enabled, otherwise `false`.
     */
    fun setBusTimesAutoRefreshEnabled(autoRefresh: Boolean)

    /**
     * Should the bus times display show night services?
     *
     * @return `true` if the bus times display should show night services, `false` if not.
     */
    fun isBusTimesShowingNightServices(): Boolean

    /**
     * Are bus times sorted by time?
     *
     * @return `true` if bus times are sorted by time, `false` if by service.
     */
    fun isBusTimesSortedByTime(): Boolean

    /**
     * Set whether bus times should be sorted by time or not.
     *
     * @param sortedByTime `true` if bus times should be sorted by time, `false` if by service.
     */
    fun setBusTimesSortedByTime(sortedByTime: Boolean)

    /**
     * Get the number of departures to show per service on the bus times display.
     *
     * @return The number of departures to show per service on the bus times display.
     */
    fun getBusTimesNumberOfDeparturesToShowPerService(): Int

    /**
     * Is the map zoom buttons shown?
     *
     * @return `true` if the map zoom buttons are shown, `false` if not.
     */
    fun isMapZoomButtonsShown(): Boolean

    /**
     * Is the GPS prompt disabled?
     *
     * @return `true` if the GPS prompt is disabled, `false` if not.
     */
    fun isGpsPromptDisabled(): Boolean

    /**
     * Set whether the GPS prompt is disabled or not.
     *
     * @param disabled `true` if the GPS prompt should be disabled, `false` if not.
     */
    fun setGpsPromptDisabled(disabled: Boolean)

    /**
     * Get the last known latitude the user saw on the map.
     *
     * @return The last known latitude the user saw on the map.
     */
    fun getLastMapLatitude(): Double

    /**
     * Set the last known latitude the user saw on the map.
     *
     * @param latitude The last known latitude the user saw on the map.
     */
    fun setLastMapLatitude(latitude: Double)

    /**
     * Get the last known longitude the user saw on the map.
     *
     * @return The last known longitude the user saw on the map.
     */
    fun getLastMapLongitude(): Double

    /**
     * Set the last known longitude the user saw on the map.
     *
     * @param longitude The last known longitude the user saw on the map.
     */
    fun setLastMapLongitude(longitude: Double)

    /**
     * Get the last known zoom level the user saw on the map.
     *
     * @return The last known zoom level the user saw on the map.
     */
    fun getLastMapZoomLevel(): Float

    /**
     * Set the last known zoom level the user saw on the map.
     *
     * @param zoomLevel The last known zoom level the user saw on the map.
     */
    fun setLastMapZoomLevel(zoomLevel: Float)

    /**
     * Get the last known map type.
     *
     * @return The last known map type.
     */
    fun getLastMapType(): Int

    /**
     * Set the last known map type.
     *
     * @param mapType The last known map type.
     */
    fun setLastMapType(mapType: Int)

    /**
     * Get the timestamp of when the last check was for a bus stop database update.
     *
     * @return The timestamp of when the last check was for a bus stop database update.
     */
    fun getBusStopDatabaseUpdateLastCheckTimestamp(): Long

    /**
     * Set the timestamp of when the last check was for a bus stop database update.
     *
     * @param timestamp The timestamp of when the last check was for a bus stop database update.
     */
    fun setBusStopDatabaseUpdateLastCheckTimestamp(timestamp: Long)
}
