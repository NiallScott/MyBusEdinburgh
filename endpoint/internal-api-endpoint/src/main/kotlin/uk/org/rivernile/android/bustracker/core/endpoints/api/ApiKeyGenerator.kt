/*
 * Copyright (C) 2019 - 2026 Niall 'Rivernile' Scott
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

import okio.ByteString.Companion.encodeUtf8
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

/**
 * This class is used to generate API keys for the internal API.
 *
 * @author Niall Scott
 */
internal interface ApiKeyGenerator {

    /**
     * Generated a hashed version of the key, using the current time.
     *
     * @return A hashed version of the key, or empty [String] if there was an issue.
     */
    fun generateHashedApiKey(): String
}

internal class RealApiKeyGenerator @Inject constructor(
    @param:ForInternalApiKey private val unhashedKey: String
) : ApiKeyGenerator {

    @Suppress("SimpleDateFormat")
    private val dateFormatter = SimpleDateFormat("yyyyMMddHH").also {
        it.timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun generateHashedApiKey() = generateHashedApiKey(Date())

    private fun generateHashedApiKey(time: Date): String {
        return (unhashedKey + dateFormatter.format(time))
            .encodeUtf8()
            .sha256()
            .hex()
            .lowercase()
    }
}
