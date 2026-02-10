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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor

/**
 * This is the Room entity definition of an arrival alert service.
 *
 * @property id The ID of the entity.
 * @property arrivalAlertId The ID of the parent arrival alert.
 * @property serviceName The name of the service.
 * @property operatorCode The code of the operator of the service.
 * @author Niall Scott
 */
@Entity(
    tableName = "arrival_alert_service",
    indices = [
        Index(
            name = "arrival_alert_service_arrival_alert_id",
            value = [
                "arrival_alert_id"
            ]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = RoomArrivalAlertEntity::class,
            parentColumns = [
                "id"
            ],
            childColumns = [
                "arrival_alert_id"
            ],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
internal data class RoomArrivalAlertServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo("arrival_alert_id") val arrivalAlertId: Long,
    @ColumnInfo("service_name") val serviceName: String,
    @ColumnInfo("operator_code") val operatorCode: String
)

internal fun Collection<ServiceDescriptor>.toArrivalAlertServiceEntityList(
    arrivalAlertId: Long
): List<RoomArrivalAlertServiceEntity> {
    return map { it.toArrivalAlertServiceEntity(arrivalAlertId) }
}

private fun ServiceDescriptor.toArrivalAlertServiceEntity(
    arrivalAlertId: Long
): RoomArrivalAlertServiceEntity {
    return RoomArrivalAlertServiceEntity(
        id = 0,
        arrivalAlertId = arrivalAlertId,
        serviceName = serviceName,
        operatorCode = operatorCode
    )
}
