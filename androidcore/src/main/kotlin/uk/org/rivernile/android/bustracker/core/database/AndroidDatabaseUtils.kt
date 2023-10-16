/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.IOException
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import java.io.File
import javax.inject.Inject

/**
 * This class is an Android specific implementation of [DatabaseUtils].
 *
 * @param context The application [Context].
 * @param exceptionLogger The exception logger.
 * @param ioDispatcher The IO [CoroutineDispatcher].
 * @author Niall Scott
 */
internal class AndroidDatabaseUtils @Inject constructor(
    private val context: Context,
    private val exceptionLogger: ExceptionLogger,
    @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher): DatabaseUtils {

    @Throws(IOException::class)
    override suspend fun createTemporaryFile(prefix: String): File {
        return withContext(ioDispatcher) {
            @Suppress("BlockingMethodInNonBlockingContext")
            File.createTempFile(prefix, ".tmp", context.cacheDir).also {
                it.deleteOnExit()
            }
        }
    }

    override fun getDatabasePath(dbFileName: String): File = context.getDatabasePath(dbFileName)
}