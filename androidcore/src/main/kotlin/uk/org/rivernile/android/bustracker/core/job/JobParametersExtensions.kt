/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.job

import android.app.job.JobParameters
import android.net.Network
import android.os.Build

/**
 * This extension function on [JobParameters] allows the caller to retrieve the [Network] a job
 * should perform networking over in a backwards compatible way. This API was introduced in
 * [Build.VERSION_CODES.P]. On the supported SDK versions, this function will return the value
 * returned by [JobParameters.getNetwork], otherwise `null` will be returned.
 *
 * @return The [Network] as provided by [JobParameters.getNetwork] if on a suitable platform
 * version, otherwise `null`.
 * @see JobParameters.getNetwork
 * @author Niall Scott
 */
fun JobParameters.getNetworkCompat(): Network? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            network
        } else {
            null
        }