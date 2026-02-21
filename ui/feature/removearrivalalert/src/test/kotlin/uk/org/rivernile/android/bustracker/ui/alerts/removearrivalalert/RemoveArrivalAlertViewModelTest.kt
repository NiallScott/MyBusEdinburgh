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

package uk.org.rivernile.android.bustracker.ui.alerts.removearrivalalert

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Tests for [RemoveArrivalAlertViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RemoveArrivalAlertViewModelTest {

    @Test
    fun onUserConfirmRemovalDoesNotAttemptToRemoveAlertWhenStopIdentifierIsNull() = runTest {
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { null }
            ),
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithStopIdentifier = {
                    fail("Not expecting any arrival alerts to be removed.")
                }
            )
        )

        viewModel.onUserConfirmRemoval()
    }

    @Test
    fun onUserConfirmRemovalCausesRemovalWhenStopIdentifierIsPopulated() = runTest {
        var removalCount = 0
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithStopIdentifier = { stopIdentifier ->
                    assertEquals("123456".toNaptanStopIdentifier(), stopIdentifier)
                    removalCount++
                }
            )
        )

        viewModel.onUserConfirmRemoval()

        assertEquals(1, removalCount)
    }

    private fun TestScope.createViewModel(
        arguments: Arguments = FakeArguments(),
        alertsRepository: AlertsRepository = FakeAlertsRepository()
    ): RemoveArrivalAlertViewModel {
        return RemoveArrivalAlertViewModel(
            arguments = arguments,
            alertsRepository = alertsRepository,
            defaultDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            applicationCoroutineScope = this,
            viewModelCoroutineScope = backgroundScope
        )
    }
}
