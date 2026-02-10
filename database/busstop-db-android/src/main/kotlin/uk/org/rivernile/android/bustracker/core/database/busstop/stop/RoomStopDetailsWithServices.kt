/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.stop

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import uk.org.rivernile.android.bustracker.core.database.busstop.service.RoomServiceDescriptor
import uk.org.rivernile.android.bustracker.core.database.busstop.service.RoomServiceView
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.RoomServiceStopEntity
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier

/**
 *The Room specific implementation of [StopDetailsWithServices].
 *
 * @property id The ID of the stop.
 * @property naptanStopIdentifier See [StopDetailsWithServices.naptanStopIdentifier].
 * @property stopName See [StopDetailsWithServices.stopName].
 * @property location See [StopDetailsWithServices.location].
 * @property orientation See [StopDetailsWithServices.orientation].
 * @author Niall Scott
 */
internal data class RoomStopDetailsWithServices(
    val id: Int,
    @ColumnInfo("naptan_code") override val naptanStopIdentifier: NaptanStopIdentifier,
    @Embedded override val stopName: RoomStopName,
    @Embedded override val location: RoomStopLocation,
    @ColumnInfo("bearing") override val orientation: StopOrientation,
    @Relation(
        entity = RoomServiceView::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RoomServiceStopEntity::class,
            parentColumn = "stop_id",
            entityColumn = "service_id"
        )
    )
    override val serviceListing: List<RoomServiceDescriptor>?
) : StopDetailsWithServices
