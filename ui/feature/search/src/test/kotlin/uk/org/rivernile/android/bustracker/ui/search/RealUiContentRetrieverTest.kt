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

package uk.org.rivernile.android.bustracker.ui.search

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeBusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopSearchResult
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [RealUiContentRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RealUiContentRetrieverTest {

    @Test
    fun uiContentFlowEmitsEmptySearchTermWhenSearchTermIsNull() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf(null) }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEmptySearchTermWhenSearchTermIsEmpty() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("") }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEmptySearchTermWhenSearchTermIsBlank() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf(" ") }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEmptySearchTermWhenSearchTermIsLessThan3Chars() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("ab") }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowDoesNotDebounceSearchTermWhenSearchTermIsInvalid() = runTest {
        val searchTerms = listOf("a", "b", "aa", "bb")
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = {
                    flow {
                        searchTerms.forEach { searchTerm ->
                            emit(searchTerm)
                            delay((SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS - 1L).milliseconds)
                        }
                    }
                }
            )
        )

        retriever.uiContentFlow.test {
            repeat(searchTerms.size) {
                assertEquals(UiContent.EmptySearchTerm, awaitItem())
            }

            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowDebouncesSearchTermsWhenValidButEmittedTooQuickly() = runTest {
        val searchTerms = listOf("abc", "abcd")
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = {
                    flow {
                        searchTerms.forEach { searchTerm ->
                            emit(searchTerm)
                            delay(SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS.milliseconds)
                        }
                    }
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals(searchTerms.last(), searchTerm)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.NoResults, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowDoesNotDebounceSearchTermsWhenValidButEmittedOutsideTimeframe() = runTest {
        val searchTerms = listOf("abc", "abcd")
        val searchTermsQueue = ArrayDeque(searchTerms)
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = {
                    flow {
                        searchTerms.forEach { searchTerm ->
                            emit(searchTerm)
                            delay((SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS + 1L).milliseconds)
                        }
                    }
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals(searchTermsQueue.removeFirst(), searchTerm)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.NoResults, awaitItem())
            assertEquals(UiContent.NoResults, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsInProgressWhenDataIsLoading() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("abc") }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals("abc", searchTerm)
                    flow {
                        delay((SEARCH_PROGRESS_DELAY_MILLIS + 1L).milliseconds)
                        emit(null)
                    }
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.NoResults, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsNoResultsWhenSearchEmitsNull() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("abc") }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals("abc", searchTerm)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.NoResults, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsNoResultsWhenSearchEmitsEmptyList() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("abc") }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals("abc", searchTerm)
                    flowOf(emptyList())
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.NoResults, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsContentWhenSearchEmitsSearchResults() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("abc") }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals("abc", searchTerm)
                    flowOf(
                        listOf(
                            FakeStopSearchResult(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                stopName = FakeStopName(
                                    name = "Name 1",
                                    locality = "Locality 1"
                                ),
                                orientation = StopOrientation.NORTH_EAST,
                                serviceListing = listOf(
                                    FakeServiceDescriptor(
                                        serviceName = "2",
                                        operatorCode = "TEST2"
                                    ),
                                    FakeServiceDescriptor(
                                        serviceName = "1",
                                        operatorCode = "TEST1"
                                    ),
                                    FakeServiceDescriptor(
                                        serviceName = "3",
                                        operatorCode = "TEST3"
                                    )
                                )
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { services ->
                    assertNull(services)
                    flowOf(
                        mapOf(
                            FakeServiceDescriptor(
                                serviceName = "1",
                                operatorCode = "TEST1"
                            ) to ServiceColours(
                                colourPrimary = 1,
                                colourOnPrimary = 2
                            ),
                            FakeServiceDescriptor(
                                serviceName = "3",
                                operatorCode = "TEST3"
                            ) to ServiceColours(
                                colourPrimary = 3,
                                colourOnPrimary = 4
                            )
                        )
                    )
                }
            ),
            stopSearchResultDropdownMenuGenerator = FakeUiStopSearchResultDropdownMenuGenerator(
                onGetDropdownMenuItemsForStopsFlow = { stopIdentifiers ->
                    assertEquals(setOf("123456".toNaptanStopIdentifier()), stopIdentifiers)
                    flowOf(
                        mapOf(
                            "123456".toNaptanStopIdentifier() to UiStopSearchResultDropdownMenu(
                                isShown = true,
                                isStopMapItemShown = true
                            )
                        )
                    )
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(
                UiContent.Content(
                    results = persistentListOf(
                        UiStopSearchResult(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Name 1",
                                locality = "Locality 1"
                            ),
                            orientation = StopOrientation.NORTH_EAST,
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
                            dropdownMenu = UiStopSearchResultDropdownMenu(
                                isShown = true,
                                isStopMapItemShown = true
                            )
                        )
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    private fun TestScope.createRetriever(
        state: State = FakeState(),
        servicesRepository: ServicesRepository = FakeServicesRepository(
            onGetColoursForServicesFlow = { services ->
                assertNull(services)
                flowOf(null)
            }
        ),
        busStopsRepository: BusStopsRepository = FakeBusStopsRepository(),
        stopSearchResultDropdownMenuGenerator: UiStopSearchResultDropdownMenuGenerator =
            FakeUiStopSearchResultDropdownMenuGenerator(),
        serviceNameComparator: Comparator<String> = naturalOrder()
    ): RealUiContentRetriever {
        return RealUiContentRetriever(
            state = state,
            servicesRepository = servicesRepository,
            busStopsRepository = busStopsRepository,
            stopSearchResultDropdownMenuGenerator = stopSearchResultDropdownMenuGenerator,
            alphanumericComparator = serviceNameComparator,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }
}
