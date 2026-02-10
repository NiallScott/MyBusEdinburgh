/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import kotlin.time.Instant

/**
 * This is the Room entity definition of a proximity alert.
 *
 * @property id The ID of the alert.
 * @property timeAdded The time the alert was created at as a UNIX timestamp in milliseconds.
 * @property stopIdentifier The stop for the proximity alert.
 * @property radiusTriggerMeters The radius, in meters, within the stop location that a proximity
 * alert should be triggered for.
 * @author Niall Scott
 */
@Entity(
    tableName = "proximity_alert",
    indices = [
        Index(
            name = "proximity_alert_stop_code",
            value = [
                "stop_code"
            ],
            unique = true
        )
    ]
)
internal data class RoomProximityAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo("time_added_millis") val timeAdded: Instant,
    @ColumnInfo("stop_code") val stopIdentifier: StopIdentifier,
    @ColumnInfo("radius_trigger_meters") val radiusTriggerMeters: Int
)

internal fun ProximityAlert.toProximityAlertEntity(): RoomProximityAlertEntity {
    return RoomProximityAlertEntity(
        id = id,
        timeAdded = timeAdded,
        stopIdentifier = stopIdentifier,
        radiusTriggerMeters = radiusTriggerMeters
    )
}
