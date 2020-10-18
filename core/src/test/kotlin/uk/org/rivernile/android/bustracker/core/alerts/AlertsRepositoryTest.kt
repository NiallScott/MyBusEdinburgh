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

package uk.org.rivernile.android.bustracker.core.alerts

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
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
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [AlertsRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AlertsRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertManager: AlertManager
    @Mock
    private lateinit var alertsDao: AlertsDao

    private lateinit var repository: AlertsRepository

    @Before
    fun setUp() {
        repository = AlertsRepository(
                alertManager,
                alertsDao)
    }

    @Test
    fun hasArrivalAlertFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        whenever(alertsDao.hasArrivalAlert("123456"))
                .thenReturn(false)

        val observer = repository.hasArrivalAlertFlow("123456").test(this)
        observer.finish()

        observer.assertValues(false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun hasArrivalAlertFlowRespondsToFavouritesChanged() = coroutineRule.runBlockingTest {
        doAnswer {
            val listener = it.getArgument<AlertsDao.OnAlertsChangedListener>(0)
            listener.onAlertsChanged()
            listener.onAlertsChanged()
        }.whenever(alertsDao).addOnAlertsChangedListener(any())
        whenever(alertsDao.hasArrivalAlert("123456"))
                .thenReturn(false, true, false)

        val observer = repository.hasArrivalAlertFlow("123456").test(this)
        observer.finish()

        observer.assertValues(false, true, false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun hasProximityAlertFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        whenever(alertsDao.hasProximityAlert("123456"))
                .thenReturn(false)

        val observer = repository.hasProximityAlertFlow("123456").test(this)
        observer.finish()

        observer.assertValues(false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun hasProximityAlertFlowRespondsToFavouritesChanged() = coroutineRule.runBlockingTest {
        doAnswer {
            val listener = it.getArgument<AlertsDao.OnAlertsChangedListener>(0)
            listener.onAlertsChanged()
            listener.onAlertsChanged()
        }.whenever(alertsDao).addOnAlertsChangedListener(any())
        whenever(alertsDao.hasProximityAlert("123456"))
                .thenReturn(false, true, false)

        val observer = repository.hasProximityAlertFlow("123456").test(this)
        observer.finish()

        observer.assertValues(false, true, false)
        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun removeArrivalAlertCallsAlertManager() = coroutineRule.runBlockingTest {
        repository.removeArrivalAlert("123456")

        verify(alertManager)
                .removeArrivalAlert("123456")
    }

    @Test
    fun removeProximityAlertCallsAlertManager() = coroutineRule.runBlockingTest {
        repository.removeProximityAlert("123456")

        verify(alertManager)
                .removeProximityAlert("123456")
    }
}