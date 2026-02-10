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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import androidx.room.ColumnInfo
import androidx.sqlite.SQLiteStatement
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.calculateHashCode
import uk.org.rivernile.android.bustracker.core.domain.isEquals

/**
 * This is the Room-specific implementation of [ServiceDescriptor].
 *
 * @author Niall Scott
 */
internal class RoomServiceDescriptor(
    @ColumnInfo("name") override val serviceName: String,
    @ColumnInfo("operator_code") override val operatorCode: String
): ServiceDescriptor {

    override fun equals(other: Any?) = isEquals(other)

    override fun hashCode() = calculateHashCode()

    override fun toString(): String {
        return "RoomServiceDescriptor(serviceName='$serviceName', operatorCode='$operatorCode')"
    }
}

internal fun Collection<ServiceDescriptor>.createPlaceholders(): String {
    val size = size

    return if (size > 0) {
        buildString {
            repeat(size) { index ->
                if (index > 0) {
                    append(',')
                }

                append("(?,?)")
            }
        }
    } else {
        ""
    }
}

internal fun Collection<ServiceDescriptor>.bindStatement(
    statement: SQLiteStatement,
    offset: Int = 0
) {
    forEachIndexed { index, serviceDescriptor ->
        val baseIndex = (index * 2) + 1 + offset
        statement.bindText(baseIndex, serviceDescriptor.serviceName)
        statement.bindText(baseIndex + 1, serviceDescriptor.operatorCode)
    }
}
