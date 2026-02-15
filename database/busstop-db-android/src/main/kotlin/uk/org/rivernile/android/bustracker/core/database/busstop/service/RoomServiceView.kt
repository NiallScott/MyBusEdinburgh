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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Entity

internal const val CREATE_SERVICE_VIEW_STATEMENT =
    "SELECT service.id AS id, service.name AS name, operator.reference AS operator_code, " +
        "description, colour_primary, colour_on_primary " +
        "FROM service " +
        "LEFT JOIN operator ON operator_id = operator.id"

/**
 * This Room [Entity] contains service details.
 *
 * @property id The ID of the service.
 * @property name The display name of the service.
 * @property operatorCode The code of the operator to look up in the `operator` table.
 * @property description The service description (i.e. its route).
 * @property colourPrimary The primary colour to attribute to the service, if available.
 * @property colourOnPrimary The colour to disable on top of the primary colour of the service, if
 * available.
 * @author Niall Scott
 */
@DatabaseView(
    value = CREATE_SERVICE_VIEW_STATEMENT,
    viewName = "service_view"
)
internal data class RoomServiceView(
    val id: Int,
    val name: String,
    @ColumnInfo("operator_code") val operatorCode: String,
    val description: String,
    @ColumnInfo("colour_primary") val colourPrimary: String?,
    @ColumnInfo("colour_on_primary") val colourOnPrimary: String?
)
