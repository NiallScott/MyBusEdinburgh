/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.preferences.AppTheme
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [SettingsFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SettingsFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val liveDataTaskExecutor = InstantTaskExecutorRule()

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    @Test
    fun appThemeLiveDataEmitsAppThemValueFromPreferenceRepository() = runTest {
        whenever(preferenceRepository.appThemeFlow)
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                AppTheme.SYSTEM_DEFAULT,
                AppTheme.DARK,
                AppTheme.LIGHT))
        val viewModel = createViewModel()

        val observer = viewModel.appThemeLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            AppTheme.SYSTEM_DEFAULT,
            AppTheme.DARK,
            AppTheme.LIGHT)
    }

    @Test
    fun numberOfDeparturesPerServiceLiveDataEmitsValuesFromPreferenceRepository() = runTest {
        whenever(preferenceRepository.liveTimesNumberOfDeparturesFlow)
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                1,
                2,
                3,
                4))
        val viewModel = createViewModel()

        val observer = viewModel.numberOfDeparturesPerServiceLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            1,
            2,
            3,
            4)
    }

    @Test
    fun onClearSearchHistoryClickedCausesShowClearSearchHistory() {
        val viewModel = createViewModel()
        val observer = viewModel.showClearSearchHistoryLiveData.test()

        viewModel.onClearSearchHistoryClicked()

        observer.assertSize(1)
    }

    private fun createViewModel() =
        SettingsFragmentViewModel(
            preferenceRepository,
            coroutineRule.testDispatcher)
}