/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation

/**
 * This sealed interface has child classes which encapsulate the possible items shown on the UI.
 *
 * @author Niall Scott
 */
sealed interface UiItem {

    /**
     * Shows a map item on the U.
     *
     * @property latitude The latitude of the point to show on the map.
     * @property longitude The longitude of the point to show on the map.
     * @property orientation The orientation of the marker to show on the map.
     */
    data class Map(
            val latitude: Double,
            val longitude: Double,
            val orientation: StopOrientation) : UiItem

    /**
     * This sealed interface encapsulates the different states of the distance item.
     */
    sealed interface Distance : UiItem {

        /**
         * This item is shown when the distance between the device and the stop is known.
         *
         * @property distanceKilometers The distance, in kilometers, between the device and the
         * stop.
         */
        data class Known(
                val distanceKilometers: Float) : Distance

        /**
         * This item is shown when the distance between the device and the stop is unknown, but not
         * because of insufficient permission or any other known type of issue.
         */
        object Unknown : Distance

        /**
         * This item is shown when the distance cannot be shown because there is insufficient
         * permission access to obtain a location.
         */
        object PermissionDenied : Distance

        /**
         * This item is shown when location is turned off on the device.
         */
        object LocationOff : Distance

        /**
         * This is a pseudo-item: it is not shown. Instead, it acts as a marker to denote the
         * distance item should not be shown as the device does not have a location feature.
         */
        object NoLocationFeature : Distance
    }

    /**
     * This item is a service which is known to call at this stop.
     *
     * @property id The ID of the item.
     * @property name The name of the service.
     * @property description A description of this service.
     * @property colour The colour of this service, if one is set.
     */
    data class Service(
            val id: Long,
            val name: String,
            val description: String?,
            val colour: Int?) : UiItem

    /**
     * This item is shown when there are no known services for the stop.
     */
    object NoServices : UiItem
}