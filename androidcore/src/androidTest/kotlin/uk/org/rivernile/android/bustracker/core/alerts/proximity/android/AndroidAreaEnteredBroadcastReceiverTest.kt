/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

import android.content.Intent
import android.location.LocationManager
import com.nhaarman.mockitokotlin2.any
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
import uk.org.rivernile.android.bustracker.CurrentThreadExecutor
import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.GeofencingManager
import uk.org.rivernile.android.bustracker.core.assistInject
import uk.org.rivernile.android.bustracker.core.dagger.FakeAlertsModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeCoreModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeSettingsDatabaseModule
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.core.getApplication
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [AndroidAreaEnteredBroadcastReceiver].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AndroidAreaEnteredBroadcastReceiverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertsDao: AlertsDao
    @Mock
    private lateinit var geofencingManager: GeofencingManager
    @Mock
    private lateinit var notificationDispatcher: AlertNotificationDispatcher
    private val currentThreadExecutor = CurrentThreadExecutor()

    private lateinit var receiver: AndroidAreaEnteredBroadcastReceiver

    @Before
    fun setUp() {
        val alertsModule = FakeAlertsModule(
                geofencingManager = geofencingManager,
                alertNotificationDispatcher = notificationDispatcher)
        val coreModule = FakeCoreModule(
                backgroundExecutor = currentThreadExecutor,
                globalCoroutineScope = coroutineRule,
                defaultDispatcher = coroutineRule.testDispatcher)
        val settingsDatabaseModule = FakeSettingsDatabaseModule(alertsDao)

        assistInject(
                getApplication(),
                alertsModule = alertsModule,
                coreModule = coreModule,
                settingsDatabaseModule = settingsDatabaseModule)

        receiver = AndroidAreaEnteredBroadcastReceiver()
    }

    @Test
    fun invokingBroadcastReceiverWithoutAlertIdDoesNotShowNotification() =
            coroutineRule.runBlockingTest {
        val context = getApplication()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)

        receiver.onReceive(context, intent)

        verify(alertsDao, never())
                .removeProximityAlert(any<Int>())
        verify(geofencingManager, never())
                .removeGeofence(any())
        verify(notificationDispatcher, never())
                .dispatchProximityAlertNotification(any())
    }

    @Test
    fun invokingBroadcastReceiverWithInvalidAlertIdDoesNotShowNotification() =
            coroutineRule.runBlockingTest {
        val context = getApplication()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, -1)
                .putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true)

        receiver.onReceive(context, intent)

        verify(alertsDao, never())
                .removeProximityAlert(any<Int>())
        verify(geofencingManager, never())
                .removeGeofence(any())
        verify(notificationDispatcher, never())
                .dispatchProximityAlertNotification(any())
    }

    @Test
    fun invokingBroadcastReceiverWithNotEnteringProximityDoesNotShowNotification() =
            coroutineRule.runBlockingTest {
        val context = getApplication()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, 1)
                .putExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)

        receiver.onReceive(context, intent)

        verify(alertsDao, never())
                .removeProximityAlert(any<Int>())
        verify(geofencingManager, never())
                .removeGeofence(any())
        verify(notificationDispatcher, never())
                .dispatchProximityAlertNotification(any())
    }

    @Test
    fun invokingBroadcastReceiverWithAlertMissingFromDatabaseDoesNotShowNotification() =
            coroutineRule.runBlockingTest {
        val context = getApplication()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, 1)
                .putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true)
        whenever(alertsDao.getProximityAlert(1))
                .thenReturn(null)

        receiver.onReceive(context, intent)

        verify(alertsDao)
                .removeProximityAlert(1)
        verify(geofencingManager)
                .removeGeofence(1)
        verify(notificationDispatcher, never())
                .dispatchProximityAlertNotification(any())
    }

    @Test
    fun invokingBroadcastReceiverAndCriteriaSatisfiedShowsNotification() =
            coroutineRule.runBlockingTest {
        val context = getApplication()
        val intent = Intent(context, AndroidAreaEnteredBroadcastReceiver::class.java)
                .putExtra(AndroidAreaEnteredBroadcastReceiver.EXTRA_ALERT_ID, 1)
                .putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true)
        val proximityAlert = ProximityAlert(1, 123L, "123456", 50)
        whenever(alertsDao.getProximityAlert(1))
                .thenReturn(proximityAlert)

        receiver.onReceive(context, intent)

        verify(alertsDao)
                .removeProximityAlert(1)
        verify(geofencingManager)
                .removeGeofence(1)
        verify(notificationDispatcher)
                .dispatchProximityAlertNotification(proximityAlert)
    }
}