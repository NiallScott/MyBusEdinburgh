/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.arrivals

import android.app.ServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject

/**
 * This is the pre-Android S specific implementation of [ArrivalAlertTaskLauncher].
 *
 * @param context The application [Context].
 * @author Niall Scott
 */
internal class LegacyAndroidArrivalAlertTaskLauncher @Inject constructor(
    private val context: Context
) : ArrivalAlertTaskLauncher {

    override fun launchArrivalAlertTask() {
        ContextCompat.startForegroundService(
            context,
            context.arrivalAlertServiceStartIntent
        )
    }
}

/**
 * This is the Android S+ specific implementation of [ArrivalAlertTaskLauncher].
 *
 * @param context The application [Context].
 * @param exceptionLogger Used to log handled exceptions.
 * @author Niall Scott
 */
@RequiresApi(Build.VERSION_CODES.S)
internal class V31AndroidArrivalAlertTaskLauncher @Inject constructor(
    private val context: Context,
    private val exceptionLogger: ExceptionLogger
) : ArrivalAlertTaskLauncher {

    override fun launchArrivalAlertTask() {
        try {
            ContextCompat.startForegroundService(
                context,
                context.arrivalAlertServiceStartIntent
            )
        } catch (e: ServiceStartNotAllowedException) {
            exceptionLogger.log(e)
        }
    }
}

/**
 * The [Intent] used to start the [ArrivalAlertRunnerService].
 */
private val Context.arrivalAlertServiceStartIntent get() =
    Intent(this, ArrivalAlertRunnerService::class.java)