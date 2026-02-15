/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AlertsStateRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AlertsStateRetrieverTest {

    @Mock
    private lateinit var featureRepository: FeatureRepository
    @Mock
    private lateinit var alertsRepository: AlertsRepository

    private lateinit var retriever: AlertsStateRetriever

    @BeforeTest
    fun setUp() {
        retriever = AlertsStateRetriever(
            featureRepository,
            alertsRepository
        )
    }

    @Test
    fun isArrivalAlertVisibleFlowEmitsFalseWhenDoesNotHaveArrivalAlertFeature() = runTest {
        whenever(featureRepository.hasArrivalAlertFeature)
            .thenReturn(false)

        retriever.isArrivalAlertVisibleFlow.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isArrivalAlertVisibleFlowEmitsTrueWhenDoesHaveArrivalAlertFeature() = runTest {
        whenever(featureRepository.hasArrivalAlertFeature)
            .thenReturn(true)

        retriever.isArrivalAlertVisibleFlow.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isProximityAlertVisibleFlowEmitsFalseWhenDoesNotHaveProximityAlertFeature() = runTest {
        whenever(featureRepository.hasProximityAlertFeature)
            .thenReturn(false)

        retriever.isProximityAlertVisibleFlow.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isProximityAlertVisibleFlowEmitsTrueWhenDoesHaveProximityAlertFeature() = runTest {
        whenever(featureRepository.hasProximityAlertFeature)
            .thenReturn(true)

        retriever.isProximityAlertVisibleFlow.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getHasArrivalAlertFlowEmitsNullWhenStopIdentifierIsNull() = runTest {
        val stopIdentifierFlow = flowOf(null)

        retriever.getHasArrivalAlertFlow(stopIdentifierFlow).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getHasArrivalAlertFlowEmitsValuesFromAlertsRepository() = runTest {
        val stopIdentifierFlow = flowOf("123456".toNaptanStopIdentifier())
        whenever(alertsRepository.hasArrivalAlertFlow("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(false, true, false))

        retriever.getHasArrivalAlertFlow(stopIdentifierFlow).test {
            assertNull(awaitItem())
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getHasProximityAlertFlowEmitsNullWhenStopIdentifierIsNull() = runTest {
        val stopIdentifierFlow = flowOf(null)

        retriever.getHasProximityAlertFlow(stopIdentifierFlow).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getHasProximityAlertFlowEmitsValuesFromAlertsRepository() = runTest {
        val stopIdentifierFlow = flowOf("123456".toNaptanStopIdentifier())
        whenever(alertsRepository.hasProximityAlertFlow("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(false, true, false))

        retriever.getHasProximityAlertFlow(stopIdentifierFlow).test {
            assertNull(awaitItem())
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }
}
