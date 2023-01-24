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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName

/**
 * This class describes a stop marker on the Google Map.
 *
 * *Implementation note*
 * This class is used by the Google Maps Utils clustering functionality, which has a somewhat
 * painful interface to work with. Internally, it uses the [ClusterItem] as a key (this class
 * implements [ClusterItem]). This means the [equals] and [hashCode] methods need to return a
 * stable value, and the [stopCode] is our key for this marker.
 *
 * If this class were a `data class`, it would generate [equals], [hashCode] and [toString]
 * implementations for all fields, meaning that if any field changed then so would the results
 * yielded by [equals] and [hashCode], and Google Maps Utils clustering would deem it a new marker,
 * despite only the data changing, which produces weird visual artefacts.
 *
 * To do real equality checks, the [deepEquals] method is available.
 *
 * TL;DR; for reasons, [equals] and [hashCode] are only calculated from the [stopCode] and no other
 * field.
 *
 * @property stopCode The stop code of the marker.
 * @property stopName The name properties of the marker.
 * @property latLng The location of the marker as a Google Map [LatLng] object.
 * @property orientation The orientation of the stop icon marker.
 * @property serviceListing An object which describes the service listing for this marker.
 * @author Niall Scott
 */
class UiStopMarker(
        val stopCode: String,
        val stopName: StopName,
        val latLng: LatLng,
        val orientation: Int,
        val serviceListing: UiServiceListing?) : ClusterItem {

    override fun getPosition() = latLng

    override fun getTitle(): String? = null

    override fun getSnippet(): String? = null

    override fun getZIndex(): Float? = null

    /**
     * Should the info window be shown?
     */
    val isInfoWindowShown = serviceListing != null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        other as UiStopMarker

        if (stopCode != other.stopCode) {
            return false
        }

        return true
    }

    override fun hashCode() = stopCode.hashCode()

    override fun toString(): String {
        return "UiStopMarker(" +
                "stopCode='$stopCode', " +
                "stopName=$stopName, " +
                "latLng=$latLng, " +
                "orientation=$orientation, " +
                "serviceListing=$serviceListing, " +
                "isInfoWindowShown=$isInfoWindowShown)"
    }

    /**
     * Perform a deep equality comparison between this instance and the [other] instance.
     *
     * This behaves exactly like [equals] would normally work, except for the reasons specified in
     * the class documentation, we're not able to rely upon [equals], so this method is provided
     * instead.
     *
     * @param other The other object to compare ourselves against.
     * @return `true` if this object is deeply equal to the other object, otherwise `false`.
     */
    fun deepEquals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        other as UiStopMarker

        return stopCode == other.stopCode &&
                stopName == other.stopName &&
                latLng == other.latLng &&
                orientation == other.orientation &&
                serviceListing == other.serviceListing
    }
}