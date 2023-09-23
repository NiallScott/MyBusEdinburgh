/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.app

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.packagemanager.getPackageInfoCompat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is an Android-specific implementation of [AppRepository].
 *
 * @param context The application [Context].
 * @param packageManager The Android [PackageManager].
 * @param exceptionLogger Used to log exceptions.
 * @author Niall Scott
 */
@Singleton
internal class AndroidAppRepository @Inject constructor(
    private val context: Context,
    private val packageManager: PackageManager,
    private val exceptionLogger: ExceptionLogger) : AppRepository {

    override val appVersion get() = try {
        packageManager.getPackageInfoCompat(context.packageName, 0).let {
            AppVersion(
                    it.versionName,
                    PackageInfoCompat.getLongVersionCode(it))
        }
    } catch (e: PackageManager.NameNotFoundException) {
        exceptionLogger.log(e)
        // This should never happen as we should always be able to look up our own package name. If
        // this happens, there is something terribly wrong in the system.
        throw UnsupportedOperationException()
    }
}