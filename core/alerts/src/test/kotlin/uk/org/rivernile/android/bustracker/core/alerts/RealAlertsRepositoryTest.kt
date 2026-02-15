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

package uk.org.rivernile.android.bustracker.core.alerts

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.FakeArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.FakeProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.ArrivalAlert
    as DatabaseArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.ArrivalAlertDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.FakeArrivalAlertDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.InsertableArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.FakeProximityAlertDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.InsertableProximityAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.ProximityAlert
    as DatabaseProximityAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.ProximityAlertDao
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.time.FakeTimeUtils
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Instant

/**
 * Tests for [AlertsRepository].
 *
 * @author Niall Scott
 */
class RealAlertsRepositoryTest {

    @Test
    fun addArrivalAlertAddsAlertToDaoAndLaunchesArrivalAlertTask() = runTest {
        val launchArrivalAlertTaskInvocationTracker = InvocationTracker()
        val addArrivalAlertTracker = AddArrivalAlertTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = launchArrivalAlertTaskInvocationTracker
            ),
            arrivalAlertDao = FakeArrivalAlertDao(
                onAddArrivalAlert = addArrivalAlertTracker
            )
        )
        val request = ArrivalAlertRequest(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ),
                FakeServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST2"
                ),
                FakeServiceDescriptor(
                    serviceName = "3",
                    operatorCode = "TEST3"
                )
            ),
            timeTrigger = 5
        )
        val expected = InsertableArrivalAlert(
            id = 0,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            timeTriggerMinutes = 5,
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ),
                FakeServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST2"
                ),
                FakeServiceDescriptor(
                    serviceName = "3",
                    operatorCode = "TEST3"
                )
            )
        )

        repository.addArrivalAlert(request)

        assertEquals(listOf(expected), addArrivalAlertTracker.addedArrivalAlerts)
        assertEquals(1, launchArrivalAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun addProximityAlertAddsAlertToDaoAndLaunchesArrivalAlertTask() = runTest {
        val launchProximityAlertTaskInvocationTracker = InvocationTracker()
        val addedProximityAlertTracker = AddProximityAlertTracker()
        val repository = createAlertsRepository(
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = launchProximityAlertTaskInvocationTracker
            ),
            proximityAlertDao = FakeProximityAlertDao(
                onAddProximityAlert = addedProximityAlertTracker
            )
        )
        val request = ProximityAlertRequest("123456".toNaptanStopIdentifier(), 50)
        val expected = InsertableProximityAlert(
            id = 0,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            radiusTriggerMeters = 50
        )

        repository.addProximityAlert(request)

        assertEquals(listOf(expected), addedProximityAlertTracker.addedProximityAlerts)
        assertEquals(1, launchProximityAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun removeArrivalAlertWithStopIdentifierRemovesArrivalAlert() = runTest {
        val removeArrivalAlertTracker = RemoveAlertByStopIdentifierTracker()
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onRemovalArrivalAlertByStopIdentifier = removeArrivalAlertTracker
            )
        )

        repository.removeArrivalAlert("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            removeArrivalAlertTracker.stopIdentifiers
        )
    }

    @Test
    fun removeArrivalAlertWithIdRemovesArrivalAlert() = runTest {
        val removeArrivalAlertTracker = RemoveAlertByIdTracker()
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onRemoveArrivalAlertById = removeArrivalAlertTracker
            )
        )

        repository.removeArrivalAlert(42)

        assertEquals(listOf(42), removeArrivalAlertTracker.ids)
    }

    @Test
    fun removeAllArrivalAlertsRemovesAllArrivalAlertsInDao() = runTest {
        val removeAllArrivalAlertsTracker = InvocationTracker()
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onRemoveAllArrivalAlerts = removeAllArrivalAlertsTracker
            )
        )

        repository.removeAllArrivalAlerts()

        assertEquals(1, removeAllArrivalAlertsTracker.invocationCount)
    }

    @Test
    fun removeProximityAlertWithStopIdentifierRemovesProximityAlert() = runTest {
        val removeProximityAlertTracker = RemoveAlertByStopIdentifierTracker()
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onRemoveProximityAlertByStopIdentifier = removeProximityAlertTracker
            )
        )

        repository.removeProximityAlert("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            removeProximityAlertTracker.stopIdentifiers
        )
    }

    @Test
    fun removeProximityAlertWithIdRemovesProximityAlert() = runTest {
        val removeProximityAlertTracker = RemoveAlertByIdTracker()
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onRemoveProximityAlertById = removeProximityAlertTracker
            )
        )

        repository.removeProximityAlert(42)

        assertEquals(listOf(42), removeProximityAlertTracker.ids)
    }

    @Test
    fun removeAllProximityAlertsRemovesAllProximityAlertsInDao() = runTest {
        val removeAllProximityAlertsTracker = InvocationTracker()
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onRemoveAllProximityAlerts = removeAllProximityAlertsTracker
            )
        )

        repository.removeAllProximityAlerts()

        assertEquals(1, removeAllProximityAlertsTracker.invocationCount)
    }

    @Test
    fun getAllArrivalAlertsMapsNullResult() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetAllArrivalAlerts = { null }
            )
        )

        val result = repository.getAllArrivalAlerts()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertsMapsNonNullResult() = runTest {
        val items = listOf(
            InsertableArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(10L),
                stopIdentifier = "1".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 1
            ),
            InsertableArrivalAlert(
                id = 2,
                timeAdded = Instant.fromEpochMilliseconds(20L),
                stopIdentifier = "2".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    )
                ),
                timeTriggerMinutes = 2
            ),
            InsertableArrivalAlert(
                id = 3,
                timeAdded = Instant.fromEpochMilliseconds(30L),
                stopIdentifier = "3".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST3"
                    )
                ),
                timeTriggerMinutes = 3
            )
        )
        val expected = listOf(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(10L),
                stopIdentifier = "1".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 1
            ),
            ArrivalAlert(
                id = 2,
                timeAdded = Instant.fromEpochMilliseconds(20L),
                stopIdentifier = "2".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    )
                ),
                timeTriggerMinutes = 2
            ),
            ArrivalAlert(
                id = 3,
                timeAdded = Instant.fromEpochMilliseconds(30L),
                stopIdentifier = "3".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST3"
                    )
                ),
                timeTriggerMinutes = 3
            )
        )
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetAllArrivalAlerts = { items }
            )
        )

        val result = repository.getAllArrivalAlerts()

        assertEquals(expected, result)
    }

    @Test
    fun getAllArrivalAlertStopsMapsNullResult() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetAllArrivalAlertStops = { null }
            )
        )

        val result = repository.getAllArrivalAlertStops()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertStopsMapsNonNullResult() = runTest {
        val items = setOf(
            "1".toNaptanStopIdentifier(),
            "2".toNaptanStopIdentifier(),
            "3".toNaptanStopIdentifier()
        )
        val expected = setOf(
            "1".toNaptanStopIdentifier(),
            "2".toNaptanStopIdentifier(),
            "3".toNaptanStopIdentifier()
        )
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetAllArrivalAlertStops = { items }
            )
        )

        val result = repository.getAllArrivalAlertStops()

        assertEquals(expected, result)
    }

    @Test
    fun getProximityAlertMapsNullResult() = runTest {
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onGetProximityAlert = { null }
            )
        )

        val result = repository.getProximityAlert(42)

        assertNull(result)
    }

    @Test
    fun hasArrivalAlertFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetHasArrivalAlertFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    intervalFlowOf(0L, 10L, false, false, true, true, false)
                }
            )
        )

        repository.hasArrivalAlertFlow("123456".toNaptanStopIdentifier()).test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun hasProximityAlertFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onGetHasProximityAlertFlow = {
                    intervalFlowOf(0L, 10L, false, false, true, true, false)
                }
            )
        )

        repository.hasProximityAlertFlow("123456".toNaptanStopIdentifier()).test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getArrivalAlertCountReturnsValueFromDao() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetArrivalAlertCount = { 123 }
            )
        )

        val result = repository.getArrivalAlertCount()

        assertEquals(123, result)
    }

    @Test
    fun getProximityAlertCountReturnsValueFromDao() = runTest {
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onGetProximityAlertCount = { 123 }
            )
        )

        val result = repository.getProximityAlertCount()

        assertEquals(123, result)
    }

    @Test
    fun arrivalAlertCountFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onArrivalAlertCountFlow = { intervalFlowOf(0L, 10L, 0, 0, 1, 1, 2, 2, 3) }
            )
        )

        repository.arrivalAlertCountFlow.test {
            assertEquals(0, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
        }
    }

    @Test
    fun arrivalAlertStopIdentifiersFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetAllArrivalAlertStopsFlow = {
                    intervalFlowOf(
                        initialDelay = 0L,
                        interval = 10L,
                        listOf("123".toNaptanStopIdentifier(), "456".toNaptanStopIdentifier()),
                        listOf("123".toNaptanStopIdentifier(), "456".toNaptanStopIdentifier()),
                        listOf("123".toNaptanStopIdentifier(), "789".toNaptanStopIdentifier())
                    )
                }
            )
        )

        repository.arrivalAlertStopIdentifiersFlow.test {
            assertEquals(
                setOf(
                    "123".toNaptanStopIdentifier(),
                    "456".toNaptanStopIdentifier()
                ),
                awaitItem())
            assertEquals(
                setOf(
                    "123".toNaptanStopIdentifier(),
                    "789".toNaptanStopIdentifier()
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun allProximityAlertsFlowEmitsDistinctValues() = runTest {
        val alert1 = InsertableProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(1L),
            stopIdentifier = "1".toNaptanStopIdentifier(),
            radiusTriggerMeters = 1
        )
        val alert2 = InsertableProximityAlert(
            id = 2,
            timeAdded = Instant.fromEpochMilliseconds(2L),
            stopIdentifier = "2".toNaptanStopIdentifier(),
            radiusTriggerMeters = 2
        )
        val alert3 = InsertableProximityAlert(
            id = 3,
            timeAdded = Instant.fromEpochMilliseconds(3L),
            stopIdentifier = "3".toNaptanStopIdentifier(),
            radiusTriggerMeters = 3
        )
        val expected1 = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(1L),
            stopIdentifier = "1".toNaptanStopIdentifier(),
            distanceFromMeters = 1
        )
        val expected2 = ProximityAlert(
            id = 2,
            timeAdded = Instant.fromEpochMilliseconds(2L),
            stopIdentifier = "2".toNaptanStopIdentifier(),
            distanceFromMeters = 2
        )
        val expected3 = ProximityAlert(
            id = 3,
            timeAdded = Instant.fromEpochMilliseconds(3L),
            stopIdentifier = "3".toNaptanStopIdentifier(),
            distanceFromMeters = 3
        )
        val flow = intervalFlowOf(
            0L,
            10L,
            null,
            null,
            listOf(alert1),
            listOf(alert1),
            listOf(alert1, alert2, alert3),
            listOf(alert2)
        )
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onAllProximityAlertsFlow = { flow }
            )
        )

        repository.allProximityAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(listOf(expected1), awaitItem())
            assertEquals(listOf(expected1, expected2, expected3), awaitItem())
            assertEquals(listOf(expected2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun proximityAlertStopCodesFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            proximityAlertDao = FakeProximityAlertDao(
                onAllProximityAlertStopsFlow = {
                    intervalFlowOf(
                        initialDelay = 0L,
                        interval = 10L,
                        listOf("123".toNaptanStopIdentifier(), "456".toNaptanStopIdentifier()),
                        listOf("123".toNaptanStopIdentifier(), "456".toNaptanStopIdentifier()),
                        listOf("123".toNaptanStopIdentifier(), "789".toNaptanStopIdentifier())
                    )
                }
            )
        )

        repository.proximityAlertStopIdentifiersFlow.test {
            assertEquals(
                setOf(
                    "123".toNaptanStopIdentifier(),
                    "456".toNaptanStopIdentifier()
                ),
                awaitItem()
            )
            assertEquals(
                setOf(
                    "123".toNaptanStopIdentifier(),
                    "789".toNaptanStopIdentifier()
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun allAlertsFlowEmitsDistinctValues() = runTest {
        val arrivalAlert1 = InsertableArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(1L),
            stopIdentifier = "1".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ),
                FakeServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST2"
                ),
                FakeServiceDescriptor(
                    serviceName = "3",
                    operatorCode = "TEST3"
                )
            ),
            timeTriggerMinutes = 1
        )
        val arrivalAlert2 = InsertableArrivalAlert(
            id = 2,
            timeAdded = Instant.fromEpochMilliseconds(2L),
            stopIdentifier = "2".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "4",
                    operatorCode = "TEST4"
                ),
                FakeServiceDescriptor(
                    serviceName = "5",
                    operatorCode = "TEST5"
                ),
                FakeServiceDescriptor(
                    serviceName = "6",
                    operatorCode = "TEST6"
                )
            ),
            timeTriggerMinutes = 6
        )
        val proximityAlert1 = InsertableProximityAlert(
            id = 3,
            timeAdded = Instant.fromEpochMilliseconds(3L),
            stopIdentifier = "3".toNaptanStopIdentifier(),
            radiusTriggerMeters = 3
        )
        val proximityAlert2 = InsertableProximityAlert(
            id = 4,
            timeAdded = Instant.fromEpochMilliseconds(4L),
            stopIdentifier = "4".toNaptanStopIdentifier(),
            radiusTriggerMeters = 4
        )
        val expectedArrivalAlert1 = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(1L),
            stopIdentifier = "1".toNaptanStopIdentifier(),
            setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ),
                FakeServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST2"
                ),
                FakeServiceDescriptor(
                    serviceName = "3",
                    operatorCode = "TEST3"
                )
            ),
            1
        )
        val expectedArrivalAlert2 = ArrivalAlert(
            id = 2,
            timeAdded = Instant.fromEpochMilliseconds(2L),
            stopIdentifier = "2".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "4",
                    operatorCode = "TEST4"
                ),
                FakeServiceDescriptor(
                    serviceName = "5",
                    operatorCode = "TEST5"
                ),
                FakeServiceDescriptor(
                    serviceName = "6",
                    operatorCode = "TEST6"
                )
            ),
            timeTriggerMinutes = 6
        )
        val expectedProximityAlert1 = ProximityAlert(
            id = 3,
            timeAdded = Instant.fromEpochMilliseconds(3L),
            stopIdentifier = "3".toNaptanStopIdentifier(),
            distanceFromMeters = 3
        )
        val expectedProximityAlert2 = ProximityAlert(
            id = 4,
            timeAdded = Instant.fromEpochMilliseconds(4L),
            stopIdentifier = "4".toNaptanStopIdentifier(),
            distanceFromMeters = 4
        )
        val arrivalAlertsFlow = intervalFlowOf(
            0L,
            10L,
            null,
            null,
            listOf(arrivalAlert1),
            listOf(arrivalAlert1, arrivalAlert2),
            listOf(arrivalAlert2)
        )
        val proximityAlertsFlow = intervalFlowOf(
            5L,
            10L,
            null,
            null,
            listOf(proximityAlert1),
            listOf(proximityAlert1, proximityAlert2),
            listOf(proximityAlert2)
        )
        val repository = createAlertsRepository(
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetAllArrivalAlertsFlow = { arrivalAlertsFlow }
            ),
            proximityAlertDao = FakeProximityAlertDao(
                onAllProximityAlertsFlow = { proximityAlertsFlow }
            )
        )

        repository.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(
                listOf(expectedArrivalAlert1),
                awaitItem()
            )
            assertEquals(
                listOf(
                    expectedArrivalAlert1,
                    expectedProximityAlert1
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    expectedArrivalAlert1,
                    expectedArrivalAlert2,
                    expectedProximityAlert1
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    expectedArrivalAlert1,
                    expectedArrivalAlert2,
                    expectedProximityAlert1,
                    expectedProximityAlert2
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    expectedArrivalAlert2,
                    expectedProximityAlert1,
                    expectedProximityAlert2
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    expectedArrivalAlert2,
                    expectedProximityAlert2
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun ensureTasksRunningIfAlertsExistDoesNotStartArrivalTaskWhenCountIsZero() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = { fail("Should not launch the arrival alert task.") }
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = { fail("Should not launch the proximity alert task") }
            ),
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetArrivalAlertCount = { 0 }
            ),
            proximityAlertDao = FakeProximityAlertDao(
                onGetProximityAlertCount = { 0 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()
    }

    @Test
    fun ensureTasksRunningLaunchesTasks() {
        val launchArrivalAlertTaskInvocationTracker = InvocationTracker()
        val launchProximityAlertTaskInvocationTracker = InvocationTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = launchArrivalAlertTaskInvocationTracker
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = launchProximityAlertTaskInvocationTracker
            )
        )

        repository.ensureTasksRunning()

        assertEquals(1, launchArrivalAlertTaskInvocationTracker.invocationCount)
        assertEquals(1, launchProximityAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsArrivalTaskWhenCountIsGreaterThanZero() = runTest {
        val launchArrivalAlertTaskInvocationTracker = InvocationTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = launchArrivalAlertTaskInvocationTracker
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = { fail("Should not launch the proximity alert task") }
            ),
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetArrivalAlertCount = { 1 }
            ),
            proximityAlertDao = FakeProximityAlertDao(
                onGetProximityAlertCount = { 0 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()

        assertEquals(1, launchArrivalAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun ensureTasksRunningIfAlertsExistDoesNotStartProximityTaskWhenCountIsZero() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = { fail("Should not launch the arrival alert task.") }
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = { fail("Should not launch the proximity alert task") }
            ),
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetArrivalAlertCount = { 0 }
            ),
            proximityAlertDao = FakeProximityAlertDao(
                onGetProximityAlertCount = { 0 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsProximityTaskWhenCountIsGreaterThanZero() = runTest {
        val launchProximityAlertTaskInvocationTracker = InvocationTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = { fail("Should not launch the arrival alert task.") }
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = launchProximityAlertTaskInvocationTracker
            ),
            arrivalAlertDao = FakeArrivalAlertDao(
                onGetArrivalAlertCount = { 0 }
            ),
            proximityAlertDao = FakeProximityAlertDao(
                onGetProximityAlertCount = { 1 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()

        assertEquals(1, launchProximityAlertTaskInvocationTracker.invocationCount)
    }

    private fun createAlertsRepository(
        arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(),
        proximityAlertTaskLauncher: ProximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(),
        arrivalAlertDao: ArrivalAlertDao = FakeArrivalAlertDao(),
        proximityAlertDao: ProximityAlertDao = FakeProximityAlertDao()
    ): RealAlertsRepository {
        return RealAlertsRepository(
            arrivalAlertTaskLauncher = arrivalAlertTaskLauncher,
            proximityAlertTaskLauncher = proximityAlertTaskLauncher,
            arrivalAlertDao = arrivalAlertDao,
            proximityAlertDao = proximityAlertDao,
            timeUtils = FakeTimeUtils(
                onGetCurrentTimeMillis = { 123L },
                onNow = { Instant.fromEpochMilliseconds(123L) }
            )
        )
    }

    private class InvocationTracker : () -> Unit {

        var invocationCount = 0
            private set

        override fun invoke() {
            invocationCount++
        }
    }

    private class AddArrivalAlertTracker : (DatabaseArrivalAlert) -> Unit {

        val addedArrivalAlerts get() = _addedArrivalAlerts.toList()
        private val _addedArrivalAlerts = mutableListOf<DatabaseArrivalAlert>()

        override fun invoke(p1: DatabaseArrivalAlert) {
            _addedArrivalAlerts += p1
        }
    }

    private class AddProximityAlertTracker : (DatabaseProximityAlert) -> Unit {

        val addedProximityAlerts get() = _addedProximityAlerts.toList()
        private val _addedProximityAlerts = mutableListOf<DatabaseProximityAlert>()

        override fun invoke(p1: DatabaseProximityAlert) {
            _addedProximityAlerts += p1
        }
    }

    private class RemoveAlertByStopIdentifierTracker : (StopIdentifier) -> Unit {

        val stopIdentifiers get() = _stopIdentifiers.toList()
        private val _stopIdentifiers = mutableListOf<StopIdentifier>()

        override fun invoke(p1: StopIdentifier) {
            _stopIdentifiers += p1
        }
    }

    private class RemoveAlertByIdTracker : (Int) -> Unit {

        val ids get() = _ids.toList()
        private val _ids = mutableListOf<Int>()

        override fun invoke(p1: Int) {
            _ids += p1
        }
    }
}
