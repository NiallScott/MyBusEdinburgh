/*
 * Copyright (C) 2019 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.api

import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiKey
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

/**
 * This class is used to generate API keys for the API.
 *
 * @param unhashedKey The unhashed version of the key.
 * @param exceptionLogger Used to log exceptions.
 * @author Niall Scott
 */
internal class ApiKeyGenerator @Inject constructor(
    @param:ForInternalApiKey private val unhashedKey: String,
    private val exceptionLogger: ExceptionLogger
) {

    @Suppress("SimpleDateFormat")
    private val dateFormatter = SimpleDateFormat("yyyyMMddHH").also {
        it.timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Generated a hashed version of the key, using the current time.
     *
     * @return A hashed version of the key, or empty [String] if there was an issue.
     */
    fun generateHashedApiKey() = generateHashedApiKey(Date())

    /**
     * Generate a hashed version of the key.
     *
     * @param time A [Date] object representing the time the key should be hashed for.
     * @return A hashed version of the key, or empty [String] if there was an issue.
     */
    private fun generateHashedApiKey(time: Date): String {
        val combinedKey = unhashedKey + dateFormatter.format(time)

        return try {
            val m = MessageDigest.getInstance("MD5")
            m.update(combinedKey.toByteArray(), 0, combinedKey.length)
            var hashedKey = BigInteger(1, m.digest()).toString(16)

            while (hashedKey.length < 32) {
                hashedKey = "0$hashedKey"
            }

            hashedKey
        } catch (e: NoSuchAlgorithmException) {
            exceptionLogger.log(e)
            ""
        }
    }
}