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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.net.SocketFactory

/**
 * This [androidx.work.Worker] is the entry point in commencing a check and possible update of the
 * stop database.
 *
 * @param context The application [Context].
 * @param params The worker parameters.
 * @param updateChecker The implementation used to perform the check and update of the stop
 * database.
 * @author Niall Scott
 */
@HiltWorker
internal class StopDatabaseUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val updateChecker: DatabaseUpdateChecker)
    : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return if (updateChecker.checkForDatabaseUpdates(socketFactory)) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    /**
     * Used to get the [SocketFactory] of the work in a compatible way. From
     * [Build.VERSION_CODES.P], this may be non-`null`. Prior to this point, this value will always
     * be `null`.
     */
    private val socketFactory: SocketFactory? get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.network?.socketFactory
        } else {
            null
        }
    }
}