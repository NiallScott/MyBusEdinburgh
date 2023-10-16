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

package uk.org.rivernile.android.bustracker.core.packagemanager

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build

/**
 * Android Tiramisu deprecates the [PackageManager.getPackageInfo] method, replaced by a method of
 * the same name with a different signature, which encapsulates the supplied flags in an object.
 *
 * This method provides compatibility so that the new method is used on API level 33 and above, and
 * uses the old method prior to this.
 *
 * @param packageName See [PackageManager.getPackageInfo].
 * @param flags See [PackageManager.getPackageInfo].
 * @return See [PackageManager.getPackageInfo].
 * @throws NameNotFoundException See [PackageManager.getPackageInfo].
 * @see PackageManager.getPackageInfo
 */
@Throws(NameNotFoundException::class)
fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int): PackageInfo = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    else -> @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
}