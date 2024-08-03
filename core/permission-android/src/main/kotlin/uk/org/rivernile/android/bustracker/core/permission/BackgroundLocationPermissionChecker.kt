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

package uk.org.rivernile.android.bustracker.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import javax.inject.Inject

/**
 * This is used to check whether we have permission to access the device location while in the
 * background.
 *
 * @author Niall Scott
 */
internal interface BackgroundLocationPermissionChecker {

    /**
     * Do we have permission to obtain the device location in the background?
     *
     * @return `true` if we have permission to obtain the device location in the background,
     * otherwise `false`.
     */
    fun checkBackgroundLocationPermission(): Boolean
}

/**
 * A backwards compatible implementation of [BackgroundLocationPermissionChecker] which always
 * returns `true` as this is always available prior to API level 29.
 */
internal class LegacyBackgroundLocationPermissionChecker @Inject constructor()
    : BackgroundLocationPermissionChecker {

    override fun checkBackgroundLocationPermission() = true
}

/**
 * An implementation of [BackgroundLocationPermissionChecker] for API level 29+.
 *
 * @param context The [android.app.Application] [Context].
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class V29BackgroundLocationPermissionChecker @Inject constructor(
    private val context: Context
) : BackgroundLocationPermissionChecker {

    override fun checkBackgroundLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}