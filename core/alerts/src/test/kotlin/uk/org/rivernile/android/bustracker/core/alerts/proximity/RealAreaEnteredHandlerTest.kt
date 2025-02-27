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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [RealAreaEnteredHandler].
 *
 * @author Niall Scott
 */
class RealAreaEnteredHandlerTest {

    @Test
    fun handleAreaEnteredRemovesGeofence() = runTest {
        val removedGeofencesTracker = IdTracker()
        val handler = createAreaEnteredHandler(
            alertsRepository = FakeAlertsRepository(
                onRemoveProximityAlertWithId = { },
                onGetProximityAlert = {
                    assertEquals(1, it)
                    null
                }
            ),
            geofencingManager = FakeGeofencingManager(
                onRemoveGeofence = removedGeofencesTracker
            )
        )

        handler.handleAreaEntered(1)

        assertEquals(listOf(1), removedGeofencesTracker.ids)
    }

    @Test
    fun handleAreaEnteredRemovesAlertFromDao() = runTest {
        val removedProximityAlertTracker = IdTracker()
        val handler = createAreaEnteredHandler(
            alertsRepository = FakeAlertsRepository(
                onRemoveProximityAlertWithId = removedProximityAlertTracker,
                onGetProximityAlert = {
                    assertEquals(1, it)
                    null
                }
            ),
            geofencingManager = FakeGeofencingManager(
                onRemoveGeofence = { }
            )
        )

        handler.handleAreaEntered(1)

        assertEquals(listOf(1), removedProximityAlertTracker.ids)
    }

    @Test
    fun handleAreaEnteredDoesNotDispatchNotificationWhenAlertDoesNotExist() = runTest {
        val dispatchedNotifications = ProximityAlertTracker()
        val handler = createAreaEnteredHandler(
            alertsRepository = FakeAlertsRepository(
                onRemoveProximityAlertWithId = { },
                onGetProximityAlert = {
                    assertEquals(1, it)
                    null
                }
            ),
            geofencingManager = FakeGeofencingManager(
                onRemoveGeofence = { }
            ),
            notificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchProximityAlertNotification = dispatchedNotifications
            )
        )

        handler.handleAreaEntered(1)

        assertTrue(dispatchedNotifications.proximityAlerts.isEmpty())
    }

    @Test
    fun handleAreaEnteredDispatchesNotificationWhenAlertDoesExist() = runTest {
        val proximityAlert = ProximityAlert(1, 123L, "123456", 250)
        val dispatchedNotifications = ProximityAlertTracker()
        val handler = createAreaEnteredHandler(
            alertsRepository = FakeAlertsRepository(
                onRemoveProximityAlertWithId = { },
                onGetProximityAlert = {
                    assertEquals(1, it)
                    proximityAlert
                }
            ),
            geofencingManager = FakeGeofencingManager(
                onRemoveGeofence = { }
            ),
            notificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchProximityAlertNotification = dispatchedNotifications
            )
        )

        handler.handleAreaEntered(1)

        assertEquals(listOf(proximityAlert), dispatchedNotifications.proximityAlerts)
    }

    private fun createAreaEnteredHandler(
        alertsRepository: AlertsRepository = FakeAlertsRepository(),
        geofencingManager: GeofencingManager = FakeGeofencingManager(),
        notificationDispatcher: AlertNotificationDispatcher = FakeAlertNotificationDispatcher()
    ): RealAreaEnteredHandler {
        return RealAreaEnteredHandler(
            alertsRepository,
            geofencingManager,
            notificationDispatcher
        )
    }

    private class IdTracker : (Int) -> Unit {

        val ids get() = _ids.toList()
        private val _ids = mutableListOf<Int>()

        override fun invoke(p1: Int) {
            _ids += p1
        }
    }

    private class ProximityAlertTracker : (ProximityAlert) -> Unit {

        val proximityAlerts get() = _proximityAlerts
        private val _proximityAlerts = mutableListOf<ProximityAlert>()

        override fun invoke(p1: ProximityAlert) {
            _proximityAlerts += p1
        }
    }
}