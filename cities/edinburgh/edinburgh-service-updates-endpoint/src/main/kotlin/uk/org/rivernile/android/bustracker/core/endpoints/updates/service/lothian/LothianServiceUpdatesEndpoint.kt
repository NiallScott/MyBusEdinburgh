/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.updates.service.lothian

import kotlinx.serialization.SerializationException
import okio.IOException
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import javax.inject.Inject

/**
 * This class implements [ServiceUpdatesEndpoint] specifically for the Lothian Buses endpoint.
 *
 * @param lothianServiceUpdatesApi The Retrofit Lothian service updates API.
 * @param connectivityRepository Used to determine the connectivity state.
 * @param exceptionLogger Logs exceptions.
 * @author Niall Scott
 */
internal class LothianServiceUpdatesEndpoint @Inject constructor(
    private val lothianServiceUpdatesApi: LothianServiceUpdatesApi,
    private val connectivityRepository: ConnectivityRepository,
    private val exceptionLogger: ExceptionLogger
) : ServiceUpdatesEndpoint {

    override suspend fun getServiceUpdates(): ServiceUpdatesResponse {
        return if (connectivityRepository.hasInternetConnectivity) {
            try {
                val response = lothianServiceUpdatesApi.getServiceUpdates()

                if (response.isSuccessful) {
                    ServiceUpdatesResponse.Success(
                        response.body()?.toServiceUpdatesOrNull()
                    )
                } else {
                    val error = "Status code = ${response.code()}; " +
                            "Body = ${response.errorBody()?.string()}"

                    exceptionLogger.log(RuntimeException(error))
                    ServiceUpdatesResponse.Error.ServerError(error)
                }
            } catch (e: IOException) {
                exceptionLogger.log(e)
                ServiceUpdatesResponse.Error.Io(e)
            } catch (e: SerializationException) {
                exceptionLogger.log(e)
                ServiceUpdatesResponse.Error.ServerError("Could not parse data.")
            }
        } else {
            ServiceUpdatesResponse.Error.NoConnectivity
        }
    }
}