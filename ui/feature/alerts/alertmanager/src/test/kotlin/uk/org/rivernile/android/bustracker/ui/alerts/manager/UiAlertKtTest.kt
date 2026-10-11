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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.alerts.Alert
import uk.org.rivernile.android.bustracker.core.alerts.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Tests for `UiAlert.kt`.
 *
 * @author Niall Scott
 */
class UiAlertKtTest {

    @Test
    fun toUiAlertsWithEmptyCollectionReturnsEmptyList() {
        val result = emptyList<Alert>().toUiAlerts(
            stopDetails = null,
            serviceColours = null,
            hasMapFeature = false,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun toAlertsWithArrivalAlertWithNullStopDetailsResultsInNullStopDetails() {
        val result = listOf<Alert>(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 5
            )
        ).toUiAlerts(
            stopDetails = null,
            serviceColours = serviceColours,
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = null,
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1",
                            colours = UiServiceColours(
                                backgroundColour = 1,
                                textColour = 2
                            )
                        )
                    ),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithArrivalAlertWithEmptyStopDetailsResultsInNullStopDetails() {
        val result = listOf<Alert>(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 5
            )
        ).toUiAlerts(
            stopDetails = emptyMap(),
            serviceColours = serviceColours,
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = null,
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1",
                            colours = UiServiceColours(
                                backgroundColour = 1,
                                textColour = 2
                            )
                        )
                    ),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithArrivalAlertWithEmptyServicesMapsCorrectly() {
        val result = listOf<Alert>(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = emptySet(),
                timeTriggerMinutes = 5
            )
        ).toUiAlerts(
            stopDetails = mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
            serviceColours = null,
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = stopDetails1,
                    services = persistentListOf(),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithArrivalAlertWithPopulatedServicesWithNullServiceColoursMapsCorrectly() {
        val result = listOf<Alert>(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 5
            )
        ).toUiAlerts(
            stopDetails = mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
            serviceColours = null,
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = stopDetails1,
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1",
                            colours = null
                        )
                    ),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithArrivalAlertWithPopulatedServicesWithEmptyServiceColoursMapsCorrectly() {
        val result = listOf<Alert>(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 5
            )
        ).toUiAlerts(
            stopDetails = mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
            serviceColours = emptyMap(),
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = stopDetails1,
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1",
                            colours = null
                        )
                    ),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithArrivalAlertWithPopulatedServicesWithPopulatedServiceColoursMapsCorrectly() {
        val result = listOf<Alert>(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    ServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    )
                ),
                timeTriggerMinutes = 5
            )
        ).toUiAlerts(
            stopDetails = mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
            serviceColours = mapOf(
                ServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ) to ServiceColours(
                    colourPrimary = 1,
                    colourOnPrimary = 2
                )
            ),
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = stopDetails1,
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1",
                            colours = UiServiceColours(
                                backgroundColour = 1,
                                textColour = 2
                            )
                        ),
                        UiServiceName(
                            serviceName = "2",
                            colours = null
                        )
                    ),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithArrivalAlertWithPopulatedServicesSortsServicesCorrectly() {
        val result = listOf<Alert>(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    ServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST3"
                    ),
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    ServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    )
                ),
                timeTriggerMinutes = 5
            )
        ).toUiAlerts(
            stopDetails = mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
            serviceColours = null,
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = stopDetails1,
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1",
                            colours = null
                        ),
                        UiServiceName(
                            serviceName = "2",
                            colours = null
                        ),
                        UiServiceName(
                            serviceName = "3",
                            colours = null
                        )
                    ),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithProximityAlertWithNullStopDetailsResultsInNullStopDetails() {
        val result = listOf<Alert>(
            ProximityAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                distanceFromMeters = 250
            )
        ).toUiAlerts(
            stopDetails = null,
            serviceColours = serviceColours,
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ProximityAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = null,
                    distanceFromMeters = 250,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsWithProximityAlertWithEmptyStopDetailsResultsInNullStopDetails() {
        val result = listOf<Alert>(
            ProximityAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                distanceFromMeters = 250
            )
        ).toUiAlerts(
            stopDetails = emptyMap(),
            serviceColours = serviceColours,
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf<UiAlert>(
                UiAlert.ProximityAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = null,
                    distanceFromMeters = 250,
                    isMapShown = true
                )
            ),
            result
        )
    }

    @Test
    fun toAlertsMapsAlertsCorrectly() {
        val result = listOf(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    ServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST3"
                    ),
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    ServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST2"
                    )
                ),
                timeTriggerMinutes = 5
            ),
            ProximityAlert(
                id = 2,
                timeAdded = Instant.fromEpochMilliseconds(456L),
                stopIdentifier = "987654".toNaptanStopIdentifier(),
                distanceFromMeters = 250
            )
        ).toUiAlerts(
            stopDetails = mapOf(
                "123456".toNaptanStopIdentifier() to stopDetails1,
                "987654".toNaptanStopIdentifier() to stopDetails2
            ),
            serviceColours = mapOf(
                ServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ) to ServiceColours(
                    colourPrimary = 1,
                    colourOnPrimary = 2
                ),
                ServiceDescriptor(
                    serviceName = "3",
                    operatorCode = "TEST3"
                ) to ServiceColours(
                    colourPrimary = 3,
                    colourOnPrimary = 4
                )
            ),
            hasMapFeature = true,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(
            listOf(
                UiAlert.ArrivalAlert(
                    id = 1,
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopDetails = stopDetails1,
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1",
                            colours = UiServiceColours(
                                backgroundColour = 1,
                                textColour = 2
                            )
                        ),
                        UiServiceName(
                            serviceName = "2",
                            colours = null
                        ),
                        UiServiceName(
                            serviceName = "3",
                            colours = UiServiceColours(
                                backgroundColour = 3,
                                textColour = 4
                            )
                        )
                    ),
                    timeTriggerMinutes = 5,
                    isMapShown = true
                ),
                UiAlert.ProximityAlert(
                    id = 2,
                    stopIdentifier = "987654".toNaptanStopIdentifier(),
                    stopDetails = stopDetails2,
                    distanceFromMeters = 250,
                    isMapShown = true
                )
            ),
            result
        )
    }

    private val stopDetails1 get() = UiStopDetails(
        stopName = UiStopName(
            name = "Stop Name 1",
            locality = "Locality 1"
        ),
        latLon = UiLatLon(
            latitude = 1.1,
            longitude = 2.2
        ),
        orientation = StopOrientation.NORTH_EAST
    )

    private val stopDetails2 get() = UiStopDetails(
        stopName = UiStopName(
            name = "Stop Name 2",
            locality = "Locality 2"
        ),
        latLon = UiLatLon(
            latitude = 3.3,
            longitude = 4.5
        ),
        orientation = StopOrientation.WEST
    )

    private val serviceColours get() = mapOf(
        ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        ) to ServiceColours(
            colourPrimary = 1,
            colourOnPrimary = 2
        )
    )
}
