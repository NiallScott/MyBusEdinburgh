/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealUnableToRunAlertsHandler].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RealUnableToRunAlertsHandlerTest {

    @Test
    fun handleUnableToRunArrivalAlertsDoesNothingWhenNoAlerts() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val removeAllAlertsCounter = InvocationCounter()
        val dispatchNotificationCounter = InvocationCounter()
        val handler = createHandler(
            alertsRepository = FakeAlertsRepository(
                onGetArrivalAlertCount = { 0 },
                onRemoveAllArrivalAlerts = removeAllAlertsCounter
            ),
            errorNotificationDispatcher = FakeErrorNotificationDispatcher(
                onDispatchUnableToRunArrivalAlertsNotification = dispatchNotificationCounter
            )
        )

        handler.handleUnableToRunArrivalAlerts()

        assertEquals(0, removeAllAlertsCounter.invocationCount)
        assertEquals(0, dispatchNotificationCounter.invocationCount)
    }

    @Test
    fun handleUnableToRunArrivalAlertsRemovesAlertAndNotifiesWhenCountIsGreaterThan0() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val removeAllAlertsCounter = InvocationCounter()
        val dispatchNotificationCounter = InvocationCounter()
        val handler = createHandler(
            alertsRepository = FakeAlertsRepository(
                onGetArrivalAlertCount = { 1 },
                onRemoveAllArrivalAlerts = removeAllAlertsCounter
            ),
            errorNotificationDispatcher = FakeErrorNotificationDispatcher(
                onDispatchUnableToRunArrivalAlertsNotification = dispatchNotificationCounter
            )
        )

        handler.handleUnableToRunArrivalAlerts()

        assertEquals(1, removeAllAlertsCounter.invocationCount)
        assertEquals(1, dispatchNotificationCounter.invocationCount)
    }

    @Test
    fun handleUnableToRunProximityAlertsDoesNothingWhenNoAlerts() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val removeAllAlertsCounter = InvocationCounter()
        val dispatchNotificationCounter = InvocationCounter()
        val handler = createHandler(
            alertsRepository = FakeAlertsRepository(
                onGetProximityAlertCount = { 0 },
                onRemoveAllProximityAlerts = removeAllAlertsCounter
            ),
            errorNotificationDispatcher = FakeErrorNotificationDispatcher(
                onDispatchUnableToRunProximityAlertsNotification = dispatchNotificationCounter
            )
        )

        handler.handleUnableToRunProximityAlerts()

        assertEquals(0, removeAllAlertsCounter.invocationCount)
        assertEquals(0, dispatchNotificationCounter.invocationCount)
    }

    @Test
    fun handleUnableToRunProximityAlertsRemovesAlertAndNotifiesWhenCountIsGreaterThan0() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val removeAllAlertsCounter = InvocationCounter()
        val dispatchNotificationCounter = InvocationCounter()
        val handler = createHandler(
            alertsRepository = FakeAlertsRepository(
                onGetProximityAlertCount = { 1 },
                onRemoveAllProximityAlerts = removeAllAlertsCounter
            ),
            errorNotificationDispatcher = FakeErrorNotificationDispatcher(
                onDispatchUnableToRunProximityAlertsNotification = dispatchNotificationCounter
            )
        )

        handler.handleUnableToRunProximityAlerts()

        assertEquals(1, removeAllAlertsCounter.invocationCount)
        assertEquals(1, dispatchNotificationCounter.invocationCount)
    }

    private fun TestScope.createHandler(
        alertsRepository: AlertsRepository = FakeAlertsRepository(),
        errorNotificationDispatcher: ErrorNotificationDispatcher = FakeErrorNotificationDispatcher()
    ): RealUnableToRunAlertsHandler {
        return RealUnableToRunAlertsHandler(
            alertsRepository = alertsRepository,
            errorNotificationDispatcher = errorNotificationDispatcher,
            applicationCoroutineScope = this
        )
    }

    private class InvocationCounter : () -> Unit {

        var invocationCount = 0
            private set

        override fun invoke() {
            invocationCount++
        }
    }
}
