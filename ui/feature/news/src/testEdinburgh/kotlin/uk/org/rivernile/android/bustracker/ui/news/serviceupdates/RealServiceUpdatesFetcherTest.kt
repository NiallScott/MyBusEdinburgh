/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.updates.FakeServiceUpdateRepository
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdateRepository
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealServiceUpdatesFetcher].
 *
 * @author Niall Scott
 */
class RealServiceUpdatesFetcherTest {

    @Test
    fun serviceUpdatesFlowFirstSubscribeImmediatelyCausesRefresh() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesRepository = FakeServiceUpdateRepository(
                onServiceUpdatesFlow = {
                    flowOf(
                        ServiceUpdatesResult.InProgress,
                        ServiceUpdatesResult.Success(
                            serviceUpdates = null,
                            loadTimeMillis = 123L
                        )
                    )
                }
            )
        )

        fetcher.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesResult.Success(
                    serviceUpdates = null,
                    loadTimeMillis = 123L
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }

        fetcher.close()
    }

    @Test
    fun serviceUpdatesFlowRefreshCausesRefreshOfData() = runTest {
        var iteration = 0
        val fetcher = createFetcher(
            serviceUpdatesRepository = FakeServiceUpdateRepository(
                onServiceUpdatesFlow = {
                    when (iteration) {
                        0 -> flowOf(
                            ServiceUpdatesResult.InProgress,
                            ServiceUpdatesResult.Success(
                                serviceUpdates = null,
                                loadTimeMillis = 123L
                            )
                        )
                        1 -> flowOf(
                            ServiceUpdatesResult.InProgress,
                            ServiceUpdatesResult.Error.Server(loadTimeMillis = 456L)
                        )
                        else -> throw IndexOutOfBoundsException()
                    }.also { iteration++ }
                }
            )
        )

        fetcher.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesResult.Success(
                    serviceUpdates = null,
                    loadTimeMillis = 123L
                ),
                awaitItem()
            )

            fetcher.refresh()

            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Error.Server(loadTimeMillis = 456L), awaitItem())
            ensureAllEventsConsumed()
        }

        fetcher.close()
    }

    @Test
    fun serviceUpdatesFlowAfterCloseCausesNoOpOnRefresh() = runTest {
        var iteration = 0
        val fetcher = createFetcher(
            serviceUpdatesRepository = FakeServiceUpdateRepository(
                onServiceUpdatesFlow = {
                    when (iteration) {
                        0 -> flowOf(
                            ServiceUpdatesResult.InProgress,
                            ServiceUpdatesResult.Success(
                                serviceUpdates = null,
                                loadTimeMillis = 123L
                            )
                        )
                        else -> throw IndexOutOfBoundsException()
                    }.also { iteration++ }
                }
            )
        )

        fetcher.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesResult.Success(
                    serviceUpdates = null,
                    loadTimeMillis = 123L
                ),
                awaitItem()
            )

            fetcher.refresh()
            fetcher.close()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun serviceUpdatesFlowWithCloseCalledBeforeFirstSubscribeNeverEmitsValues() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesRepository = FakeServiceUpdateRepository(
                onServiceUpdatesFlow = { throw UnsupportedOperationException() }
            )
        )

        fetcher.close()
        fetcher.serviceUpdatesFlow.test {
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun serviceUpdatesFlowReEmitsPreviousItemOnResubscribe() = runTest {
        var iteration = 0
        val fetcher = createFetcher(
            serviceUpdatesRepository = FakeServiceUpdateRepository(
                onServiceUpdatesFlow = {
                    when (iteration) {
                        0 -> flowOf(
                            ServiceUpdatesResult.InProgress,
                            ServiceUpdatesResult.Success(
                                serviceUpdates = null,
                                loadTimeMillis = 123L
                            )
                        )
                        else -> throw IndexOutOfBoundsException()
                    }.also { iteration++ }
                }
            )
        )

        fetcher.close()

        fetcher.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesResult.Success(
                    serviceUpdates = null,
                    loadTimeMillis = 123L
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }

        fetcher.serviceUpdatesFlow.test {
            assertEquals(
                ServiceUpdatesResult.Success(
                    serviceUpdates = null,
                    loadTimeMillis = 123L
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    private fun TestScope.createFetcher(
        serviceUpdatesRepository: ServiceUpdateRepository = FakeServiceUpdateRepository()
    ): RealServiceUpdatesFetcher {
        return RealServiceUpdatesFetcher(
            serviceUpdateRepository = serviceUpdatesRepository,
            viewModelCoroutineScope = backgroundScope
        )
    }
}