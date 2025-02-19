/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [StopDatabaseUpdateWorker].
 *
 * @author Niall Scott
 */
class StopDatabaseUpdateWorkerTest {

    @Test
    fun returnsRetryWhenUpdateWasNotSuccessful() = runTest {
        val workerFactory = createWorkerFactory(
            updateChecker = FakeDatabaseUpdateChecker(
                onCheckForDatabaseUpdates = {
                    assertNull(it)
                    false
                }
            )
        )
        val worker = TestListenableWorkerBuilder<StopDatabaseUpdateWorker>(
            ApplicationProvider.getApplicationContext())
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun returnsSuccessWhenUpdateWasSuccessful() = runTest {
        val workerFactory = createWorkerFactory(
            updateChecker = FakeDatabaseUpdateChecker(
                onCheckForDatabaseUpdates = {
                    assertNull(it)
                    true
                }
            )
        )
        val worker = TestListenableWorkerBuilder<StopDatabaseUpdateWorker>(
            ApplicationProvider.getApplicationContext())
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    private fun createWorkerFactory(
        updateChecker: DatabaseUpdateChecker = FakeDatabaseUpdateChecker()
    ): WorkerFactory {
        return object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ) = StopDatabaseUpdateWorker(
                appContext,
                workerParameters,
                updateChecker
            )
        }
    }
}