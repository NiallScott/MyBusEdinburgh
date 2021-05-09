/*
 * Copyright (C) 2020 - 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.startup

import android.content.Intent
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.assistInject
import uk.org.rivernile.android.bustracker.core.dagger.FakeAlertsModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeCoreModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeSettingsDatabaseModule
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.getApplication
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [DeviceBootBroadcastReceiver].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DeviceBootBroadcastReceiverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertsDao: AlertsDao
    @Mock
    private lateinit var arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher
    @Mock
    private lateinit var proximityAlertTaskLauncher: ProximityAlertTaskLauncher

    private lateinit var receiver: DeviceBootBroadcastReceiver

    @Before
    fun setUp() {
        val alertsModule = FakeAlertsModule(
                arrivalAlertTaskLauncher = arrivalAlertTaskLauncher,
                proximityAlertTaskLauncher = proximityAlertTaskLauncher)
        val coreModule = FakeCoreModule(
                applicationCoroutineScope = coroutineRule,
                defaultDispatcher = coroutineRule.testDispatcher)
        val settingsDatabaseModule = FakeSettingsDatabaseModule(alertsDao)

        assistInject(
                getApplication(),
                alertsModule = alertsModule,
                coreModule = coreModule,
                settingsDatabaseModule = settingsDatabaseModule)

        receiver = DeviceBootBroadcastReceiver()
    }

    @Test
    fun invokingBroadcastReceiverWithoutActionDoesNotExecute() = coroutineRule.runBlockingTest {
        givenArrivalAlertCount(1)
        givenProximityAlertCount(1)
        val context = getApplication()
        val intent = Intent(context, DeviceBootBroadcastReceiver::class.java)

        receiver.onReceive(context, intent)

        verify(arrivalAlertTaskLauncher, never())
                .launchArrivalAlertTask()
        verify(proximityAlertTaskLauncher, never())
                .launchProximityAlertTask()
    }

    @Test
    fun invokingBroadcastReceiverWithoutBootCompletedActionDoesNotExecute() =
            coroutineRule.runBlockingTest {
        givenArrivalAlertCount(1)
        givenProximityAlertCount(1)
        val context = getApplication()
        val intent = Intent(context, DeviceBootBroadcastReceiver::class.java)
                .setAction("a.b.c")

        receiver.onReceive(context, intent)

        verify(arrivalAlertTaskLauncher, never())
                .launchArrivalAlertTask()
        verify(proximityAlertTaskLauncher, never())
                .launchProximityAlertTask()
    }

    @Test
    fun invokingBroadcastReceiverWithNoArrivalAlertsDoesNotStartArrivalAlertTask() =
            coroutineRule.runBlockingTest {
        givenArrivalAlertCount(0)
        givenProximityAlertCount(1)
        val context = getApplication()
        val intent = Intent(context, DeviceBootBroadcastReceiver::class.java)
                .setAction(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        verify(arrivalAlertTaskLauncher, never())
                .launchArrivalAlertTask()
    }

    @Test
    fun invokingBroadcastReceiverWithNoProximityAlertsDoesNotStartArrivalAlertTask() =
            coroutineRule.runBlockingTest {
        givenArrivalAlertCount(1)
        givenProximityAlertCount(0)
        val context = getApplication()
        val intent = Intent(context, DeviceBootBroadcastReceiver::class.java)
                .setAction(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        verify(proximityAlertTaskLauncher, never())
                .launchProximityAlertTask()
    }

    @Test
    fun invokingBroadcastReceiverWithArrivalAlertDoesStartArrivalAlertTask() =
            coroutineRule.runBlockingTest {
        givenArrivalAlertCount(1)
        givenProximityAlertCount(0)
        val context = getApplication()
        val intent = Intent(context, DeviceBootBroadcastReceiver::class.java)
                .setAction(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        verify(arrivalAlertTaskLauncher)
                .launchArrivalAlertTask()
    }

    @Test
    fun invokingBroadcastReceiverWithProximityAlertDoesStartArrivalAlertTask() =
            coroutineRule.runBlockingTest {
        givenArrivalAlertCount(0)
        givenProximityAlertCount(1)
        val context = getApplication()
        val intent = Intent(context, DeviceBootBroadcastReceiver::class.java)
                .setAction(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        verify(proximityAlertTaskLauncher)
                .launchProximityAlertTask()
    }

    private suspend fun givenArrivalAlertCount(count: Int) {
        whenever(alertsDao.getArrivalAlertCount())
                .thenReturn(count)
    }

    private suspend fun givenProximityAlertCount(count: Int) {
        whenever(alertsDao.getProximityAlertCount())
                .thenReturn(count)
    }
}