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

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.search.SearchHistoryRepository
import javax.inject.Inject

/**
 * This is a [ViewModel] for use with [ClearSearchHistoryDialogFragment].
 *
 * @param searchHistoryRepository Used to access search history data.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher] to dispatch on.
 * @author Niall Scott
 */
@HiltViewModel
class ClearSearchHistoryDialogFragmentViewModel @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher): ViewModel() {

    /**
     * This is called when the user has confirmed they wish to clear their search history.
     */
    fun onUserConfirmClearSearchHistory() {
        applicationCoroutineScope.launch(defaultDispatcher) {
            searchHistoryRepository.clearSearchHistory()
        }
    }
}