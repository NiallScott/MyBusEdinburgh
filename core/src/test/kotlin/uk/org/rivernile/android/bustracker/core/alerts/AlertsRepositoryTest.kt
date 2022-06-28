/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.Alert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [AlertsRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AlertsRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertManager: AlertManager
    @Mock
    private lateinit var alertsDao: AlertsDao
    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var repository: AlertsRepository

    @Before
    fun setUp() {
        repository = AlertsRepository(
                alertManager,
                alertsDao,
                timeUtils)
    }

    @Test
    fun hasArrivalAlertFlowGetsInitialValue() = runTest {
        whenever(alertsDao.hasArrivalAlert("123456"))
                .thenReturn(false)

        val observer = repository.hasArrivalAlertFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun hasArrivalAlertFlowRespondsToFavouritesChanged() = runTest {
        doAnswer {
            val listener = it.getArgument<AlertsDao.OnAlertsChangedListener>(0)
            listener.onAlertsChanged()
            listener.onAlertsChanged()
        }.whenever(alertsDao).addOnAlertsChangedListener(any())
        whenever(alertsDao.hasArrivalAlert("123456"))
                .thenReturn(false, true, false)

        val observer = repository.hasArrivalAlertFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun hasProximityAlertFlowGetsInitialValue() = runTest {
        whenever(alertsDao.hasProximityAlert("123456"))
                .thenReturn(false)

        val observer = repository.hasProximityAlertFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun hasProximityAlertFlowRespondsToFavouritesChanged() = runTest {
        doAnswer {
            val listener = it.getArgument<AlertsDao.OnAlertsChangedListener>(0)
            listener.onAlertsChanged()
            listener.onAlertsChanged()
        }.whenever(alertsDao).addOnAlertsChangedListener(any())
        whenever(alertsDao.hasProximityAlert("123456"))
                .thenReturn(false, true, false)

        val observer = repository.hasProximityAlertFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun addArrivalAlertAddsArrivalAlertToAlertManager() = runTest {
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(123L)
        val expected = ArrivalAlert(0, 123L, "123456", listOf("1", "2", "3"), 5)

        repository.addArrivalAlert(ArrivalAlertRequest(
                "123456",
                listOf("1", "2", "3"),
                5))

        verify(alertManager)
                .addArrivalAlert(expected)
    }

    @Test
    fun addProximityAlertAddsProximityAlertToAlertManager() = runTest {
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(123L)
        val expected = ProximityAlert(0, 123L, "123456", 250)

        repository.addProximityAlert(ProximityAlertRequest("123456", 250))

        verify(alertManager)
                .addProximityAlert(expected)
    }

    @Test
    fun removeArrivalAlertCallsAlertManager() = runTest {
        repository.removeArrivalAlert("123456")

        verify(alertManager)
                .removeArrivalAlert("123456")
    }

    @Test
    fun removeProximityAlertCallsAlertManager() = runTest {
        repository.removeProximityAlert("123456")

        verify(alertManager)
                .removeProximityAlert("123456")
    }

    @Test
    fun getAllAlertsFlowGetsInitialValue() = runTest {
        val alerts = listOf(ArrivalAlert(1, 123L, "123456", listOf("1"), 10))
        whenever(alertsDao.getAllAlerts())
                .thenReturn(alerts)

        val observer = repository.getAllAlertsFlow().test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(alerts)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun getAllAlertsFlowRespondsToAlertChanges() = runTest {
        val alerts1 = listOf(ArrivalAlert(1, 123L, "123456", listOf("1"), 10))
        val alerts2 = emptyList<Alert>()
        val alerts3 = listOf(ProximityAlert(2, 123L, "123457", 250))
        doAnswer {
            val listener = it.getArgument<AlertsDao.OnAlertsChangedListener>(0)
            listener.onAlertsChanged()
            listener.onAlertsChanged()
        }.whenever(alertsDao).addOnAlertsChangedListener(any())
        whenever(alertsDao.getAllAlerts())
                .thenReturn(alerts1, alerts2, alerts3)

        val observer = repository.getAllAlertsFlow().test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(alerts1, alerts2, alerts3)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }
}