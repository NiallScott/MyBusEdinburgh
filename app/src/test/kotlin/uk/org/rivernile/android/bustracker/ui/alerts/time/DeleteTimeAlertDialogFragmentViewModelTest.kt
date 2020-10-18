/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.time

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [DeleteTimeAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DeleteTimeAlertDialogFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertsRepository: AlertsRepository

    private lateinit var viewModel: DeleteTimeAlertDialogFragmentViewModel

    @Before
    fun setUp() {
        viewModel = DeleteTimeAlertDialogFragmentViewModel(
                alertsRepository,
                coroutineRule,
                coroutineRule.testDispatcher)
    }

    @Test
    fun onUserConfirmDeletionDoesNotCauseDeletionWhenStopCodeIsNull() =
            coroutineRule.runBlockingTest {
        viewModel.stopCode = null

        viewModel.onUserConfirmDeletion()

        verify(alertsRepository, never())
                .removeArrivalAlert(anyOrNull())
    }

    @Test
    fun onUserConfirmDeletionDoesNotCauseDeletionWhenStopCodeIsEmpty() =
            coroutineRule.runBlockingTest {
        viewModel.stopCode = ""

        viewModel.onUserConfirmDeletion()

        verify(alertsRepository, never())
                .removeArrivalAlert(anyOrNull())
    }

    @Test
    fun onUserConfirmDeletionCausesDeletionWhenStopCodeIsPopulated() =
            coroutineRule.runBlockingTest {
        viewModel.stopCode = "123456"

        viewModel.onUserConfirmDeletion()

        verify(alertsRepository)
                .removeArrivalAlert(anyOrNull())
    }
}