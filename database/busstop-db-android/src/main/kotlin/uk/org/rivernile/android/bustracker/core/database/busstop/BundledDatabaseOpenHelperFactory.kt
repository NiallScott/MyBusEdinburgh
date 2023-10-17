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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject

/**
 * A [SupportSQLiteOpenHelper.Factory] which creates [BundledDatabaseOpenHelper] instances.
 *
 * @param context The application [Context].
 * @param frameworkSQLiteOpenHelperFactory Used to open framework connections to databases.
 * @param databaseOpener An implementation used to open the database to peek in to it before it is
 * handed off to Room.
 * @param exceptionLogger Used to log handled exceptions.
 * @author Niall Scott
 */
internal class BundledDatabaseOpenHelperFactory @Inject constructor(
    private val context: Context,
    private val frameworkSQLiteOpenHelperFactory: FrameworkSQLiteOpenHelperFactory,
    private val databaseOpener: DatabaseOpener,
    private val exceptionLogger: ExceptionLogger)
    : SupportSQLiteOpenHelper.Factory {

    companion object {

        private const val ASSET_PREPACKAGED_DATABASE_PATH = "busstops10.db"
    }

    override fun create(
        configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        return BundledDatabaseOpenHelper(
            context,
            frameworkSQLiteOpenHelperFactory.create(configuration),
            context.getString(R.string.asset_db_version).toLong(),
            ASSET_PREPACKAGED_DATABASE_PATH,
            databaseOpener,
            exceptionLogger)
    }
}