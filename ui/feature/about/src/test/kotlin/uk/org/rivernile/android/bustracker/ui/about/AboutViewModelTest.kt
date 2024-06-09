/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.about

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [AboutViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AboutViewModelTest {

    @Mock
    private lateinit var state: AboutViewModelState
    @Mock
    private lateinit var aboutItemsGenerator: AboutItemsGenerator

    @Test
    fun uiStateFlowEmitsInitialState() = runTest {
        val expectedItems = listOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        val expectedUiState = UiState(items = expectedItems)
        whenever(aboutItemsGenerator.createAboutItems())
            .thenReturn(expectedItems)
        whenever(aboutItemsGenerator.aboutItemsFlow)
            .thenReturn(emptyFlow())
        whenever(state.isCreditsShownFlow)
            .thenReturn(emptyFlow())
        whenever(state.isOpenSourceLicencesShownFlow)
            .thenReturn(emptyFlow())
        whenever(state.actionFlow)
            .thenReturn(emptyFlow())
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.uiStateFlow.test {
            assertEquals(expectedUiState, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsItemsFromAboutItemsGenerator() = runTest {
        val expectedItems1 = listOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        val expectedItems2 = listOf(
            UiAboutItem.OneLineItem.PrivacyPolicy,
            UiAboutItem.TwoLinesItem.Twitter
        )
        val expectedUiState1 = UiState(items = expectedItems1)
        val expectedUiState2 = UiState(items = expectedItems2)
        whenever(aboutItemsGenerator.createAboutItems())
            .thenReturn(expectedItems1)
        whenever(aboutItemsGenerator.aboutItemsFlow)
            .thenReturn(intervalFlowOf(1L, 0L, expectedItems2))
        whenever(state.isCreditsShownFlow)
            .thenReturn(flowOf(false))
        whenever(state.isOpenSourceLicencesShownFlow)
            .thenReturn(flowOf(false))
        whenever(state.actionFlow)
            .thenReturn(flowOf(null))
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.uiStateFlow.test {
            assertEquals(expectedUiState1, awaitItem())
            assertEquals(expectedUiState2, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsIsCreditsShownValues() = runTest {
        val expectedItems = listOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        whenever(aboutItemsGenerator.createAboutItems())
            .thenReturn(expectedItems)
        whenever(aboutItemsGenerator.aboutItemsFlow)
            .thenReturn(flowOf(expectedItems))
        whenever(state.isCreditsShownFlow)
            .thenReturn(intervalFlowOf(0L, 1L, true, false, true))
        whenever(state.isOpenSourceLicencesShownFlow)
            .thenReturn(flowOf(false))
        whenever(state.actionFlow)
            .thenReturn(flowOf(null))
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    items = expectedItems,
                    isCreditsShown = true,
                ),
                awaitItem()
            )

            assertEquals(
                UiState(
                    items = expectedItems,
                    isCreditsShown = false,
                ),
                awaitItem()
            )

            assertEquals(
                UiState(
                    items = expectedItems,
                    isCreditsShown = true,
                ),
                awaitItem()
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsIsOpenSourceLicencesShownValues() = runTest {
        val expectedItems = listOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        whenever(aboutItemsGenerator.createAboutItems())
            .thenReturn(expectedItems)
        whenever(aboutItemsGenerator.aboutItemsFlow)
            .thenReturn(flowOf(expectedItems))
        whenever(state.isCreditsShownFlow)
            .thenReturn(flowOf(false))
        whenever(state.isOpenSourceLicencesShownFlow)
            .thenReturn(intervalFlowOf(0L, 1L, true, false, true))
        whenever(state.actionFlow)
            .thenReturn(flowOf(null))
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    items = expectedItems,
                    isOpenSourceLicencesShown = true,
                ),
                awaitItem()
            )

            assertEquals(
                UiState(
                    items = expectedItems,
                    isOpenSourceLicencesShown = false,
                ),
                awaitItem()
            )

            assertEquals(
                UiState(
                    items = expectedItems,
                    isOpenSourceLicencesShown = true,
                ),
                awaitItem()
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsActionValues() = runTest {
        val expectedItems = listOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        whenever(aboutItemsGenerator.createAboutItems())
            .thenReturn(expectedItems)
        whenever(aboutItemsGenerator.aboutItemsFlow)
            .thenReturn(flowOf(expectedItems))
        whenever(state.isCreditsShownFlow)
            .thenReturn(flowOf(false))
        whenever(state.isOpenSourceLicencesShownFlow)
            .thenReturn(flowOf(false))
        whenever(state.actionFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    1L,
                    UiAction.ShowPrivacyPolicy,
                    null,
                    UiAction.ShowAppTwitter,
                    null
                )
            )
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    items = expectedItems,
                    action = UiAction.ShowPrivacyPolicy
                ),
                awaitItem()
            )

            assertEquals(
                UiState(
                    items = expectedItems,
                    action = null
                ),
                awaitItem()
            )

            assertEquals(
                UiState(
                    items = expectedItems,
                    action = UiAction.ShowAppTwitter
                ),
                awaitItem()
            )

            assertEquals(
                UiState(
                    items = expectedItems,
                    action = null
                ),
                awaitItem()
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onItemClickedWhenItemIsCreditsSetsCreditsShownToTrue() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onItemClicked(UiAboutItem.OneLineItem.Credits)

        verify(state)
            .isCreditsShown = true
    }

    @Test
    fun onItemClickedWhenItemIsPrivacyPolicySetsActionToShowPrivacyPolicy() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onItemClicked(UiAboutItem.OneLineItem.PrivacyPolicy)

        verify(state)
            .action = UiAction.ShowPrivacyPolicy
    }

    @Test
    fun onItemClickedWhenItemIsOpenSourceLicencesSetsShowOpenSourceLicencesToTrue() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onItemClicked(UiAboutItem.OneLineItem.OpenSourceLicences)

        verify(state)
            .isOpenSourceLicencesShown = true
    }

    @Test
    fun onItemClickedWhenItemIsAppVersionSetsActionToShowStoreListing() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onItemClicked(
            UiAboutItem.TwoLinesItem.AppVersion(
                versionName = "1.2.3",
                versionCode = 4
            )
        )

        verify(state)
            .action = UiAction.ShowStoreListing
    }

    @Test
    fun onItemClickedWhenItemIsAuthorSetsActionToShowAuthorWebsite() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Author)

        verify(state)
            .action = UiAction.ShowAuthorWebsite
    }

    @Test
    fun onItemClickedWhenItemIsTwitterSetsActionToShowAppTwitter() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Twitter)

        verify(state)
            .action = UiAction.ShowAppTwitter
    }

    @Test
    fun onItemClickedWhenItemIsWebsiteSetsActionToShowAppWebsite() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Website)

        verify(state)
            .action = UiAction.ShowAppWebsite
    }

    @Test
    fun onCreditsDialogDismissedSetsIsCreditsShownToFalse() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onCreditsDialogDismissed()

        verify(state)
            .isCreditsShown = false
    }

    @Test
    fun onOpenSourceDialogDismissedSetsIsOpenSourceLicencesShownToFalse() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onOpenSourceDialogDismissed()

        verify(state)
            .isOpenSourceLicencesShown = false
    }

    @Test
    fun onActionLaunchedSetsActionToNull() = runTest {
        val viewModel = createViewModel(
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelScope = backgroundScope
        )

        viewModel.onActionLaunched()

        verify(state)
            .action = null
    }

    private fun createViewModel(
        defaultDispatcher: CoroutineDispatcher,
        viewModelScope: CoroutineScope
    ): AboutViewModel {
        return AboutViewModel(
            state,
            aboutItemsGenerator,
            defaultDispatcher,
            viewModelScope
        )
    }
}