/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [AboutViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {

    @Test
    fun uiStateFlowEmitsInitialState() = runTest {
        val expectedItems = persistentListOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        val expectedUiState = UiState(items = expectedItems)
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() }
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onCreateAboutItems = { expectedItems },
                onAboutItemsFlow = { emptyFlow() }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(expectedUiState, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsItemsFromAboutItemsGenerator() = runTest {
        val expectedItems1 = persistentListOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        val expectedItems2 = persistentListOf(
            UiAboutItem.OneLineItem.PrivacyPolicy,
            UiAboutItem.TwoLinesItem.Twitter
        )
        val expectedUiState1 = UiState(items = expectedItems1)
        val expectedUiState2 = UiState(items = expectedItems2)
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { flowOf(false) },
                onIsOpenSourceLicencesShownFlow = { flowOf(false) },
                onActionFlow = { flowOf(null) }
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onCreateAboutItems = { expectedItems1 },
                onAboutItemsFlow = { intervalFlowOf(1L, 0L, expectedItems2) }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(expectedUiState1, awaitItem())
            assertEquals(expectedUiState2, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsIsCreditsShownValues() = runTest {
        val expectedItems = persistentListOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { intervalFlowOf(0L, 1L, true, false, true) },
                onIsOpenSourceLicencesShownFlow = { flowOf(false) },
                onActionFlow = { flowOf(null) }
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onCreateAboutItems = { expectedItems },
                onAboutItemsFlow = { flowOf(expectedItems) }
            )
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
        val expectedItems = persistentListOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { flowOf(false) },
                onIsOpenSourceLicencesShownFlow = { intervalFlowOf(0L, 1L, true, false, true) },
                onActionFlow = { flowOf(null) }
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onCreateAboutItems = { expectedItems },
                onAboutItemsFlow = { flowOf(expectedItems) }
            )
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
        val expectedItems = persistentListOf(
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.TwoLinesItem.Website
        )
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { flowOf(false) },
                onIsOpenSourceLicencesShownFlow = { flowOf(false) },
                onActionFlow = {
                    intervalFlowOf(
                        0L,
                        1L,
                        UiAction.ShowPrivacyPolicy,
                        null,
                        UiAction.ShowAppTwitter,
                        null
                    )
                }
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onCreateAboutItems = { expectedItems },
                onAboutItemsFlow = { flowOf(expectedItems) }
            )
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
        val setIsCreditsShownInvocationTracker = SetterInvocationTracker<Boolean>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetIsCreditsShown = setIsCreditsShownInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onItemClicked(UiAboutItem.OneLineItem.Credits)

        assertEquals(listOf(true), setIsCreditsShownInvocationTracker.invocations)
    }

    @Test
    fun onItemClickedWhenItemIsPrivacyPolicySetsActionToShowPrivacyPolicy() = runTest {
        val setActionInvocationTracker = SetterInvocationTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetAction = setActionInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onItemClicked(UiAboutItem.OneLineItem.PrivacyPolicy)

        assertEquals(listOf(UiAction.ShowPrivacyPolicy), setActionInvocationTracker.invocations)
    }

    @Test
    fun onItemClickedWhenItemIsOpenSourceLicencesSetsShowOpenSourceLicencesToTrue() = runTest {
        val setIsOpenSourceLicencesShownInvocationTracker = SetterInvocationTracker<Boolean>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetIsOpenSourceLicencesShown = setIsOpenSourceLicencesShownInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onItemClicked(UiAboutItem.OneLineItem.OpenSourceLicences)

        assertEquals(listOf(true), setIsOpenSourceLicencesShownInvocationTracker.invocations)
    }

    @Test
    fun onItemClickedWhenItemIsAppVersionSetsActionToShowStoreListing() = runTest {
        val setActionInvocationTracker = SetterInvocationTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetAction = setActionInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onItemClicked(
            UiAboutItem.TwoLinesItem.AppVersion(
                versionName = "1.2.3",
                versionCode = 4
            )
        )

        assertEquals(listOf(UiAction.ShowStoreListing), setActionInvocationTracker.invocations)
    }

    @Test
    fun onItemClickedWhenItemIsAuthorSetsActionToShowAuthorWebsite() = runTest {
        val setActionInvocationTracker = SetterInvocationTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetAction = setActionInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Author)

        assertEquals(listOf(UiAction.ShowAuthorWebsite), setActionInvocationTracker.invocations)
    }

    @Test
    fun onItemClickedWhenItemIsTwitterSetsActionToShowAppTwitter() = runTest {
        val setActionInvocationTracker = SetterInvocationTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetAction = setActionInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Twitter)

        assertEquals(listOf(UiAction. ShowAppTwitter), setActionInvocationTracker.invocations)
    }

    @Test
    fun onItemClickedWhenItemIsWebsiteSetsActionToShowAppWebsite() = runTest {
        val setActionInvocationTracker = SetterInvocationTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetAction = setActionInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Website)

        assertEquals(listOf(UiAction.ShowAppWebsite), setActionInvocationTracker.invocations)
    }

    @Test
    fun onCreditsDialogDismissedSetsIsCreditsShownToFalse() = runTest {
        val setIsCreditsShownInvocationTracker = SetterInvocationTracker<Boolean>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetIsCreditsShown = setIsCreditsShownInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onCreditsDialogDismissed()

        assertEquals(listOf(false), setIsCreditsShownInvocationTracker.invocations)
    }

    @Test
    fun onOpenSourceDialogDismissedSetsIsOpenSourceLicencesShownToFalse() = runTest {
        val setIsOpenSourceLicencesShownInvocationTracker = SetterInvocationTracker<Boolean>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetIsOpenSourceLicencesShown = setIsOpenSourceLicencesShownInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onOpenSourceDialogDismissed()

        assertEquals(listOf(false), setIsOpenSourceLicencesShownInvocationTracker.invocations)
    }

    @Test
    fun onActionLaunchedSetsActionToNull() = runTest {
        val setActionInvocationTracker = SetterInvocationTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeAboutViewModelState(
                onIsCreditsShownFlow = { emptyFlow() },
                onIsOpenSourceLicencesShownFlow = { emptyFlow() },
                onActionFlow = { emptyFlow() },
                onSetAction = setActionInvocationTracker
            ),
            aboutItemsGenerator = FakeAboutItemsGenerator(
                onAboutItemsFlow = { emptyFlow() },
                onCreateAboutItems = { emptyList() }
            )
        )

        viewModel.onActionLaunched()

        assertEquals(listOf(null), setActionInvocationTracker.invocations)
    }

    private fun TestScope.createViewModel(
        state: AboutViewModelState = FakeAboutViewModelState(
            onIsCreditsShownFlow = { emptyFlow() },
            onIsOpenSourceLicencesShownFlow = { emptyFlow() },
            onActionFlow = { emptyFlow() }
        ),
        aboutItemsGenerator: AboutItemsGenerator = FakeAboutItemsGenerator(
            onAboutItemsFlow = { emptyFlow() }
        )
    ): AboutViewModel {
        return AboutViewModel(
            state,
            aboutItemsGenerator,
            UnconfinedTestDispatcher(testScheduler),
            backgroundScope
        )
    }

    private class SetterInvocationTracker<T> : (T) -> Unit {

        val invocations get() = _invocations.toList()
        private val _invocations = mutableListOf<T>()

        override fun invoke(p1: T) {
            _invocations += p1
        }
    }
}