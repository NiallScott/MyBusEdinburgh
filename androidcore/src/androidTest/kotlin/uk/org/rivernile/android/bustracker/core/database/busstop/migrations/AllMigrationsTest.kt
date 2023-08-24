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

package uk.org.rivernile.android.bustracker.core.database.busstop.migrations

import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.BundledDatabaseOpenHelperFactory
import uk.org.rivernile.android.bustracker.core.database.busstop.RoomBusStopDatabase
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import kotlin.test.fail

/**
 * Test all database migrations, from version 1 up to the current database version.
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AllMigrationsTest {

    companion object {

        private const val TEST_DB = "stopdb-migration-test"
    }

    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    @Test
    fun migrateAll() {
        val databaseFile = context.getDatabasePath(TEST_DB)

        if (databaseFile.exists() && !databaseFile.delete()) {
            fail("Unable to delete existing database prior to test.")
        }

        val openHelperFactory = BundledDatabaseOpenHelperFactory(
            context,
            FrameworkSQLiteOpenHelperFactory(),
            exceptionLogger)

        Room.databaseBuilder(
            context,
            RoomBusStopDatabase::class.java,
            TEST_DB)
            .addMigrations(*allMigrations)
            .openHelperFactory(openHelperFactory)
            .build()
            .openHelper
            .readableDatabase
            .close()
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val allMigrations get() = arrayOf(Migration1To2())
}