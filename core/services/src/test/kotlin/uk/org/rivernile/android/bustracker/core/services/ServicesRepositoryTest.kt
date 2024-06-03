/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.service.FakeServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.FakeServiceDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.service.FakeServiceWithColour
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDao
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [ServicesRepository].
 *
 * @author Niall Scott
 */
class ServicesRepositoryTest {

    @Test
    fun getColoursForServicesFlowWithNullOverrideAndNullServices() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertNull(it)

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
                }
            ),
            serviceColourOverride = null,
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getColoursForServicesFlow(null).test {
            assertNull(awaitItem())
            assertNull(awaitItem())
            assertEquals(mapOf("1" to ServiceColours(1, 10)), awaitItem())
            assertEquals(
                mapOf(
                    "1" to ServiceColours(1, 10),
                    "3" to ServiceColours(3, 30)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowWithNullOverrideAndEmptyServices() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertEquals(emptySet(), it)

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
                }
            ),
            serviceColourOverride = null,
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getColoursForServicesFlow(emptySet()).test {
            assertNull(awaitItem())
            assertNull(awaitItem())
            assertEquals(mapOf("1" to ServiceColours(1, 10)), awaitItem())
            assertEquals(
                mapOf(
                    "1" to ServiceColours(1, 10),
                    "3" to ServiceColours(3, 30)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowWithNullOverrideAndNonEmptyServices() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertEquals(setOf("1", "2", "3", "4", "5"), it)

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
                }
            ),
            serviceColourOverride = null,
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getColoursForServicesFlow(setOf("1", "2", "3", "4", "5")).test {
            assertNull(awaitItem())
            assertNull(awaitItem())
            assertEquals(mapOf("1" to ServiceColours(1, 10)), awaitItem())
            assertEquals(
                mapOf(
                    "1" to ServiceColours(1, 10),
                    "3" to ServiceColours(3, 30)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowWithNonNullOverrideAndNullServices() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertNull(it)

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
                }
            ),
            serviceColourOverride = { serviceName, currentColour ->
                if (serviceName == "3" && currentColour == 3) 33 else null
            },
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getColoursForServicesFlow(null).test {
            assertNull(awaitItem())
            assertNull(awaitItem())
            assertEquals(mapOf("1" to ServiceColours(1, 10)), awaitItem())
            assertEquals(
                mapOf(
                    "1" to ServiceColours(1, 10),
                    "3" to ServiceColours(33, 330)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowWithNonNullOverrideAndEmptyServices() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertEquals(emptySet(), it)

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
                }
            ),
            serviceColourOverride = { serviceName, currentColour ->
                if (serviceName == "3" && currentColour == 3) 33 else null
            },
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getColoursForServicesFlow(emptySet()).test {
            assertNull(awaitItem())
            assertNull(awaitItem())
            assertEquals(mapOf("1" to ServiceColours(1, 10)), awaitItem())
            assertEquals(
                mapOf(
                    "1" to ServiceColours(1, 10),
                    "3" to ServiceColours(33, 330)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowWithNonNullOverrideAndNonEmptyServices() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertEquals(setOf("1", "2", "3", "4", "5"), it)

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
                }
            ),
            serviceColourOverride = { serviceName, currentColour ->
                when {
                    serviceName == "3" && currentColour == 3 -> 33
                    serviceName == "5" -> 5
                    else -> null
                }
            },
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getColoursForServicesFlow(setOf("1", "2", "3", "4", "5")).test {
            assertEquals(mapOf("5" to ServiceColours(5, 50)), awaitItem())
            assertEquals(mapOf("5" to ServiceColours(5, 50)), awaitItem())
            assertEquals(
                mapOf(
                    "1" to ServiceColours(1, 10),
                    "5" to ServiceColours(5, 50)
                ),
                awaitItem()
            )
            assertEquals(
                mapOf(
                    "1" to ServiceColours(1, 10),
                    "3" to ServiceColours(33, 330),
                    "5" to ServiceColours(5, 50)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceDetailsFlowEmitsExpectedValuesWhenOverrideIsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceDetailsFlow = {
                    assertEquals("123456", it)

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
                }
            ),
            serviceColourOverride = null,
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getServiceDetailsFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(
                listOf(
                    ServiceDetails("1", "Route", ServiceColours(1, 10))
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    ServiceDetails("1", "Route", ServiceColours(1, 10)),
                    ServiceDetails("2", "Route 2", ServiceColours(2, 20))
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceDetailsFlowEmitsExpectedValuesWhenOverrideIsNotNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceDetailsFlow = {
                    assertEquals("123456", it)

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
                }
            ),
            serviceColourOverride = { serviceName, currentColour ->
                when {
                    serviceName == "1" && currentColour == 1 -> 10
                    serviceName == "2" && currentColour == 2 -> 20
                    else -> null
                }
            },
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getServiceDetailsFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(
                listOf(
                    ServiceDetails("1", "Route", ServiceColours(10, 100))
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    ServiceDetails("1", "Route", ServiceColours(10, 100)),
                    ServiceDetails("2", "Route 2", ServiceColours(20, 200))
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allServiceNamesWithColorFlowEmitsExpectedValuesWhenOverrideIsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onAllServiceNamesWithColourFlow = {
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
                }
            ),
            serviceColourOverride = null,
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.allServiceNamesWithColourFlow.test {
            assertNull(awaitItem())
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10))
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10)),
                    ServiceWithColour("2", null),
                    ServiceWithColour("3", ServiceColours(3, 30))
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allServiceNamesWithColorFlowEmitsExpectedValuesWhenOverrideIsNotNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onAllServiceNamesWithColourFlow = {
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
                }
            ),
            serviceColourOverride = { serviceName, currentColour ->
                when {
                    serviceName == "2" && currentColour == null -> 2
                    serviceName == "3" && currentColour == 3 -> 33
                    else -> null
                }
            },
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.allServiceNamesWithColourFlow.test {
            assertNull(awaitItem())
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10))
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10)),
                    ServiceWithColour("2", ServiceColours(2, 20)),
                    ServiceWithColour("3", ServiceColours(33, 330))
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsExpectedValuesWhenOverrideIsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceNamesWithColourFlow = {
                    assertEquals("123456", it)

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
                }
            ),
            serviceColourOverride = null,
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getServiceNamesWithColourFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10))
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10)),
                    ServiceWithColour("2", null),
                    ServiceWithColour("3", ServiceColours(3, 30))
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsExpectedValuesWhenOverrideIsNotNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceNamesWithColourFlow = {
                    assertEquals("123456", it)

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
                }
            ),
            serviceColourOverride = { serviceName, currentColour ->
                when {
                    serviceName == "2" && currentColour == null -> 2
                    serviceName == "3" && currentColour == 3 -> 33
                    else -> null
                }
            },
            serviceColoursGenerator = { colour -> colour?.let { ServiceColours(it, it * 10) } }
        )

        repository.getServiceNamesWithColourFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10))
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    ServiceWithColour("1", ServiceColours(1, 10)),
                    ServiceWithColour("2", ServiceColours(2, 20)),
                    ServiceWithColour("3", ServiceColours(33, 330))
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun hasServicesFlowEmitsFalseWhenServiceCountIsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onServiceCountFlow = { flowOf(null) }
            )
        )

        repository.hasServicesFlow.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun hasServicesFlowEmitsFalseWhenServiceCountIs0() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onServiceCountFlow = { flowOf(0) }
            )
        )

        repository.hasServicesFlow.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun hasServicesFlowEmitsTrueWhenServiceCountIs1() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onServiceCountFlow = { flowOf(1) }
            )
        )

        repository.hasServicesFlow.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    private fun createServicesRepository(
        serviceDao: ServiceDao = FakeServiceDao(),
        serviceColourOverride: ((String, Int?) -> Int?)? = { _, _ -> null },
        serviceColoursGenerator: (Int?) -> ServiceColours? = { null }
    ): ServicesRepository {
        val serviceColourOverrideCallback = serviceColourOverride?.let {
            object : ServiceColourOverride {
                override fun overrideServiceColour(serviceName: String, currentColour: Int?) =
                    it(serviceName, currentColour)
            }
        }

        return ServicesRepository(
            serviceDao,
            serviceColourOverrideCallback,
            object : ServiceColoursGenerator {
                override fun generateServiceColours(serviceColour: Int?) =
                    serviceColoursGenerator(serviceColour)
            }
        )
    }
}