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

package uk.org.rivernile.android.bustracker.ui.favouritestops.addoredit

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeBusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FakeFavouritesRepository
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealUiContentFetcher].
 *
 * @author Niall Scott
 */
class RealUiContentFetcherTest {

    @Test
    fun uiContentFlowEmitsInProgressWhenStopIdentifierIsNull() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf(null) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAddItemWhenStopIsNotAFavouriteWithoutStopName() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf(null) }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(null)
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(null)
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Add(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = null,
                    isPositiveButtonEnabled = false
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAddItemWhenStopIsNotAFavourite() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf(null) }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(null)
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(FakeStopName(name = "Stop Name", locality = "Locality"))
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Add(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = UiStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    isPositiveButtonEnabled = false
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEditItemWhenStopIsAFavouriteWithoutStopName() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf(null) }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(
                        FavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = "Saved Name"
                        )
                    )
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(null)
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Edit(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = null,
                    isPositiveButtonEnabled = false,
                    savedName = "Saved Name"
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEditItemWhenStopIsAFavourite() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf(null) }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(
                        FavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = "Saved Name"
                        )
                    )
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(FakeStopName(name = "Stop Name", locality = "Locality"))
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Edit(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = UiStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    isPositiveButtonEnabled = false,
                    savedName = "Saved Name"
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAddWithPositiveButtonDisabledWhenStopNameIsNotValid() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf("") }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(null)
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(FakeStopName(name = "Stop Name", locality = "Locality"))
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Add(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = UiStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    isPositiveButtonEnabled = false
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsAddWithPositiveButtonEnabledWhenStopNameIsValid() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf("A") }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(null)
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(FakeStopName(name = "Stop Name", locality = "Locality"))
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Add(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = UiStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    isPositiveButtonEnabled = true
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEditWithPositiveButtonDisabledWhenStopNameIsNotValid() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf("") }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(
                        FavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = "Saved Name"
                        )
                    )
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(FakeStopName(name = "Stop Name", locality = "Locality"))
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Edit(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = UiStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    isPositiveButtonEnabled = false,
                    savedName = "Saved Name"
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsEditWithPositiveButtonEnabledWhenStopNameIsValid() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf("A") }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(
                        FavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = "Saved Name"
                        )
                    )
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(FakeStopName(name = "Stop Name", locality = "Locality"))
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Edit(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = UiStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    isPositiveButtonEnabled = true,
                    savedName = "Saved Name"
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsExpectedValuesWhenTransitionFromNullStopIdToPopulated() = runTest {
        val fetcher = createUiContentFetcher(
            arguments = FakeArguments(
                onStopIdentifierFlow = {
                    flow {
                        emit(null)
                        delay(1L)
                        emit("123456".toNaptanStopIdentifier())
                    }
                }
            ),
            state = FakeState(
                onStopNameTextFlow = { flowOf(null) }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onGetFavouriteStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(null)
                }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetNameForStopFlow = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    flowOf(FakeStopName(name = "Stop Name", locality = "Locality"))
                }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Mode.Add(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = UiStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    isPositiveButtonEnabled = false
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createUiContentFetcher(
        arguments: Arguments = FakeArguments(),
        state: State = FakeState(),
        favouritesRepository: FavouritesRepository = FakeFavouritesRepository(),
        busStopsRepository: BusStopsRepository = FakeBusStopsRepository()
    ): UiContentFetcher {
        return RealUiContentFetcher(
            arguments = arguments,
            state = state,
            favouritesRepository = favouritesRepository,
            busStopsRepository = busStopsRepository
        )
    }
}
