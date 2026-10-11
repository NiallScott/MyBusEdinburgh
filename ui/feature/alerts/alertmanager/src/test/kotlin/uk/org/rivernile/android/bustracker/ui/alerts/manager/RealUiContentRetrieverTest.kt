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

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FakeFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * Tests for [RealUiContentRetriever].
 *
 * @author Niall Scott
 */
class RealUiContentRetrieverTest {

    @Test
    fun uiContentFlowEmitsNoAlertsFeatureWhenNoAlertFeaturesEnabled() = runTest {
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { false },
                onHasProximityAlertFeature = { false }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.NoAlertsFeature, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowDoesNotEmitNoAlertsFeatureWhenHasArrivalAlertFeature() = runTest {
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { false }
            ),
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = { flowOf(null) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.EmptyAlerts, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowDoesNotEmitNoAlertsFeatureWhenHasProximityAlertFeature() = runTest {
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { false },
                onHasProximityAlertFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = { flowOf(null) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.EmptyAlerts, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEmptyAlertsWhenAlertsIsNull() = runTest {
        val retriever = createRetriever(
            featureRepository = featureRepositoryWithAllFeaturesEnabled,
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = { flowOf(null) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.EmptyAlerts, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEmptyAlertsWhenAlertsIsEmpty() = runTest {
        val retriever = createRetriever(
            featureRepository = featureRepositoryWithAllFeaturesEnabled,
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = { flowOf(emptyList()) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.EmptyAlerts, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAlertsWithoutStopDetailsWhenStopDetailsIsNull() = runTest {
        val retriever = createRetriever(
            featureRepository = featureRepositoryWithAllFeaturesEnabled,
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = {
                    flowOf(
                        listOf(arrivalAlert, proximityAlert)
                    )
                }
            ),
            servicesRepository = servicesRepositoryWithServices,
            stopDetailsRetriever = FakeUiStopDetailsRetriever(
                onGetUiStopDetailsFlow = {
                    assertEquals(
                        setOf(
                            "123456".toNaptanStopIdentifier(),
                            "987654".toNaptanStopIdentifier()
                        ),
                        it
                    )
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    alerts = persistentListOf(
                        uiArrivalAlert.copy(stopDetails = null),
                        uiProximityAlert.copy(stopDetails = null)
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAlertsWithoutStopDetailsWhenStopDetailsIsEmpty() = runTest {
        val retriever = createRetriever(
            featureRepository = featureRepositoryWithAllFeaturesEnabled,
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = {
                    flowOf(
                        listOf(arrivalAlert, proximityAlert)
                    )
                }
            ),
            servicesRepository = servicesRepositoryWithServices,
            stopDetailsRetriever = FakeUiStopDetailsRetriever(
                onGetUiStopDetailsFlow = {
                    assertEquals(
                        setOf(
                            "123456".toNaptanStopIdentifier(),
                            "987654".toNaptanStopIdentifier()
                        ),
                        it
                    )
                    flowOf(emptyMap())
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    alerts = persistentListOf(
                        uiArrivalAlert.copy(stopDetails = null),
                        uiProximityAlert.copy(stopDetails = null)
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAlertsWithServicesWithoutColoursWhenServicesColoursIsNull() = runTest {
        val retriever = createRetriever(
            featureRepository = featureRepositoryWithAllFeaturesEnabled,
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = {
                    flowOf(
                        listOf(arrivalAlert, proximityAlert)
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            ),
            stopDetailsRetriever = stopDetailsRetrieverWithStopDetails
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    alerts = persistentListOf(
                        uiArrivalAlert.copy(
                            services = persistentListOf(
                                UiServiceName(
                                    serviceName = "1",
                                    colours = null
                                )
                            )
                        ),
                        uiProximityAlert
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAlertsWithServicesWithoutColoursWhenServicesColoursIsEmpty() = runTest {
        val retriever = createRetriever(
            featureRepository = featureRepositoryWithAllFeaturesEnabled,
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = {
                    flowOf(
                        listOf(arrivalAlert, proximityAlert)
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(emptyMap())
                }
            ),
            stopDetailsRetriever = stopDetailsRetrieverWithStopDetails
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    alerts = persistentListOf(
                        uiArrivalAlert.copy(
                            services = persistentListOf(
                                UiServiceName(
                                    serviceName = "1",
                                    colours = null
                                )
                            )
                        ),
                        uiProximityAlert
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAlertsWithNoMapFeatureWhenNoMapFeature() = runTest {
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { false }
            ),
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = {
                    flowOf(
                        listOf(arrivalAlert, proximityAlert)
                    )
                }
            ),
            servicesRepository = servicesRepositoryWithServices,
            stopDetailsRetriever = stopDetailsRetrieverWithStopDetails
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    alerts = persistentListOf(
                        uiArrivalAlert.copy(isMapShown = false),
                        uiProximityAlert.copy(isMapShown = false)
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAlerts() = runTest {
        val retriever = createRetriever(
            featureRepository = featureRepositoryWithAllFeaturesEnabled,
            alertsRepository = FakeAlertsRepository(
                onAllAlertsFlow = {
                    flowOf(
                        listOf(arrivalAlert, proximityAlert)
                    )
                }
            ),
            servicesRepository = servicesRepositoryWithServices,
            stopDetailsRetriever = stopDetailsRetrieverWithStopDetails
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    alerts = persistentListOf(uiArrivalAlert, uiProximityAlert)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRetriever(
        featureRepository: FeatureRepository = FakeFeatureRepository(),
        alertsRepository: AlertsRepository = FakeAlertsRepository(),
        servicesRepository: ServicesRepository = FakeServicesRepository(),
        stopDetailsRetriever: UiStopDetailsRetriever = FakeUiStopDetailsRetriever(),
        serviceNameComparator: Comparator<String> = naturalOrder()
    ): RealUiContentRetriever {
        return RealUiContentRetriever(
            featureRepository = featureRepository,
            alertsRepository = alertsRepository,
            servicesRepository = servicesRepository,
            stopDetailsRetriever = stopDetailsRetriever,
            serviceNameComparator = serviceNameComparator
        )
    }

    private val featureRepositoryWithAllFeaturesEnabled get() = FakeFeatureRepository(
        onHasArrivalAlertFeature = { true },
        onHasProximityAlertFeature = { true },
        onHasStopMapUiFeature = { true }
    )

    private val servicesRepositoryWithServices get() = FakeServicesRepository(
        onGetColoursForServicesFlow = {
            assertNull(it)
            flowOf(
                mapOf(
                    ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ) to ServiceColours(
                        colourPrimary = 1,
                        colourOnPrimary = 2
                    )
                )
            )
        }
    )

    private val stopDetailsRetrieverWithStopDetails get() = FakeUiStopDetailsRetriever(
        onGetUiStopDetailsFlow = {
            assertEquals(
                setOf(
                    "123456".toNaptanStopIdentifier(),
                    "987654".toNaptanStopIdentifier()
                ),
                it
            )
            flowOf(
                mapOf(
                    "123456".toNaptanStopIdentifier() to uiStopDetails1,
                    "987654".toNaptanStopIdentifier() to uiStopDetails2
                )
            )
        }
    )

    private val arrivalAlert get() = ArrivalAlert(
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

    private val proximityAlert get() = ProximityAlert(
        id = 2,
        timeAdded = Instant.fromEpochMilliseconds(123L),
        stopIdentifier = "987654".toNaptanStopIdentifier(),
        distanceFromMeters = 250
    )

    private val uiArrivalAlert get() = UiAlert.ArrivalAlert(
        id = 1,
        stopIdentifier = "123456".toNaptanStopIdentifier(),
        stopDetails = uiStopDetails1,
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

    private val uiProximityAlert get() = UiAlert.ProximityAlert(
        id = 2,
        stopIdentifier = "987654".toNaptanStopIdentifier(),
        stopDetails = uiStopDetails2,
        distanceFromMeters = 250,
        isMapShown = true
    )

    private val uiStopDetails1 get() = UiStopDetails(
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

    private val uiStopDetails2 get() = UiStopDetails(
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
}
