/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import androidx.core.content.ContextCompat
import javax.inject.Inject

/**
 * The Android implementation of [PermissionChecker].
 *
 * @param context The application [Context].
 * @author Niall Scott
 */
internal class AndroidPermissionChecker @Inject constructor(
        private val context: Context) : PermissionChecker {

    override fun checkLocationPermission() =
            hasCoarseLocationPermission() && hasFineLocationPermission()

    /**
     * Has [Manifest.permission.ACCESS_COARSE_LOCATION] been granted?
     *
     * @return `true` if [Manifest.permission.ACCESS_COARSE_LOCATION] has been granted, otherwise
     * `false`.
     */
    private fun hasCoarseLocationPermission() =
            ContextCompat.checkSelfPermission(context, Manifest.permission
                    .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /**
     * Has [Manifest.permission.ACCESS_FINE_LOCATION] been granted?
     *
     * @return `true` if [Manifest.permission.ACCESS_FINE_LOCATION] has been granted, otherwise
     * `false`.
     */
    private fun hasFineLocationPermission() =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
}