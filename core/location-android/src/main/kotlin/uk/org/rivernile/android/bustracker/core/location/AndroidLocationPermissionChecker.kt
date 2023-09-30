/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject

/**
 * A convenience class to check location permissions.
 *
 * The methods in here are named in the convention of `check*Permission`. This means that Lint picks
 * our convenience methods up as checking a permission and does not add a warning at the call site
 * claiming the permission has not been checked.
 *
 * @param context The application [Context].
 * @author Niall Scott
 */
internal class AndroidLocationPermissionChecker @Inject constructor(
    private val context: Context) {

    /**
     * Has either [Manifest.permission.ACCESS_FINE_LOCATION] or
     * [Manifest.permission.ACCESS_COARSE_LOCATION] been granted to us?
     *
     * @return `true` if [Manifest.permission.ACCESS_FINE_LOCATION] or
     * [Manifest.permission.ACCESS_COARSE_LOCATION] has been granted to us, otherwise `false`.
     */
    fun checkHasEitherFineOrCoarseLocationPermission() =
        checkFineLocationPermission() || checkCoarseLocationPermission()

    /**
     * Has [Manifest.permission.ACCESS_FINE_LOCATION] been granted to us?
     *
     * @return `true` if [Manifest.permission.ACCESS_FINE_LOCATION] has been granted to us,
     * otherwise `false`.
     */
    private fun checkFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Has [Manifest.permission.ACCESS_COARSE_LOCATION] been granted?
     *
     * @return `true` if [Manifest.permission.ACCESS_COARSE_LOCATION] has been granted, otherwise
     * `false`.
     */
    private fun checkCoarseLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}