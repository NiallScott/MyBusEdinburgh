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

package uk.org.rivernile.android.bustracker.core.alerts

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Tests for [DeviceBootBroadcastReceiver].
 *
 * @author Niall Scott
 */
@Ignore("Until I figure out how to do BroadcastReceiver testing.")
class DeviceBootBroadcastReceiverTest {

    @Test
    fun invokingBroadcastReceiverWithNoActionDoesNotEnsureAlertTasksRunning() {
        val receiver = DeviceBootBroadcastReceiver().apply {
            alertsRepository = FakeAlertsRepository(
                onEnsureTasksRunning = { fail("Not expecting to ensure the tasks are running.") }
            )
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, DeviceBootBroadcastReceiver::class.java)

        receiver.onReceive(context, intent)
    }

    @Test
    fun invokingBroadcastReceiverWithWrongActionDoesNotEnsureAlertTasksRunning() {
        val receiver = DeviceBootBroadcastReceiver().apply {
            alertsRepository = FakeAlertsRepository(
                onEnsureTasksRunning = { fail("Not expecting to ensure the tasks are running.") }
            )
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent("wrong.action")

        receiver.onReceive(context, intent)
    }

    @Test
    fun invokingBroadcastReceiverWithBootCompletedActionEnsuresAlertTasksRunning() {
        var invocationCounter = 0
        val receiver = DeviceBootBroadcastReceiver().apply {
            alertsRepository = FakeAlertsRepository(
                onEnsureTasksRunning = { invocationCounter++ }
            )
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        assertEquals(1, invocationCounter)
    }
}