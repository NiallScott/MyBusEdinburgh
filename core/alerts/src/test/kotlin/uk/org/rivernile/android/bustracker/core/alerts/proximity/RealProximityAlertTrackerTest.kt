/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeBusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.time.FakeTimeUtils
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Instant

/**
 * Tests for [ProximityAlertTracker].
 *
 * @author Niall Scott
 */
class RealProximityAlertTrackerTest {

    companion object {

        private const val MAX_DURATION_MILLIS = 3600000L
    }

    @Test
    fun trackProximityAlertWithNoLocationInBusStopDaoDoesNotAddGeofence() = runTest {
        val alert = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(101L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 50
        )
        val tracker = createProximityAlertTracker(
            busStopsRepository = FakeBusStopsRepository(
                onGetStopLocation = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    null
                }
            ),
            geofencingManager = FakeGeofencingManager(
                onAddGeofence = { _, _, _, _, _ -> fail("Not expecting to add any geofences.") }
            )
        )

        tracker.trackProximityAlert(alert)
    }

    @Test
    fun trackProximityAlertOutwithTimeRangeDoesNotAddGeofence() = runTest {
        val alert = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(100L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 50
        )
        val location = FakeStopLocation(1.0, 2.0)
        val tracker = createProximityAlertTracker(
            busStopsRepository = FakeBusStopsRepository(
                onGetStopLocation = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    location
                }
            ),
            geofencingManager = FakeGeofencingManager(
                onAddGeofence = { _, _, _, _, _ -> fail("Not expecting to add any geofences.") }
            ),
            timeUtils = FakeTimeUtils(
                onGetCurrentTimeMillis = { MAX_DURATION_MILLIS + 101 }
            )
        )

        tracker.trackProximityAlert(alert)
    }

    @Test
    fun trackProximityAlertWithinTimeRangeAddsGeofence() = runTest {
        val alert = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(100L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 50
        )
        val location = FakeStopLocation(1.0, 2.0)
        var addGeofenceInvocationCount = 0
        val tracker = createProximityAlertTracker(
            busStopsRepository = FakeBusStopsRepository(
                onGetStopLocation = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    location
                }
            ),
            geofencingManager = FakeGeofencingManager(
                onAddGeofence = { id, latitude, longitude, radius, duration ->
                    assertEquals(1, id)
                    assertEquals(1.0, latitude)
                    assertEquals(2.0, longitude)
                    assertEquals(50f, radius)
                    assertEquals(100L, duration)
                    addGeofenceInvocationCount++
                }
            ),
            timeUtils = FakeTimeUtils(
                onGetCurrentTimeMillis = { MAX_DURATION_MILLIS }
            )
        )

        tracker.trackProximityAlert(alert)

        assertEquals(1, addGeofenceInvocationCount)
    }

    @Test
    fun removeProximityAlertCallsGeofencingManager() {
        val removedGeofencesTracker = RemovedGeofencesTracker()
        val tracker = createProximityAlertTracker(
            geofencingManager = FakeGeofencingManager(
                onRemoveGeofence = removedGeofencesTracker
            )
        )

        tracker.removeProximityAlert(1)

        assertEquals(listOf(1), removedGeofencesTracker.geofences)
    }

    private fun createProximityAlertTracker(
        busStopsRepository: BusStopsRepository = FakeBusStopsRepository(),
        geofencingManager: GeofencingManager = FakeGeofencingManager(),
        timeUtils: TimeUtils = FakeTimeUtils()
    ): RealProximityAlertTracker {
        return RealProximityAlertTracker(
            busStopsRepository,
            geofencingManager,
            timeUtils
        )
    }

    private class RemovedGeofencesTracker : (Int) -> Unit {

        val geofences get() = _geofences.toList()
        private val _geofences = mutableListOf<Int>()

        override fun invoke(p1: Int) {
            _geofences += p1
        }
    }
}
