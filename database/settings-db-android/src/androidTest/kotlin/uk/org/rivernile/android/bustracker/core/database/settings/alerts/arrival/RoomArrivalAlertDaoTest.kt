/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.settings.RoomSettingsDatabase
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Tests for [RoomArrivalAlertDao].
 *
 * @author Niall Scott
 */
class RoomArrivalAlertDaoTest {

    private lateinit var database: RoomSettingsDatabase

    @BeforeTest
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder<RoomSettingsDatabase>(
                context = ApplicationProvider.getApplicationContext<Context>()
            ).build()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun addArrivalAlertWithoutServicesReturnsExpectedData() = runTest {
        database.arrivalAlertDao.addArrivalAlert(
            FakeArrivalAlert(
                id = 0,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                timeTriggerMinutes = 5,
                services = emptySet()
            )
        )

        val arrivalAlerts = database.arrivalAlertDao.getAllArrivalAlerts()

        assertEquals(
            listOf(
                RoomArrivalAlert(
                    id = 1,
                    timeAdded = Instant.fromEpochMilliseconds(123L),
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    timeTriggerMinutes = 5,
                    services = emptySet()
                )
            ),
            arrivalAlerts
        )
    }

    @Test
    fun addArrivalAlertWithServicesReturnsExpectedData() = runTest {
        database.arrivalAlertDao.addArrivalAlert(
            FakeArrivalAlert(
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
                    )
                )
            )
        )

        val arrivalAlerts = database.arrivalAlertDao.getAllArrivalAlerts()

        assertEquals(
            listOf(
                RoomArrivalAlert(
                    id = 1,
                    timeAdded = Instant.fromEpochMilliseconds(123L),
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    timeTriggerMinutes = 5,
                    services = setOf(
                        RoomArrivalAlertServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        RoomArrivalAlertServiceDescriptor(
                            serviceName = "2",
                            operatorCode = "TEST2"
                        )
                    )
                )
            ),
            arrivalAlerts
        )
    }

    @Test
    fun addArrivalAlertWithIdenticalStopCodeToPreviousUpdatesItem() = runTest {
        database.arrivalAlertDao.addArrivalAlert(
            FakeArrivalAlert(
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
                    )
                )
            )
        )
        database.arrivalAlertDao.addArrivalAlert(
            FakeArrivalAlert(
                id = 0,
                timeAdded = Instant.fromEpochMilliseconds(456L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                timeTriggerMinutes = 10,
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST3"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "4",
                        operatorCode = "TEST4"
                    )
                )
            )
        )
        val arrivalAlerts = database.arrivalAlertDao.getAllArrivalAlerts()

        assertEquals(
            listOf(
                RoomArrivalAlert(
                    id = 2,
                    timeAdded = Instant.fromEpochMilliseconds(456L),
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    timeTriggerMinutes = 10,
                    services = setOf(
                        RoomArrivalAlertServiceDescriptor(
                            serviceName = "3",
                            operatorCode = "TEST3"
                        ),
                        RoomArrivalAlertServiceDescriptor(
                            serviceName = "4",
                            operatorCode = "TEST4"
                        )
                    )
                )
            ),
            arrivalAlerts
        )
        with(database) {
            openHelper
                .readableDatabase
                .query("""
                    SELECT COUNT(*)
                    FROM arrival_alert_service
                """.trimIndent())
                .use {
                    assertTrue(it.moveToNext())

                    assertEquals(2, it.getInt(0))
                }
        }
    }

    @Test
    fun removeArrivalAlertByIdRemovesArrivalAlertAndServices() = runTest {
        with(database.arrivalAlertDao) {
            addArrivalAlert(
                FakeArrivalAlert(
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
                        )
                    )
                )
            )

            removeArrivalAlert(id = 1)
        }

        val arrivalAlerts = database.arrivalAlertDao.getAllArrivalAlerts()

        assertTrue(arrivalAlerts.isEmpty())
        with(database) {
            openHelper
                .readableDatabase
                .query("""
                    SELECT COUNT(*)
                    FROM arrival_alert_service
                """.trimIndent())
                .use {
                    assertTrue(it.moveToNext())

                    assertEquals(0, it.getInt(0))
                }
        }
    }

    @Test
    fun removeArrivalAlertByStopIdentifierRemovesArrivalAlertAndServices() = runTest {
        with(database.arrivalAlertDao) {
            addArrivalAlert(
                FakeArrivalAlert(
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
                        )
                    )
                )
            )

            removeArrivalAlert(stopIdentifier = "123456".toNaptanStopIdentifier())
        }

        val arrivalAlerts = database.arrivalAlertDao.getAllArrivalAlerts()

        assertTrue(arrivalAlerts.isEmpty())
        with(database) {
            openHelper
                .readableDatabase
                .query("""
                    SELECT COUNT(*)
                    FROM arrival_alert_service
                """.trimIndent())
                .use {
                    assertTrue(it.moveToNext())

                    assertEquals(0, it.getInt(0))
                }
        }
    }
}
