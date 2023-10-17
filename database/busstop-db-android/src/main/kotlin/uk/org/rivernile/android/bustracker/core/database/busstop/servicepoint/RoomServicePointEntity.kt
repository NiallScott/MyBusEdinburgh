/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * This Room [Entity] contains the service points (used for drawing routes).
 *
 * @property id The ID of the item.
 * @property serviceId The ID of the service this point is for.
 * @property stopId The ID of the stop this point is for.
 * @property orderValue Used to order the results for correct route generating.
 * @property chainage An ID of the group of points this point belongs to.
 * @property latitude The latitude of the point.
 * @property longitude The longitude of the point.
 * @author Niall Scott
 */
@Entity(
    tableName = "service_point",
    indices = [
        Index(
            name = "service_point_index",
            value = [ "serviceId", "chainage", "orderValue" ]
        )
    ])
internal data class RoomServicePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val serviceId: Int,
    val stopId: Int?,
    val orderValue: Int,
    val chainage: Int,
    val latitude: Double,
    val longitude: Double)