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

package uk.org.rivernile.android.bustracker.core.services

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDetails as StoredServiceDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceWithColour as StoredServiceWithColour
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ServicesRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ServicesRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var serviceDao: ServiceDao
    @Mock
    private lateinit var serviceColourOverride: ServiceColourOverride

    @Test
    fun getColoursForServicesFlowWithNullOverrideAndNullServices() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.getColoursForServicesFlow(null))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyMap(),
                    mapOf("1" to 1),
                    mapOf(
                        "1" to 1,
                        "2" to null,
                        "3" to 3
                    )
                )
            )

        val observer = repository.getColoursForServicesFlow(null).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            null,
            mapOf("1" to 1),
            mapOf(
                "1" to 1,
                "3" to 3
            )
        )
    }

    @Test
    fun getColoursForServicesFlowWithNullOverrideAndEmptyServices() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.getColoursForServicesFlow(emptySet()))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyMap(),
                    mapOf("1" to 1),
                    mapOf(
                        "1" to 1,
                        "2" to null,
                        "3" to 3
                    )
                )
            )

        val observer = repository.getColoursForServicesFlow(emptySet()).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            null,
            mapOf("1" to 1),
            mapOf(
                "1" to 1,
                "3" to 3
            )
        )
    }

    @Test
    fun getColoursForServicesFlowWithNullOverrideAndNonEmptyServices() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.getColoursForServicesFlow(setOf("1", "2", "3", "4", "5")))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyMap(),
                    mapOf("1" to 1),
                    mapOf(
                        "1" to 1,
                        "2" to null,
                        "3" to 3
                    )
                )
            )

        val observer = repository.getColoursForServicesFlow(setOf("1", "2", "3", "4", "5"))
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            null,
            mapOf("1" to 1),
            mapOf(
                "1" to 1,
                "3" to 3
            )
        )
    }

    @Test
    fun getColoursForServicesFlowWithNonNullOverrideAndNullServices() = runTest {
        val repository = ServicesRepository(serviceDao, serviceColourOverride)
        whenever(serviceColourOverride.overrideServiceColour(eq("1"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour(eq("2"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour("3", 3))
            .thenReturn(33)
        whenever(serviceDao.getColoursForServicesFlow(null))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyMap(),
                    mapOf("1" to 1),
                    mapOf(
                        "1" to 1,
                        "2" to null,
                        "3" to 3
                    )
                )
            )

        val observer = repository.getColoursForServicesFlow(null).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            null,
            mapOf("1" to 1),
            mapOf(
                "1" to 1,
                "3" to 33
            )
        )
    }

    @Test
    fun getColoursForServicesFlowWithNonNullOverrideAndEmptyServices() = runTest {
        val repository = ServicesRepository(serviceDao, serviceColourOverride)
        whenever(serviceColourOverride.overrideServiceColour(eq("1"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour(eq("2"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour("3", 3))
            .thenReturn(33)
        whenever(serviceDao.getColoursForServicesFlow(emptySet()))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyMap(),
                    mapOf("1" to 1),
                    mapOf(
                        "1" to 1,
                        "2" to null,
                        "3" to 3
                    )
                )
            )

        val observer = repository.getColoursForServicesFlow(emptySet()).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            null,
            mapOf("1" to 1),
            mapOf(
                "1" to 1,
                "3" to 33
            )
        )
    }

    @Test
    fun getColoursForServicesFlowWithNonNullOverrideAndNonEmptyServices() = runTest {
        val repository = ServicesRepository(serviceDao, serviceColourOverride)
        whenever(serviceColourOverride.overrideServiceColour(eq("1"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour(eq("2"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour("3", null))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour("3", 3))
            .thenReturn(33)
        whenever(serviceColourOverride.overrideServiceColour(eq("4"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour(eq("5"), anyOrNull()))
            .thenReturn(5)
        whenever(serviceDao.getColoursForServicesFlow(setOf("1", "2", "3", "4", "5")))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyMap(),
                    mapOf("1" to 1),
                    mapOf(
                        "1" to 1,
                        "2" to null,
                        "3" to 3
                    )
                )
            )

        val observer = repository.getColoursForServicesFlow(setOf("1", "2", "3", "4", "5"))
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            mapOf("5" to 5),
            mapOf("5" to 5),
            mapOf(
                "1" to 1,
                "5" to 5
            ),
            mapOf(
                "1" to 1,
                "3" to 33,
                "5" to 5
            )
        )
    }

    @Test
    fun getServiceDetailsFlowEmitsExpectedValuesWhenOverrideIsNull() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.getServiceDetailsFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    listOf(FakeServiceDetails("1", "Route", 1)),
                    listOf(
                        FakeServiceDetails("1", "Route", 1),
                        FakeServiceDetails("2", "Route 2", 2)
                    )
                )
            )

        val observer = repository.getServiceDetailsFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(ServiceDetails("1", "Route", 1)),
            listOf(
                ServiceDetails("1", "Route", 1),
                ServiceDetails("2", "Route 2", 2)
            )
        )
    }

    @Test
    fun getServiceDetailsFlowEmitsExpectedValuesWhenOverrideIsNotNull() = runTest {
        val repository = ServicesRepository(serviceDao, serviceColourOverride)
        whenever(serviceColourOverride.overrideServiceColour("1", 1))
            .thenReturn(10)
        whenever(serviceColourOverride.overrideServiceColour("2", 2))
            .thenReturn(20)
        whenever(serviceDao.getServiceDetailsFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    listOf(FakeServiceDetails("1", "Route", 1)),
                    listOf(
                        FakeServiceDetails("1", "Route", 1),
                        FakeServiceDetails("2", "Route 2", 2)
                    )
                )
            )

        val observer = repository.getServiceDetailsFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(ServiceDetails("1", "Route", 10)),
            listOf(
                ServiceDetails("1", "Route", 10),
                ServiceDetails("2", "Route 2", 20)
            )
        )
    }

    @Test
    fun allServiceNamesWithColorFlowEmitsExpectedValuesWhenOverrideIsNull() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.allServiceNamesWithColourFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    listOf(FakeServiceWithColour("1", 1)),
                    listOf(
                        FakeServiceWithColour("1", 1),
                        FakeServiceWithColour("2", null),
                        FakeServiceWithColour("3", 3)
                    )
                )
            )

        val observer = repository.allServiceNamesWithColourFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(ServiceWithColour("1", 1)),
            listOf(
                ServiceWithColour("1", 1),
                ServiceWithColour("2", null),
                ServiceWithColour("3", 3)
            )
        )
    }

    @Test
    fun allServiceNamesWithColorFlowEmitsExpectedValuesWhenOverrideIsNotNull() = runTest {
        val repository = ServicesRepository(serviceDao, serviceColourOverride)
        whenever(serviceColourOverride.overrideServiceColour(eq("1"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour("2", null))
            .thenReturn(2)
        whenever(serviceColourOverride.overrideServiceColour("3", 3))
            .thenReturn(33)
        whenever(serviceDao.allServiceNamesWithColourFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    listOf(FakeServiceWithColour("1", 1)),
                    listOf(
                        FakeServiceWithColour("1", 1),
                        FakeServiceWithColour("2", null),
                        FakeServiceWithColour("3", 3)
                    )
                )
            )

        val observer = repository.allServiceNamesWithColourFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(ServiceWithColour("1", 1)),
            listOf(
                ServiceWithColour("1", 1),
                ServiceWithColour("2", 2),
                ServiceWithColour("3", 33)
            )
        )
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsExpectedValuesWhenOverrideIsNull() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.getServiceNamesWithColourFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    listOf(FakeServiceWithColour("1", 1)),
                    listOf(
                        FakeServiceWithColour("1", 1),
                        FakeServiceWithColour("2", null),
                        FakeServiceWithColour("3", 3)
                    )
                )
            )

        val observer = repository.getServiceNamesWithColourFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(ServiceWithColour("1", 1)),
            listOf(
                ServiceWithColour("1", 1),
                ServiceWithColour("2", null),
                ServiceWithColour("3", 3)
            )
        )
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsExpectedValuesWhenOverrideIsNotNull() = runTest {
        val repository = ServicesRepository(serviceDao, serviceColourOverride)
        whenever(serviceColourOverride.overrideServiceColour(eq("1"), anyOrNull()))
            .thenReturn(null)
        whenever(serviceColourOverride.overrideServiceColour("2", null))
            .thenReturn(2)
        whenever(serviceColourOverride.overrideServiceColour("3", 3))
            .thenReturn(33)
        whenever(serviceDao.getServiceNamesWithColourFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    listOf(FakeServiceWithColour("1", 1)),
                    listOf(
                        FakeServiceWithColour("1", 1),
                        FakeServiceWithColour("2", null),
                        FakeServiceWithColour("3", 3)
                    )
                )
            )

        val observer = repository.getServiceNamesWithColourFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(ServiceWithColour("1", 1)),
            listOf(
                ServiceWithColour("1", 1),
                ServiceWithColour("2", 2),
                ServiceWithColour("3", 33)
            )
        )
    }

    @Test
    fun hasServicesFlowEmitsFalseWhenServiceCountIsNull() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.serviceCountFlow)
            .thenReturn(flowOf(null))

        val observer = repository.hasServicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(false)
    }

    @Test
    fun hasServicesFlowEmitsFalseWhenServiceCountIs0() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.serviceCountFlow)
            .thenReturn(flowOf(0))

        val observer = repository.hasServicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(false)
    }

    @Test
    fun hasServicesFlowEmitsTrueWhenServiceCountIs1() = runTest {
        val repository = ServicesRepository(serviceDao, null)
        whenever(serviceDao.serviceCountFlow)
            .thenReturn(flowOf(1))

        val observer = repository.hasServicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(true)
    }

    private data class FakeServiceDetails(
        override val name: String,
        override val description: String?,
        override val colour: Int?) : StoredServiceDetails

    private data class FakeServiceWithColour(
        override val name: String,
        override val colour: Int?) : StoredServiceWithColour
}