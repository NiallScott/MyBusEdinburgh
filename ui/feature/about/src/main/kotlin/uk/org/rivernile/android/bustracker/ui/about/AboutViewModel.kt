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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import javax.inject.Inject

/**
 * This is the [ViewModel] for the 'about' screen.
 *
 * @param state The state held for this instance.
 * @param aboutItemsGenerator Used to generate the [UiAboutItem]s.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @param viewModelCoroutineScope The [ViewModel] [CoroutineScope] to use.
 * @author Niall Scott
 */
@HiltViewModel
internal class AboutViewModel @Inject constructor(
    private val state: AboutViewModelState,
    private val aboutItemsGenerator: AboutItemsGenerator,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    /**
     * This [kotlinx.coroutines.flow.StateFlow] emits the latest [UiState].
     */
    val uiStateFlow = combinedUiStateFlow
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState(items = aboutItemsGenerator.createAboutItems())
        )

    /**
     * This is called when an item has been clicked.
     *
     * @param item The item which was clicked.
     */
    fun onItemClicked(item: UiAboutItem) {
        when (item) {
            is UiAboutItem.OneLineItem.Credits -> state.isCreditsShown = true
            is UiAboutItem.OneLineItem.PrivacyPolicy ->
                state.action = UiAction.ShowPrivacyPolicy
            is UiAboutItem.OneLineItem.OpenSourceLicences -> state.isOpenSourceLicencesShown = true
            is UiAboutItem.TwoLinesItem.AppVersion -> state.action = UiAction.ShowStoreListing
            is UiAboutItem.TwoLinesItem.Author -> state.action = UiAction.ShowAuthorWebsite
            is UiAboutItem.TwoLinesItem.Twitter -> state.action = UiAction.ShowAppTwitter
            is UiAboutItem.TwoLinesItem.Website -> state.action = UiAction.ShowAppWebsite
            else -> Unit
        }
    }

    /**
     * This is called when the credits dialog has been dismissed.
     */
    fun onCreditsDialogDismissed() {
        state.isCreditsShown = false
    }

    /**
     * This is called when the open source dialog has been dismissed.
     */
    fun onOpenSourceDialogDismissed() {
        state.isOpenSourceLicencesShown = false
    }

    /**
     * This is called when the [UiAction] has been launched.
     */
    fun onActionLaunched() {
        state.action = null
    }

    private val combinedUiStateFlow get() = combine(
        aboutItemsGenerator.aboutItemsFlow,
        state.isCreditsShownFlow,
        state.isOpenSourceLicencesShownFlow,
        state.actionFlow
    ) { items, isCreditsShown, isOpenSourceLicencesShown, action ->
        UiState(
            items = items,
            isCreditsShown = isCreditsShown,
            isOpenSourceLicencesShown = isOpenSourceLicencesShown,
            action = action
        )
    }
}