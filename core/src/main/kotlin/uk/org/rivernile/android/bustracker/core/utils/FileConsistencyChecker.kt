/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

/**
 * This class is used to check the consistency of [File]s.
 *
 * @author Niall Scott
 */
class FileConsistencyChecker @Inject constructor() {

    /**
     * Calculate the hash of a given file and compare it with the expected hash.
     *
     * @param file The [File] to calculate a hash for.
     * @param expectedHash The hash to compare the calculated hash with.
     * @return `true` if the hashes match, otherwise `false`.
     * @throws IOException When there was an issue reading the file.
     */
    @Throws(IOException::class)
    fun checkFileMatchesHash(file: File, expectedHash: String): Boolean {
        val fileHash = calculateFileConsistencyHash(file)

        return fileHash == expectedHash
    }

    /**
     * Create a checksum for a [File]. This could be used to check that a file is of correct
     * consistency.
     *
     * See: http://vyshemirsky.blogspot.com/2007/08/computing-md5-digest-checksum-in-java.html
     *
     * This has been slightly modified. And now updated for Kotlin.
     *
     * @param file The file to run the MD5 checksum against.
     * @return The MD5 checksum string.
     * @throws IOException When there was an issue reading the file.
     */
    @Throws(IOException::class)
    private fun calculateFileConsistencyHash(file: File): String {
        val hasher = try {
            MessageDigest.getInstance("MD5")
        } catch (ignored: NoSuchAlgorithmException) {
            return ""
        }

        BufferedInputStream(file.inputStream()).use {
            val buffer = ByteArray(1024)

            do {
                val bytesRead = it.read(buffer)

                if (bytesRead > 0) {
                    hasher.update(buffer, 0, bytesRead)
                }
            } while (bytesRead != -1)
        }

        return hasher.digest()?.let { bytes ->
            val sb = StringBuilder()

            bytes.forEach { byte ->
                val str = Integer.toString((byte.toInt() and 0xFF) + 0x100, 16).substring(1)
                sb.append(str)
            }

            sb.toString()
        } ?: ""
    }
}