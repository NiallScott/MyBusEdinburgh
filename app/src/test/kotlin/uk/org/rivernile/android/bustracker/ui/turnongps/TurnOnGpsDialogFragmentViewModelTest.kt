/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.turnongps

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [TurnOnGpsDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TurnOnGpsDialogFragmentViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    private lateinit var viewModel: TurnOnGpsDialogFragmentViewModel

    @Before
    fun setUp() {
        viewModel = TurnOnGpsDialogFragmentViewModel(
            preferenceRepository,
            coroutineRule.scope,
            coroutineRule.testDispatcher)
    }

    @Test
    fun onDoNotRemindCheckChangedSendsThroughValueToPreferenceRepositoryWhenFalse() = runTest {
        viewModel.onDoNotRemindCheckChanged(false)
        advanceUntilIdle()

        verify(preferenceRepository)
            .setIsGpsPromptDisabled(false)
    }

    @Test
    fun onDoNotRemindCheckChangedSendsThroughValueToPreferenceRepositoryWhenTrue() = runTest {
        viewModel.onDoNotRemindCheckChanged(true)
        advanceUntilIdle()

        verify(preferenceRepository)
            .setIsGpsPromptDisabled(true)
    }

    @Test
    fun onYesClickedShowsSystemLocationSettings() {
        val observer = viewModel.showSystemLocationSettingsLiveData.test()

        viewModel.onYesClicked()

        observer.assertSize(1)
    }
}