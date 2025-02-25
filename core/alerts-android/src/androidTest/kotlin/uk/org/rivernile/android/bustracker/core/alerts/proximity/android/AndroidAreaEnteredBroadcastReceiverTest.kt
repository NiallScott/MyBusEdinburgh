/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.proximity.android

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.AreaEnteredHandler
import uk.org.rivernile.android.bustracker.core.alerts.proximity.FakeAreaEnteredHandler
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Tests for [AndroidAreaEnteredBroadcastReceiver].
 *
 * @author Niall Scott
 */
@Ignore("Until I figure out how to do BroadcastReceiver testing.")
@OptIn(ExperimentalCoroutinesApi::class)
class AndroidAreaEnteredBroadcastReceiverTest {

    @Test
    fun invokingBroadcastReceiverWithoutAlertIdDoesNotCallAreaEntered() = runTest {
        val receiver = createReceiver(
            areaEnteredHandler = FakeAreaEnteredHandler(
                onHandleAreaEntered = { fail("Not expecting to enter the area.") }
            )
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)

        receiver.onReceive(context, intent)
        advanceUntilIdle()
    }

    @Test
    fun invokingBroadcastReceiverWithInvalidAlertIdDoesNotCallAreaEntered() = runTest {
        val receiver = createReceiver(
            areaEnteredHandler = FakeAreaEnteredHandler(
                onHandleAreaEntered = { fail("Not expecting to enter the area.") }
            )
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, -1)
                .putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true)

        receiver.onReceive(context, intent)
        advanceUntilIdle()
    }

    @Test
    fun invokingBroadcastReceiverWithNotEnteringProximityDoesNotCallAreaEntered() = runTest {
        val receiver = createReceiver(
            areaEnteredHandler = FakeAreaEnteredHandler(
                onHandleAreaEntered = { fail("Not expecting to enter the area.") }
            )
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, 1)
                .putExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)

        receiver.onReceive(context, intent)
        advanceUntilIdle()
    }

    @Test
    fun invokingBroadcastReceiverAndCriteriaSatisfiedCallsAreaEntered() = runTest {
        var invocationCount = 0
        val receiver = createReceiver(
            areaEnteredHandler = FakeAreaEnteredHandler(
                onHandleAreaEntered = { invocationCount++ }
            )
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, 1)
                .putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true)

        receiver.onReceive(context, intent)
        advanceUntilIdle()

        assertEquals(1, invocationCount)
    }

    private fun TestScope.createReceiver(
        areaEnteredHandler: AreaEnteredHandler = FakeAreaEnteredHandler()
    ): AndroidAreaEnteredBroadcastReceiver {
        return AndroidAreaEnteredBroadcastReceiver().apply {
            this.areaEnteredHandler = areaEnteredHandler
            applicationCoroutineScope = backgroundScope
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler)
        }
    }
}