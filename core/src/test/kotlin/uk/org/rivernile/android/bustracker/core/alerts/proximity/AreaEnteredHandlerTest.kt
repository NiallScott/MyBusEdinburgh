/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [AreaEnteredHandler].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AreaEnteredHandlerTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertsRepository: AlertsRepository
    @Mock
    private lateinit var geofencingManager: GeofencingManager
    @Mock
    private lateinit var notificationDispatcher: AlertNotificationDispatcher

    private lateinit var handler: AreaEnteredHandler

    @Before
    fun setUp() {
        handler = AreaEnteredHandler(
            alertsRepository,
            geofencingManager,
            notificationDispatcher)
    }

    @Test
    fun handleAreaEnteredRemovesGeofence() = runTest {
        handler.handleAreaEntered(1)

        verify(geofencingManager)
                .removeGeofence(1)
    }

    @Test
    fun handleAreaEnteredRemovesAlertFromDao() = runTest {
        handler.handleAreaEntered(1)

        verify(alertsRepository)
                .removeProximityAlert(1)
    }

    @Test
    fun handleAreaEnteredDoesNotDispatchNotificationWhenAlertDoesNotExist() = runTest {
        handler.handleAreaEntered(1)

        verify(alertsRepository)
                .getProximityAlert(1)
        verify(notificationDispatcher, never())
                .dispatchProximityAlertNotification(any())
    }

    @Test
    fun handleAreaEnteredDispatchesNotificationWhenAlertDoesExist() = runTest {
        val proximityAlert = ProximityAlert(1, 123L, "123456", 250)
        whenever(alertsRepository.getProximityAlert(1))
                .thenReturn(proximityAlert)

        handler.handleAreaEntered(1)

        verify(notificationDispatcher)
                .dispatchProximityAlertNotification(proximityAlert)
    }
}