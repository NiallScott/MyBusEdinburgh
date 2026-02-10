/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.api.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion

/**
 * This class represents the JSON structure of the database version.
 *
 * @property schemaName The name of the schema/app this database version is for.
 * @property schemaVersionCode The version code of the schema.
 * @property timestampInSeconds The timestamp the database was created at, in UNIX timestamp
 * seconds.
 * @property databaseUrl The URL of the database.
 * @property sha256Checksum The SHA-256 checksum of the database to ensure data integrity.
 * @author Niall Scott
 */
@Serializable
internal data class JsonDatabaseVersion(
    @SerialName("schemaName") val schemaName: String? = null,
    @SerialName("schemaVersionCode") val schemaVersionCode: Int? = null,
    @SerialName("unixTimestampSeconds") val timestampInSeconds: Long? = null,
    @SerialName("databaseUrl") val databaseUrl: String? = null,
    @SerialName("sha256Checksum") val sha256Checksum: String? = null
)

/**
 * Verify the schema details are correct.
 *
 * @param expectedSchemaName The expected name of the schema.
 * @param expectedSchemaVersionCode The expected version code of the schema.
 * @return `true` if the schema passes verification, otherwise `false`.
 */
internal fun JsonDatabaseVersion.verify(
    expectedSchemaName: String,
    expectedSchemaVersionCode: Int
): Boolean {
    return expectedSchemaName == schemaName && expectedSchemaVersionCode == schemaVersionCode
}

/**
 * Map this [JsonDatabaseVersion] to a [DatabaseVersion]. If any required fields are missing, then
 * `null` will be returned.
 *
 * @return This [JsonDatabaseVersion] as a [DatabaseVersion], or `null` if this could not be mapped.
 */
internal fun JsonDatabaseVersion.toDatabaseVersionOrNull(): DatabaseVersion? {
    val timestampInSeconds = timestampInSeconds ?: return null
    val databaseUrl = databaseUrl?.ifBlank { null } ?: return null
    val sha256Checksum = sha256Checksum?.ifBlank { null } ?: return null

    return DatabaseVersion(
        timestampInSeconds = timestampInSeconds,
        databaseUrl = databaseUrl,
        sha256Checksum = sha256Checksum
    )
}
