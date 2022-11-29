/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [SettingsFragmentViewModel].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class SettingsFragmentViewModelTest {

    @get:Rule
    val liveDataTaskExecutor = InstantTaskExecutorRule()

    @Mock
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var viewModel: SettingsFragmentViewModel

    @Before
    fun setUp() {
        viewModel = SettingsFragmentViewModel(preferenceManager)
    }

    @Test
    fun getNumberOfDeparturesPerServiceReturnsValueFromPreferenceManager() {
        whenever(preferenceManager.getBusTimesNumberOfDeparturesToShowPerService())
                .thenReturn(4)

        val result = viewModel.numberOfDeparturesPerService

        assertEquals(4, result)
    }

    @Test
    fun onClearSearchHistoryClickedCausesShowClearSearchHistory() {
        val observer = viewModel.showClearSearchHistoryLiveData.test()

        viewModel.onClearSearchHistoryClicked()

        observer.assertSize(1)
    }
}