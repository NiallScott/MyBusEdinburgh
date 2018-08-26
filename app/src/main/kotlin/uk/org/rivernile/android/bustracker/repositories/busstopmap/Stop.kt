/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.repositories.busstopmap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * This class describes a stop to be rendered on the map.
 *
 * @author Niall Scott
 * @property stopCode The stop code.
 * @property stopName The human friendly name for the stop.
 * @property latitude The latitude of the stop.
 * @property longitude The longitude of the stop.
 * @property orientation The orientation of the stop.
 */
data class Stop(val stopCode: String,
                val stopName: String,
                val latitude: Double,
                val longitude: Double,
                val orientation: Int) : ClusterItem {

    private val latLng = LatLng(latitude, longitude)

    override fun getPosition() = latLng

    override fun getTitle() = stopName

    override fun getSnippet() = null
}