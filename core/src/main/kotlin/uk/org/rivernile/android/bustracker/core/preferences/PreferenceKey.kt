/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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
 * This enum specifies preference keys which can be listened to for changes.
 *
 * @author Niall Scott
 */
enum class PreferenceKey {

    /**
     * Key for the preference that specifies if the stop database update should occur over Wi-Fi
     * only.
     */
    DATABASE_UPDATE_WIFI_ONLY,
    /**
     * Key for the preferences that specifies the app theme.
     */
    APP_THEME,
    /**
     * Key for the preference that specifies if auto refresh is enabled by default.
     */
    LIVE_TIMES_AUTO_REFRESH_ENABLED,
    /**
     * Key for the preference that specifies if night services should be shown.
     */
    LIVE_TIMES_SHOW_NIGHT_SERVICES,
    /**
     * Key for the preference that specifies if the times should be shown in time order.
     */
    LIVE_TIMES_SORT_BY_TIME,
    /**
     * Key for the preference that specifies the number of departures that should be shown per
     * service.
     */
    LIVE_TIMES_NUMBER_OF_DEPARTURES,
    /**
     * Key for the preference that specifies whether the zoom controls should be shown on the map.
     */
    STOP_MAP_SHOW_ZOOM_CONTROLS,
    /**
     * Key for the preference that specifies what map type should be shown.
     */
    STOP_MAP_TYPE
}