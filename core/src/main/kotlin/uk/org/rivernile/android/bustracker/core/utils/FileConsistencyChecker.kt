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

package uk.org.rivernile.android.bustracker.core.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.source
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForIoDispatcher
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * This class is used to check the consistency of [File]s.
 *
 * @author Niall Scott
 */
class FileConsistencyChecker @Inject internal constructor(
    @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Calculate the hash of a given file and compare it with the expected hash.
     *
     * @param file The [File] to calculate a hash for.
     * @param expectedHash The hash to compare the calculated hash with.
     * @return `true` if the hashes match, otherwise `false`.
     * @throws IOException When there was an issue reading the file.
     */
    @Throws(IOException::class)
    suspend fun checkFileMatchesHash(file: File, expectedHash: String): Boolean {
        val fileHash = calculateFileConsistencyHash(file)

        return fileHash == expectedHash
    }

    /**
     * Create a checksum for a [File]. This could be used to check that a file is of correct
     * consistency.
     *
     * @param file The file to run the MD5 checksum against.
     * @return The MD5 checksum string.
     * @throws IOException When there was an issue readin the file.
     */
    @Throws(IOException::class)
    private suspend fun calculateFileConsistencyHash(file: File): String {
        return withContext(ioDispatcher) {
            file.source().buffer().use { source ->
                HashingSink.md5(blackholeSink()).use { sink ->
                    source.readAll(sink)
                    sink.hash.hex()
                }
            }
        }
    }
}