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

package uk.org.rivernile.android.bustracker.core.database.settings.favouritestops

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This Room [Entity] is used for storage of favourite stops.
 *
 * @author Niall Scott
 */
@Entity(
    tableName = "favourite_stop"
)
internal data class RoomFavouriteStopEntity(
    @PrimaryKey @ColumnInfo("stop_code") override val stopIdentifier: StopIdentifier,
    @ColumnInfo("stop_name") override val stopName: String
) : FavouriteStop

internal fun FavouriteStop.toFavouriteStopEntity(): RoomFavouriteStopEntity {
    return RoomFavouriteStopEntity(
        stopIdentifier = stopIdentifier,
        stopName = stopName
    )
}
