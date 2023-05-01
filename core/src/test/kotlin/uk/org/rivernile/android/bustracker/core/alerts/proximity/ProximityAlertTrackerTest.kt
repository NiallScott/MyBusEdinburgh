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

import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.BusStopsDao
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [ProximityAlertTracker].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ProximityAlertTrackerTest {

    companion object {

        private const val MAX_DURATION_MILLIS = 3600000L
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var busStopsDao: BusStopsDao
    @Mock
    private lateinit var geofencingManager: GeofencingManager
    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var tracker: ProximityAlertTracker

    @Before
    fun setUp() {
        tracker = ProximityAlertTracker(busStopsDao, geofencingManager, timeUtils)
    }

    @Test
    fun trackProximityAlertWithNoLocationInBusStopDaoDoesNotAddGeofence() = runTest {
        val alert = ProximityAlert(1, 101L, "123456", 50)
        whenever(busStopsDao.getLocationForStop("123456"))
                .thenReturn(null)

        tracker.trackProximityAlert(alert)

        verify(geofencingManager, never())
                .addGeofence(any(), any(), any(), any(), any())
    }

    @Test
    fun trackProximityAlertOutwithTimeRangeDoesNotAddGeofence() = runTest {
        val alert = ProximityAlert(1, 100L, "123456", 50)
        val location = StopLocation("123456", 1.0, 2.0)
        whenever(busStopsDao.getLocationForStop("123456"))
                .thenReturn(location)
        whenever(timeUtils.currentTimeMills)
                .thenReturn(MAX_DURATION_MILLIS + 101L)

        tracker.trackProximityAlert(alert)

        verify(geofencingManager, never())
                .addGeofence(any(), any(), any(), any(), any())
    }

    @Test
    fun trackProximityAlertWithinTimeRangeAddsGeofence() = runTest {
        val alert = ProximityAlert(1, 100L, "123456", 50)
        val location = StopLocation("123456", 1.0, 2.0)
        whenever(busStopsDao.getLocationForStop("123456"))
                .thenReturn(location)
        whenever(timeUtils.currentTimeMills)
                .thenReturn(MAX_DURATION_MILLIS)

        tracker.trackProximityAlert(alert)

        verify(geofencingManager)
                .addGeofence(1, 1.0, 2.0, 50f, 100L)
    }

    @Test
    fun removeProximityAlertCallsGeofencingManager() {
        tracker.removeProximityAlert(1)

        verify(geofencingManager)
                .removeGeofence(1)
    }
}