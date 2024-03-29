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

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is a [ViewModel] for use with [SettingsFragment].
 *
 * @author Niall Scott
 */
@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    preferenceRepository: PreferenceRepository,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher): ViewModel() {

    /**
     * This [LiveData] emits the current app theme.
     */
    val appThemeLiveData = preferenceRepository
        .appThemeFlow
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the number of departures to show per service.
     */
    val numberOfDeparturesPerServiceLiveData = preferenceRepository
        .liveTimesNumberOfDeparturesFlow
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This property contains the [LiveData] object which is invoked when the user should be
     * promoted to confirm clearing their search history.
     */
    val showClearSearchHistoryLiveData: LiveData<Unit> get() = showClearSearchHistory
    private val showClearSearchHistory = SingleLiveEvent<Unit>()

    /**
     * This is called when the user has clicked on the 'Clear search history' preference item.
     */
    fun onClearSearchHistoryClicked() {
        showClearSearchHistory.call()
    }
}