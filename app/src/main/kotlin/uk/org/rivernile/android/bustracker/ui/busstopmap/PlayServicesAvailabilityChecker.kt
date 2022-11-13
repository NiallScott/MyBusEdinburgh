/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

/**
 * This class provides a [kotlinx.coroutines.flow.Flow] which emits the availability of Google Play
 * Services on the device.
 *
 * @param context The application [Context].
 * @param googleApiAvailability Used to access Google Play Services availability.
 * @author Niall Scott
 */
class PlayServicesAvailabilityChecker @Inject constructor(
        private val context: Context,
        private val googleApiAvailability: GoogleApiAvailability) {

    companion object {

        private const val CHECK_POLL_PERIOD = 5000L
    }

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits the availability of Google Play Services.
     *
     * When Google Play Services is available, this will be emitted and then this flow will then
     * terminate. Otherwise, the availability is checked by polling every few seconds.
     */
    val apiAvailabilityFlow get() = flow {
        emit(PlayServicesAvailabilityResult.InProgress)

        while (coroutineContext.isActive) {
            val apiResult = googleApiAvailability.isGooglePlayServicesAvailable(context)

            if (apiResult == ConnectionResult.SUCCESS) {
                emit(PlayServicesAvailabilityResult.Available)
                return@flow
            } else {
                if (googleApiAvailability.isUserResolvableError(apiResult)) {
                    emit(PlayServicesAvailabilityResult.Unavailable.Resolvable(apiResult))
                } else {
                    emit(PlayServicesAvailabilityResult.Unavailable.Unresolvable(apiResult))
                }
            }

            delay(CHECK_POLL_PERIOD)
        }
    }.distinctUntilChanged()
}