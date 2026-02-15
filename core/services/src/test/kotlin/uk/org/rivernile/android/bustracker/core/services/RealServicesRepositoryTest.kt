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

package uk.org.rivernile.android.bustracker.core.services

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.service.FakeServiceColours
import uk.org.rivernile.android.bustracker.core.database.busstop.service.FakeServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.FakeServiceDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.service.FakeServiceWithColour
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDao
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
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
class RealServicesRepositoryTest {

    @Test
    fun getColoursForServicesFlowEmitsNullWhenValueFromDaoIsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        repository.getColoursForServicesFlow(null).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowPassesServiceDescriptorsToDao() = runTest {
        val serviceDescriptors = setOf(
            FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            )
        )
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertEquals(serviceDescriptors, it)
                    flowOf(null)
                }
            )
        )

        repository.getColoursForServicesFlow(serviceDescriptors).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowEmitsNullWhenValueFromDaoIsEmpty() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(emptyMap())
                }
            )
        )

        repository.getColoursForServicesFlow(null).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getColourForServicesFlowEmitsValuesWhenValueFromDaoIsPopulated() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(
                        mapOf(
                            FakeServiceDescriptor(
                                serviceName = "1",
                                operatorCode = "TEST1"
                            ) to FakeServiceColours(
                                colourPrimary = 1,
                                colourOnPrimary = 100
                            )
                        )
                    )
                }
            )
        )

        repository.getColoursForServicesFlow(null).test {
            assertEquals(
                mapOf<ServiceDescriptor, ServiceColours>(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ) to ServiceColours(
                        colourPrimary = 1,
                        colourOnPrimary = 100
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getColourForServicesFlowPassesServiceColoursGenerator() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(
                        mapOf(
                            FakeServiceDescriptor(
                                serviceName = "1",
                                operatorCode = "TEST1"
                            ) to FakeServiceColours(
                                colourPrimary = 1,
                                colourOnPrimary = null
                            )
                        )
                    )
                }
            ),
            serviceColoursGenerator = FakeServiceColoursGenerator(
                onGenerateColourOnPrimary = { 999 }
            )
        )

        repository.getColoursForServicesFlow(null).test {
            assertEquals(
                mapOf<ServiceDescriptor, ServiceColours>(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ) to ServiceColours(
                        colourPrimary = 1,
                        colourOnPrimary = 999
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getColoursForServicesFlowDoesNotIncludeServicesWithNoColours() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(
                        mapOf(
                            ServiceDescriptor(
                                serviceName = "1",
                                operatorCode = "TEST1"
                            ) to FakeServiceColours(
                                colourPrimary = null,
                                colourOnPrimary = null
                            )
                        )
                    )
                }
            )
        )

        repository.getColoursForServicesFlow(null).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun getServiceDetailsFlowWithNonNaptanIdentifierThrowsException() = runTest {
        val repository = createServicesRepository()

        repository.getServiceDetailsFlow("123456".toAtcoStopIdentifier()).first()
    }

    @Test
    fun getServiceDetailsFlowEmitsNullWhenDaoEmitsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceDetailsFlow = {
                    assertEquals("123456", it)
                    flowOf(null)
                }
            )
        )

        repository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceDetailsFlowEmitsNullWhenDaoEmitsEmptyList() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceDetailsFlow = {
                    assertEquals("123456", it)
                    flowOf(emptyList())
                }
            )
        )

        repository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceDetailsFlowEmitsItemWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceDetailsFlow = {
                    assertEquals("123456", it)
                    flowOf(
                        listOf(
                            FakeServiceDetails(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                description = "Description",
                                colours = FakeServiceColours(
                                    colourPrimary = 1,
                                    colourOnPrimary = 100
                                )
                            )
                        )
                    )
                }
            )
        )

        repository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                listOf(
                    ServiceDetails(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        description = "Description",
                        colours = ServiceColours(
                            colourPrimary = 1,
                            colourOnPrimary = 100
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceDetailsFlowEmitsItemWithAmendedColourWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceDetailsFlow = {
                    assertEquals("123456", it)
                    flowOf(
                        listOf(
                            FakeServiceDetails(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                description = "Description",
                                colours = FakeServiceColours(
                                    colourPrimary = 1,
                                    colourOnPrimary = null
                                )
                            )
                        )
                    )
                }
            ),
            serviceColoursGenerator = FakeServiceColoursGenerator(
                onGenerateColourOnPrimary = { 999 }
            )
        )

        repository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                listOf(
                    ServiceDetails(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        description = "Description",
                        colours = ServiceColours(
                            colourPrimary = 1,
                            colourOnPrimary = 999
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceDetailsFlowEmitsItemWithRemovedColourWhenDaoEmitsItemWithoutColours() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceDetailsFlow = {
                    assertEquals("123456", it)
                    flowOf(
                        listOf(
                            FakeServiceDetails(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                description = "Description",
                                colours = FakeServiceColours(
                                    colourPrimary = null,
                                    colourOnPrimary = null
                                )
                            )
                        )
                    )
                }
            )
        )

        repository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                listOf(
                    ServiceDetails(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        description = "Description",
                        colours = null
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allServiceNamesWithColourFlowEmitsNullWhenDaoEmitsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onAllServiceNamesWithColourFlow = {
                    flowOf(null)
                }
            )
        )

        repository.allServiceNamesWithColourFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allServiceNamesWithColourFlowEmitsNullWhenDaoEmitsEmptyList() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onAllServiceNamesWithColourFlow = {
                    flowOf(emptyList())
                }
            )
        )

        repository.allServiceNamesWithColourFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allServiceNamesWithColoursFlowEmitsItemWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onAllServiceNamesWithColourFlow = {
                    flowOf(
                        listOf(
                            FakeServiceWithColour(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                colours = FakeServiceColours(
                                    colourPrimary = 1,
                                    colourOnPrimary = 100
                                )
                            )
                        )
                    )
                }
            )
        )

        repository.allServiceNamesWithColourFlow.test {
            assertEquals(
                listOf(
                    ServiceWithColour(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        colours = ServiceColours(
                            colourPrimary = 1,
                            colourOnPrimary = 100
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allServiceNamesWithColoursFlowEmitsItemWithAmendedColoursWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onAllServiceNamesWithColourFlow = {
                    flowOf(
                        listOf(
                            FakeServiceWithColour(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                colours = FakeServiceColours(
                                    colourPrimary = 1,
                                    colourOnPrimary = null
                                )
                            )
                        )
                    )
                }
            ),
            serviceColoursGenerator = FakeServiceColoursGenerator(
                onGenerateColourOnPrimary = { 999 }
            )
        )

        repository.allServiceNamesWithColourFlow.test {
            assertEquals(
                listOf(
                    ServiceWithColour(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        colours = ServiceColours(
                            colourPrimary = 1,
                            colourOnPrimary = 999
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allServiceNamesWithColoursFlowEmitsItemWithoutColoursWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onAllServiceNamesWithColourFlow = {
                    flowOf(
                        listOf(
                            FakeServiceWithColour(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                colours = FakeServiceColours(
                                    colourPrimary = null,
                                    colourOnPrimary = null
                                )
                            )
                        )
                    )
                }
            )
        )

        repository.allServiceNamesWithColourFlow.test {
            assertEquals(
                listOf(
                    ServiceWithColour(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        colours = null
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun getServiceNamesWithColourFlowWhenStopIdentifierNotNaptanCodeThrowsException() = runTest {
        val repository = createServicesRepository()

        repository.getServiceNamesWithColourFlow("123456".toAtcoStopIdentifier()).first()
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsNullWhenDaoEmitsNull() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceNamesWithColourFlow = {
                    assertEquals("123456", it)
                    flowOf(null)
                }
            )
        )

        repository.getServiceNamesWithColourFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsNullWhenDaoEmitsEmpty() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceNamesWithColourFlow = {
                    assertEquals("123456", it)
                    flowOf(emptyList())
                }
            )
        )

        repository.getServiceNamesWithColourFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsItemWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceNamesWithColourFlow = {
                    assertEquals("123456", it)
                    flowOf(
                        listOf(
                            FakeServiceWithColour(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                colours = FakeServiceColours(
                                    colourPrimary = 1,
                                    colourOnPrimary = 100
                                )
                            )
                        )
                    )
                }
            )
        )

        repository.getServiceNamesWithColourFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                listOf(
                    ServiceWithColour(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        colours = ServiceColours(
                            colourPrimary = 1,
                            colourOnPrimary = 100
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsItemWithAmendedColoursWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceNamesWithColourFlow = {
                    assertEquals("123456", it)
                    flowOf(
                        listOf(
                            FakeServiceWithColour(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                colours = FakeServiceColours(
                                    colourPrimary = 1,
                                    colourOnPrimary = null
                                )
                            )
                        )
                    )
                }
            ),
            serviceColoursGenerator = FakeServiceColoursGenerator(
                onGenerateColourOnPrimary = { 999 }
            )
        )

        repository.getServiceNamesWithColourFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                listOf(
                    ServiceWithColour(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        colours = ServiceColours(
                            colourPrimary = 1,
                            colourOnPrimary = 999
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getServiceNamesWithColourFlowEmitsItemWithColoursRemovedWhenDaoEmitsItem() = runTest {
        val repository = createServicesRepository(
            serviceDao = FakeServiceDao(
                onGetServiceNamesWithColourFlow = {
                    assertEquals("123456", it)
                    flowOf(
                        listOf(
                            FakeServiceWithColour(
                                descriptor = FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                colours = FakeServiceColours(
                                    colourPrimary = null,
                                    colourOnPrimary = null
                                )
                            )
                        )
                    )
                }
            )
        )

        repository.getServiceNamesWithColourFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                listOf(
                    ServiceWithColour(
                        serviceDescriptor = FakeServiceDescriptor(
                            serviceName = "1",
                            operatorCode = "TEST1"
                        ),
                        colours = null
                    )
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
        serviceColoursGenerator: ServiceColoursGenerator = FakeServiceColoursGenerator()
    ): RealServicesRepository {
        return RealServicesRepository(
            serviceDao,
            serviceColoursGenerator
        )
    }
}
