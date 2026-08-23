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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeBusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopSearchResult
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [RealStopSearchResultRetriever].
 *
 * @author Niall Scott
 */
class RealStopSearchResultRetrieverTest {

    @Test
    fun stopSearchResultStateFlowEmitsEmptySearchTermWhenSearchTermIsNull() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf(null) }
            )
        )

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowEmitsEmptySearchTermWhenSearchTermIsEmpty() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("") }
            )
        )

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowEmitsEmptySearchTermWhenSearchTermIsBlank() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf(" ") }
            )
        )

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowEmitsEmptySearchTermWhenSearchTermIsLessThan3Chars() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("ab") }
            )
        )

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.EmptySearchTerm, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowEmitsEmptySearchTermWhenSearchTermIsLessThan3CharsWithSpaces() =
        runTest {
            val retriever = createRetriever(
                state = FakeState(
                    onSearchTermFlow = { flowOf(" ab") }
                )
            )

            retriever.stopSearchResultStateFlow.test {
                assertEquals(StopSearchResultState.EmptySearchTerm, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun stopSearchResultStateFlowDoesNotDebounceSearchTermWhenSearchTermIsInvalid() = runTest {
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

        retriever.stopSearchResultStateFlow.test {
            repeat(searchTerms.size) {
                assertEquals(StopSearchResultState.EmptySearchTerm, awaitItem())
            }

            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowDebouncesSearchTermsWhenValidButEmittedTooQuickly() = runTest {
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

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.InProgress, awaitItem())
            assertEquals(StopSearchResultState.Results(results = null), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowDoesNotDebounceSearchTermsWhenValidButEmittedOutsideTimeframe() =
        runTest {
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

            retriever.stopSearchResultStateFlow.test {
                assertEquals(StopSearchResultState.InProgress, awaitItem())
                assertEquals(StopSearchResultState.Results(results = null), awaitItem())
                assertEquals(StopSearchResultState.InProgress, awaitItem())
                assertEquals(StopSearchResultState.Results(results = null), awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun stopSearchResultStateFlowTrimsTheSearchTerm() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf(" abc ") }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals("abc", searchTerm)
                    flowOf(null)
                }
            )
        )

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.InProgress, awaitItem())
            assertEquals(StopSearchResultState.Results(results = null), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowEmitsInProgressWhenDataIsLoading() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("abc") }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals("abc", searchTerm)
                    flow {
                        delay(1.milliseconds)
                        emit(null)
                    }
                }
            )
        )

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.InProgress, awaitItem())
            assertEquals(StopSearchResultState.Results(results = null), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun stopSearchResultStateFlowEmitsResultsWhenResultsIsPopulated() = runTest {
        val stopSearchResults = listOf(
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
        val retriever = createRetriever(
            state = FakeState(
                onSearchTermFlow = { flowOf("abc") }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopSearchResultsFlow = { searchTerm ->
                    assertEquals("abc", searchTerm)
                    flowOf(stopSearchResults)
                }
            )
        )

        retriever.stopSearchResultStateFlow.test {
            assertEquals(StopSearchResultState.InProgress, awaitItem())
            assertEquals(
                StopSearchResultState.Results(
                    results = stopSearchResults
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRetriever(
        state: State = FakeState(),
        busStopsRepository: BusStopsRepository = FakeBusStopsRepository()
    ): RealStopSearchResultRetriever {
        return RealStopSearchResultRetriever(
            state = state,
            busStopsRepository = busStopsRepository
        )
    }
}
