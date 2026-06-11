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

package uk.org.rivernile.android.bustracker.core.operators

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.FakeOperatorDao
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.FakeOperatorName
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.OperatorDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealOperatorsRepository].
 *
 * @author Niall Scott
 */
class RealOperatorsRepositoryTest {

    @Test
    fun allOperatorNamesFlowHandlesNullItems() = runTest {
        val repository = createRepository(
            operatorDao = FakeOperatorDao(
                onAllOperatorNamesFlow = {
                    flowOf(null)
                }
            )
        )

        repository.allOperatorNamesFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allOperatorNamesFlowHandlesEmptyItems() = runTest {
        val repository = createRepository(
            operatorDao = FakeOperatorDao(
                onAllOperatorNamesFlow = {
                    flowOf(emptyMap())
                }
            )
        )

        repository.allOperatorNamesFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allOperatorNamesFlowHandlesPopulatedItems() = runTest {
        val repository = createRepository(
            operatorDao = FakeOperatorDao(
                onAllOperatorNamesFlow = {
                    flowOf(
                        mapOf(
                            "1" to FakeOperatorName(displayName = "One"),
                            "2" to FakeOperatorName(displayName = "Two"),
                            "3" to FakeOperatorName(displayName = "Three")
                        )
                    )
                }
            )
        )

        repository.allOperatorNamesFlow.test {
            assertEquals(
                mapOf(
                    "1" to OperatorName(displayName = "One"),
                    "2" to OperatorName(displayName = "Two"),
                    "3" to OperatorName(displayName = "Three")
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRepository(
        operatorDao: OperatorDao = FakeOperatorDao()
    ): RealOperatorsRepository {
        return RealOperatorsRepository(
            operatorDao = operatorDao
        )
    }
}
