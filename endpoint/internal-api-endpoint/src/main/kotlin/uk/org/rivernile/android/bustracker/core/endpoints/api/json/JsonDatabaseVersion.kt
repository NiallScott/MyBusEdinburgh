/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

/**
 * This class represents the JSON structure of the database version.
 *
 * @property schemaVersion The version of the database schema.
 * @property topologyId The topology ID to represent the version of the data.
 * @property databaseUrl The URL of the database.
 * @property checksum A checksum to use to verify the consistency of the database file.
 * @author Niall Scott
 */
@Serializable
internal data class JsonDatabaseVersion(
    @SerialName("db_schema_version") val schemaVersion: String? = null,
    @SerialName("topo_id") val topologyId: String? = null,
    @SerialName("db_url") val databaseUrl: String? = null,
    @SerialName("checksum") val checksum: String? = null
)