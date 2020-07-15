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

package uk.org.rivernile.android.bustracker.core.alerts.arrivals

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.CurrentThreadExecutor
import uk.org.rivernile.android.bustracker.core.assistInject
import uk.org.rivernile.android.bustracker.core.dagger.FakeCoreModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeSettingsDatabaseModule
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.getApplication

/**
 * Tests for [RemoveArrivalAlertBroadcastReceiver].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class RemoveArrivalAlertBroadcastReceiverTest {

    @Mock
    private lateinit var alertsDao: AlertsDao
    private val currentThreadExecutor = CurrentThreadExecutor()

    private lateinit var receiver: RemoveArrivalAlertBroadcastReceiver

    @Before
    fun setUp() {
        val coreModule = FakeCoreModule(currentThreadExecutor)
        val settingsDatabaseModule = FakeSettingsDatabaseModule(alertsDao)

        assistInject(
                getApplication(),
                coreModule = coreModule,
                settingsDatabaseModule = settingsDatabaseModule)

        receiver = RemoveArrivalAlertBroadcastReceiver()
    }

    @Test
    fun invokingBroadcastReceiverRemovesArrivalAlert() {
        receiver.onReceive(ApplicationProvider.getApplicationContext(), createIntent())

        verify(alertsDao)
                .removeAllArrivalAlerts()
    }

    private fun createIntent() =
            Intent(ApplicationProvider.getApplicationContext(),
                    RemoveArrivalAlertBroadcastReceiver::class.java)
}