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

package uk.org.rivernile.android.bustracker.core.location

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import javax.inject.Inject

/**
 * This is a legacy implementation of [IsLocationEnabledFetcher].
 *
 * Prior to Android [android.os.Build.VERSION_CODES.P], the system location enabled status had to be
 * retrieved from [Settings.Secure]. [android.os.Build.VERSION_CODES.P] introduced
 * [android.location.LocationManager.isLocationEnabled] which should be used over the
 * [Settings.Secure] method, which is now deprecated.
 *
 * @param context The application [Context].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
internal class LegacyIsLocationEnabledFetcher @Inject constructor(
        private val context: Context,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : IsLocationEnabledFetcher {

    // Settings.Secure.LOCATION_MODE is deprecated in newer Android versions, hence this
    // implementation.
    @Suppress("DEPRECATION")
    override suspend fun isLocationEnabled() = withContext(defaultDispatcher) {
        val mode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF)

        mode != Settings.Secure.LOCATION_MODE_OFF
    }
}