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

package uk.org.rivernile.android.bustracker.core.alerts

import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.FakeArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.FakeProximityAlert
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.ArrivalAlert
    as DatabaseArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.ProximityAlert
    as DatabaseProximityAlert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Tests for `Alert.kt`.
 *
 * @author Niall Scott
 */
class AlertKtTest {

    @Test
    fun toArrivalAlertListReturnsEmptyListWhenInputIsEmpty() {
        val result = emptyList<DatabaseArrivalAlert>().toArrivalAlertList()

        assertEquals(
            emptyList(),
            result
        )
    }

    @Test
    fun toArrivalAlertListReturnsMappedItemsWhenInputIsPopulated() {
        val result = listOf(
            FakeArrivalAlert(
                id = 0,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                timeTriggerMinutes = 5,
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                )
            ),
            FakeArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(456L),
                stopIdentifier = "987654".toNaptanStopIdentifier(),
                timeTriggerMinutes = 10,
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    )
                )
            )
        ).toArrivalAlertList()

        assertEquals(
            listOf(
                ArrivalAlert(
                    id = 0,
                    timeAdded = Instant.fromEpochMilliseconds(123L),
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    timeTriggerMinutes = 5,
                    services = setOf(
                        FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        )
                    )
                ),
                ArrivalAlert(
                    id = 1,
                    timeAdded = Instant.fromEpochMilliseconds(456L),
                    stopIdentifier = "987654".toNaptanStopIdentifier(),
                    timeTriggerMinutes = 10,
                    services = setOf(
                        FakeServiceDescriptor(
                            serviceName = "2",
                            operatorCode = "TEST2"
                        )
                    )
                )
            ),
            result
        )
    }

    @Test
    fun toArrivalAlertMapsToArrivalAlert() {
        val result = FakeArrivalAlert(
            id = 0,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            timeTriggerMinutes = 5,
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                )
            )
        ).toArrivalAlert()

        assertEquals(
            ArrivalAlert(
                id = 0,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                timeTriggerMinutes = 5,
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                )
            ),
            result
        )
    }

    @Test
    fun toProximityAlertListReturnsEmptyListWhenInputIsEmpty() {
        val result = emptyList<DatabaseProximityAlert>()

        assertEquals(
            emptyList(),
            result
        )
    }

    @Test
    fun toProximityAlertListReturnsMappedItemsWhenInputIsPopulated() {
        val result = listOf(
            FakeProximityAlert(
                id = 0,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                radiusTriggerMeters = 250
            ),
            FakeProximityAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(456L),
                stopIdentifier = "987654".toNaptanStopIdentifier(),
                radiusTriggerMeters = 500
            )
        ).toProximityAlertList()

        assertEquals(
            listOf(
                ProximityAlert(
                    id = 0,
                    timeAdded = Instant.fromEpochMilliseconds(123L),
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    distanceFromMeters = 250
                ),
                ProximityAlert(
                    id = 1,
                    timeAdded = Instant.fromEpochMilliseconds(456L),
                    stopIdentifier = "987654".toNaptanStopIdentifier(),
                    distanceFromMeters = 500
                )
            ),
            result
        )
    }

    @Test
    fun toProximityAlertMapsToArrivalAlert() {
        val result = FakeProximityAlert(
            id = 0,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            radiusTriggerMeters = 250
        ).toProximityAlert()

        assertEquals(
            ProximityAlert(
                id = 0,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                distanceFromMeters = 250
            ),
            result
        )
    }
}
