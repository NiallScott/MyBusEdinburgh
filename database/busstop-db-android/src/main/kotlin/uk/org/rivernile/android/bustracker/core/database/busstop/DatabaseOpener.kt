/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import okio.IOException
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 * This class allows a database to be opened to be peeked at prior to handing it off to Room.
 *
 * @param context The application [Context].
 * @param frameworkSQLiteOpenHelperFactory See [FrameworkSQLiteOpenHelperFactory].
 * @author Niall Scott
 */
internal class DatabaseOpener @Inject constructor(
    private val context: Context,
    private val frameworkSQLiteOpenHelperFactory: FrameworkSQLiteOpenHelperFactory
) {

    /**
     * Create an instance of the [SupportSQLiteOpenHelper], which allows a database to be opened.
     *
     * @param databaseFile A [File] object representing the disk location of the database.
     * @return A new instance of [SupportSQLiteOpenHelper] which can be used to open the database.
     * @throws IOException When the database file could not be accessed.
     */
    @Throws(IOException::class)
    fun createOpenHelper(databaseFile: File): SupportSQLiteOpenHelper {
        val version = readVersion(databaseFile)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(databaseFile.absolutePath)
            .callback(object : SupportSQLiteOpenHelper.Callback(version.coerceAtLeast(1)) {
                override fun onCreate(db: SupportSQLiteDatabase) { }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int) { }
            })
            .build()

        return frameworkSQLiteOpenHelperFactory.create(configuration)
    }

    /**
     * (Copied from DBUtil.kt in Room)
     *
     * Reads the user version number out of the database header from the given file.
     *
     * @param databaseFile The database file.
     * @return The database version
     * @throws IOException If something goes wrong reading the file, such as bad database header or
     * missing permissions.
     * @see [User Version Number](https://www.sqlite.org/fileformat.html.user_version_number).
     */
    @Throws(IOException::class)
    private fun readVersion(databaseFile: File): Int {
        return databaseFile.inputStream().channel.use { input ->
            val buffer = ByteBuffer.allocate(4)
            input.tryLock(60, 4, true)
            input.position(60)
            val read = input.read(buffer)

            if (read != 4) {
                throw IOException("Bad database header, unable to read 4 bytes at offset 60")
            }

            buffer.rewind()

            buffer.int // ByteBuffer is big-endian by default
        }
    }
}