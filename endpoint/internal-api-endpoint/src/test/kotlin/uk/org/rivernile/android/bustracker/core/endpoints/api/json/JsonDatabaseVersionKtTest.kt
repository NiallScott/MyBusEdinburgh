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

package uk.org.rivernile.android.bustracker.core.endpoints.api.json

import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for `JsonDatabaseVersion.kt`.
 *
 * @author Niall Scott
 */
class JsonDatabaseVersionKtTest {

    companion object {

        private const val EXPECTED_SCHEMA_NAME = "SchemaName"
        private const val EXPECTED_SCHEMA_VERSION_CODE = 123
        private const val EXPECTED_TIMESTAMP = 123456L
        private const val EXPECTED_DATABASE_URL = "https://some.url/path"
        private const val EXPECTED_SHA256_CHECKSUM = "abc123"
    }

    @Test
    fun verifyReturnsFalseWhenSchemaNameIsNull() {
        val result = JsonDatabaseVersion(
            schemaName = null,
            schemaVersionCode = 123
        ).verify(EXPECTED_SCHEMA_NAME, EXPECTED_SCHEMA_VERSION_CODE)

        assertFalse(result)
    }

    @Test
    fun verifyReturnsFalseWhenSchemaNameIsEmpty() {
        val result = JsonDatabaseVersion(
            schemaName = "",
            schemaVersionCode = 123
        ).verify(EXPECTED_SCHEMA_NAME, EXPECTED_SCHEMA_VERSION_CODE)

        assertFalse(result)
    }

    @Test
    fun verifyReturnsFalseWhenSchemaNameDoesNotMatchExpected() {
        val result = JsonDatabaseVersion(
            schemaName = "Does not match",
            schemaVersionCode = 123
        ).verify(EXPECTED_SCHEMA_NAME, EXPECTED_SCHEMA_VERSION_CODE)

        assertFalse(result)
    }

    @Test
    fun verifyReturnsFalseWhenSchemaVersionCodeIsNull() {
        val result = JsonDatabaseVersion(
            schemaName = EXPECTED_SCHEMA_NAME,
            schemaVersionCode = null
        ).verify(EXPECTED_SCHEMA_NAME, EXPECTED_SCHEMA_VERSION_CODE)

        assertFalse(result)
    }

    @Test
    fun verifyReturnsFalseWhenSchemaVersionCodeDoesNotMatchExpected() {
        val result = JsonDatabaseVersion(
            schemaName = EXPECTED_SCHEMA_NAME,
            schemaVersionCode = 456
        ).verify(EXPECTED_SCHEMA_NAME, EXPECTED_SCHEMA_VERSION_CODE)

        assertFalse(result)
    }

    @Test
    fun verifyReturnsTrueWhenActualMatchesExpected() {
        val result = JsonDatabaseVersion(
            schemaName = EXPECTED_SCHEMA_NAME,
            schemaVersionCode = EXPECTED_SCHEMA_VERSION_CODE
        ).verify(EXPECTED_SCHEMA_NAME, EXPECTED_SCHEMA_VERSION_CODE)

        assertTrue(result)
    }

    @Test
    fun toDatabaseVersionOrNullReturnsNullWhenTimestampInSecondsIsNull() {
        val result = JsonDatabaseVersion(
            timestampInSeconds = null,
            databaseUrl = EXPECTED_DATABASE_URL,
            sha256Checksum = EXPECTED_SHA256_CHECKSUM
        ).toDatabaseVersionOrNull()

        assertNull(result)
    }

    @Test
    fun toDatabaseVersionOrNullReturnsNullWhenDatabaseUrlIsNull() {
        val result = JsonDatabaseVersion(
            timestampInSeconds = EXPECTED_TIMESTAMP,
            databaseUrl = null,
            sha256Checksum = EXPECTED_SHA256_CHECKSUM
        ).toDatabaseVersionOrNull()

        assertNull(result)
    }

    @Test
    fun toDatabaseVersionOrNullReturnsNullWhenDatabaseUrlIsEmpty() {
        val result = JsonDatabaseVersion(
            timestampInSeconds = EXPECTED_TIMESTAMP,
            databaseUrl = "",
            sha256Checksum = EXPECTED_SHA256_CHECKSUM
        ).toDatabaseVersionOrNull()

        assertNull(result)
    }

    @Test
    fun toDatabaseVersionOrNullReturnsNullWhenSha256ChecksumIsNull() {
        val result = JsonDatabaseVersion(
            timestampInSeconds = EXPECTED_TIMESTAMP,
            databaseUrl = EXPECTED_DATABASE_URL,
            sha256Checksum = null
        ).toDatabaseVersionOrNull()

        assertNull(result)
    }

    @Test
    fun toDatabaseVersionOrNullReturnsNullWhenSha256ChecksumIsEmpty() {
        val result = JsonDatabaseVersion(
            timestampInSeconds = EXPECTED_TIMESTAMP,
            databaseUrl = EXPECTED_DATABASE_URL,
            sha256Checksum = ""
        ).toDatabaseVersionOrNull()

        assertNull(result)
    }

    @Test
    fun toDatabaseVersionOrNullReturnsDatabaseVersionWhenPropertiesExist() {
        val result = JsonDatabaseVersion(
            timestampInSeconds = EXPECTED_TIMESTAMP,
            databaseUrl = EXPECTED_DATABASE_URL,
            sha256Checksum = EXPECTED_SHA256_CHECKSUM
        ).toDatabaseVersionOrNull()

        assertEquals(
            DatabaseVersion(
                timestampInSeconds = EXPECTED_TIMESTAMP,
                databaseUrl = EXPECTED_DATABASE_URL,
                sha256Checksum = EXPECTED_SHA256_CHECKSUM
            ),
            result
        )
    }
}
