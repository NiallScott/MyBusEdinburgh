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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.app.job.JobInfo
import android.os.Build

/**
 * This extension function on [JobInfo.Builder] allows the caller to set
 * [JobInfo.Builder.setPrefetch] in a backwards compatible way. This means on Android versions prior
 * to [Build.VERSION_CODES.P] calling this method has no effect, but it will at least be safe to
 * call.
 *
 * @param prefetch See [JobInfo.Builder.setPrefetch]
 * @return See [JobInfo.Builder.setPrefetch]
 * @see JobInfo.Builder.setPrefetch
 * @author Niall Scott
 */
fun JobInfo.Builder.setPrefetchCompat(prefetch: Boolean): JobInfo.Builder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setPrefetch(prefetch)
        } else {
            this
        }

/**
 * This extension function on [JobInfo.Builder] allows the caller to set
 * [JobInfo.Builder.setRequiresBatteryNotLow] in a backwards compatible way. This means on Android
 * versions prior to [Build.VERSION_CODES.O] calling this method has no effect, but it will at least
 * be safe to call.
 *
 * @param batteryNotLow See [JobInfo.Builder.setRequiresBatteryNotLow]
 * @return See [JobInfo.Builder.setRequiresBatteryNotLow]
 * @see JobInfo.Builder.setRequiresBatteryNotLow
 * @author Niall Scott
 */
fun JobInfo.Builder.setRequiresBatteryNotLowCompat(batteryNotLow: Boolean): JobInfo.Builder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setRequiresBatteryNotLow(batteryNotLow)
        } else {
            this
        }