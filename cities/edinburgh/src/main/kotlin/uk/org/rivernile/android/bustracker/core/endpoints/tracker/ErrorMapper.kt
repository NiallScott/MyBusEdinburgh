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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.edinburghbustrackerapi.FaultCode
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Map errors to their proper [LiveTimesResponse.Error] type.
 *
 * @author Niall Scott
 */
internal class ErrorMapper @Inject constructor() {

    /**
     * Extract an error from the [BusTimes] response. If the error is not available (i.e. success),
     * then `null` is returned.
     *
     * @return The [LiveTimesResponse.Error.ServerError] representing the error, or `null` if there
     * was no error.
     */
    fun extractError(busTimes: BusTimes) = busTimes.faultCode?.let {
        when (FaultCode.convertFromString(it)) {
            FaultCode.INVALID_APP_KEY ->
                LiveTimesResponse.Error.ServerError.Authentication
            FaultCode.INVALID_PARAMETER ->
                LiveTimesResponse.Error.ServerError.Other("INVALID_PARAMETER")
            FaultCode.PROCESSING_ERROR ->
                LiveTimesResponse.Error.ServerError.Other("PROCESSING_ERROR")
            FaultCode.SYSTEM_MAINTENANCE ->
                LiveTimesResponse.Error.ServerError.Maintenance
            FaultCode.SYSTEM_OVERLOADED ->
                LiveTimesResponse.Error.ServerError.SystemOverloaded
            null -> LiveTimesResponse.Error.ServerError.Other("Fault code = $it")
        }
    }

    /**
     * Convert an error HTTP status code in to an error.
     *
     * @param statusCode The returned HTTP status code.
     * @return The [LiveTimesResponse.Error.ServerError] this maps to.
     */
    fun mapHttpStatusCode(statusCode: Int) = when (statusCode) {
        HttpURLConnection.HTTP_UNAUTHORIZED -> LiveTimesResponse.Error.ServerError.Authentication
        HttpURLConnection.HTTP_FORBIDDEN -> LiveTimesResponse.Error.ServerError.Authentication
        else -> LiveTimesResponse.Error.ServerError.Other("Server error: $statusCode")
    }
}