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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.utils.Event
import javax.inject.Inject

/**
 * This is the [ViewModel] for [BusTimesFragment].
 *
 * @param arguments The arguments for this instance.
 * @param expandedServicesTracker This implementation tracks the expanded/collapse state of the
 * services, for the purpose of showing the user services in the style of an expandable list.
 * @param liveTimesLoader Used to load live times.
 * @param lastRefreshTimeCalculator This is used to calculate the amount of time since the last
 * refresh on a continual basis for the purpose of showing this to the user.
 * @param refreshController This is used to control refreshing data.
 * @param preferenceRepository This contains the user's preferences.
 * @param connectivityRepository This informs us about the device's connectivity status. This status
 * is shown to the user.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher Computation is run on this [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class BusTimesFragmentViewModel @Inject constructor(
    arguments: Arguments,
    private val expandedServicesTracker: ExpandedServicesTracker,
    private val liveTimesLoader: LiveTimesLoader,
    private val lastRefreshTimeCalculator: LastRefreshTimeCalculator,
    private val refreshController: RefreshController,
    private val preferenceRepository: PreferenceRepository,
    connectivityRepository: ConnectivityRepository,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        /**
         * State key for the stop code.
         */
        const val STATE_STOP_CODE = Arguments.STATE_STOP_CODE
    }

    /**
     * A [Flow] which emits whether the stop code is valid.
     */
    private val isValidStopCodeFlow = arguments
        .stopCodeFlow
        .map(this::isValidStopCode)
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * A [Flow] which produces the [UiTransformedResult], containing states of either progress,
     * error or success.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val liveTimesFlow = liveTimesLoader
        .liveTimesFlow
        .transformLatest {
            emit(it) // Emit first so that the value is immediately available.
            refreshController.performAutoRefreshDelay(it)
        }
        .flowOn(defaultDispatcher)
        // TODO: this is started with SharingStarted.Lazily. We don't want to keep doing this going
        // forwards. This should be fixed with the refresh refactor.
        .stateIn(viewModelScope, SharingStarted.Lazily, UiTransformedResult.InProgress)

    /**
     * This emits the last successful load of the live times. This data populates the live times
     * list the user sees. We also hold the last successful load to calculate the UI state, because
     * after the first successful load, we never return directly to the progress state, and we only
     * go to the error state when the live times is empty (which can land us back on the progress
     * state).
     */
    private val lastSuccessFlow = liveTimesFlow
        .filterIsInstance<UiTransformedResult.Success>()
        .onStart<UiTransformedResult.Success?> { emit(null) }
        .flowOn(defaultDispatcher)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This emits the last error while loading live times.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val errorFlow = liveTimesFlow
        .transformLatest {
            when (it) {
                is UiTransformedResult.Success -> {
                    if (it.items.isEmpty()) {
                        emit(ErrorType.NO_DATA)
                    } else {
                        emit(null)
                    }
                }
                is UiTransformedResult.Error -> emit(it.error)
                else -> { /* Suppress exhaustive warning. */ }
            }
        }
        .flowOn(defaultDispatcher)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This [LiveData] exposes whether the device currently has connectivity or not. This will emit
     * distinct values.
     */
    val hasConnectivityLiveData = connectivityRepository
        .hasInternetConnectivityFlow
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] is used not to provide any data, but as a convenient way to know when the
     * lifecycle is in an active state, to control the dispatching of refresh requests. It should
     * really have been implemented with a proper lifecycle object, but [LiveData] already does
     * this, and correctly - so therefore we use [LiveData] instead.
     *
     * The UI should register against this [LiveData], but not expect any data to be emitted from
     * it.
     */
    val refreshLiveData: LiveData<Unit> get() = refresh
    private val refresh = RefreshLiveData(refreshController)

    /**
     * This [LiveData] exposes whether the live times are currently sorted by time or by service
     * name. This is based on the user preference from [PreferenceRepository]. This will emit
     * distinct values.
     */
    val isSortedByTimeLiveData = preferenceRepository
        .isLiveTimesSortByTimeFlow
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether the sorted by time item is enabled or not.
     */
    val isSortedByTimeEnabledLiveData = isValidStopCodeFlow
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] exposes whether auto refresh is enabled or not. This is based on the user
     * preference from [PreferenceRepository]. This will emit distinct values.
     */
    val isAutoRefreshLiveData = preferenceRepository
        .isLiveTimesAutoRefreshEnabledFlow
        .distinctUntilChanged()
        .onEach {
            refreshController.onAutoRefreshPreferenceChanged(liveTimesFlow.value, it)
        }
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether the auto refresh item is enabled or not.
     */
    val isAutoRefreshEnabledLiveData = isValidStopCodeFlow
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether the swipe refresh UI component is enabled.
     */
    val isSwipeRefreshEnabledLiveData = isValidStopCodeFlow
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether progress is enabled or not.
     */
    val isProgressMenuItemEnabledLiveData = arguments
        .stopCodeFlow
        .combine(liveTimesFlow, this::calculateIsProgressEnabled)
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [Flow] emits whether progress is visible or not.
     */
    private val isProgressVisibleFlow = liveTimesFlow
        .map { it is UiTransformedResult.InProgress }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This [LiveData] emits whether progress is currently visible.
     */
    val isProgressVisibleLiveData = isProgressVisibleFlow
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] exposes errors which occur when loading live times. Additionally, if the
     * state is successful but there is no data, this will generate an [ErrorType.NO_DATA].
     */
    val errorLiveData = errorFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This [Flow] emits the [List] of [UiLiveTimesItem]s to be displayed.
     */
    private val liveTimesListFlow = lastSuccessFlow
        .map { it?.items?.ifEmpty { null } }
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits the live times to be shown in the UI. It contains the last successfully
     * loaded data, but only if it's non-empty. Otherwise, it will emit `null`.
     */
    val liveTimesListLiveData = liveTimesListFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    /**
     * This [Flow] emits and holds the state for the current [UiState].
     */
    private val uiStateFlow = combine(
        liveTimesListFlow,
        errorFlow,
        isProgressVisibleFlow,
        this::calculateUiState)
        .filterNotNull()
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.PROGRESS)

    /**
     * This [LiveData] exposes the calculated top-level [UiState]. This will emit distinct values.
     *
     * - [UiState.PROGRESS]: this is only shown when the screen is first shown (other states are
     * unpopulated). After the next state ([UiState.CONTENT] or [UiState.ERROR]), this is never
     * shown again.
     * - [UiState.CONTENT]: this is always shown when [liveTimesListLiveData] contains non-`null`
     * live times after the initial first [UiState.PROGRESS] state.
     * - [UiState.ERROR]: this is only shown after the initial [UiState.PROGRESS] when
     * [liveTimesListLiveData] is `null`.
     */
    val uiStateLiveData = uiStateFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    /**
     * This [LiveData] exposes errors when there is previous content loaded which should be
     * currently shown, but a new error has occurred. Instead, a special type of object is emitted,
     * an [Event], which can only be consumed once. This is because in the content loaded
     * scenario, errors should be shown as snackbars rather than in-line UI, so that the user is
     * still able to see the previously loaded live times whilst being shown the new error.
     */
    val errorWithContentLiveData = errorFlow
        .map { error ->
            if (uiStateFlow.value == UiState.CONTENT) {
                error?.let {
                    Event(it)
                }
            } else {
                null
            }
        }
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits last-refresh updates, to inform the user when the data was last
     * successfully loaded. Once a successful load takes place, the last refresh times is emitted on
     * a continual basis. This emits distinct values.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val lastRefreshLiveData = lastSuccessFlow
        .map { it?.receiveTime ?: -1L }
        .flatMapLatest(lastRefreshTimeCalculator::getLastRefreshTimeFlow)
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This is called when the refresh menu item has been clicked by the user.
     */
    fun onRefreshMenuItemClicked() {
        refreshLiveTimes()
    }

    /**
     * This is called when the sort menu item has been clicked by the user.
     */
    fun onSortMenuItemClicked() {
        applicationCoroutineScope.launch(defaultDispatcher) {
            preferenceRepository.toggleSortByTime()
        }
    }

    /**
     * This is called when the auto refresh menu item has been clicked by the user.
     */
    fun onAutoRefreshMenuItemClicked() {
        applicationCoroutineScope.launch(defaultDispatcher) {
            preferenceRepository.toggleAutoRefresh()
        }
    }

    /**
     * This is called when the user performs the swipe to refresh action.
     */
    fun onSwipeToRefresh() {
        refreshLiveTimes()
    }

    /**
     * This is called when a parent item in the live times list has been clicked.
     *
     * @param serviceName The name of the service represented by the parent item.
     */
    fun onParentItemClicked(serviceName: String) {
        expandedServicesTracker.onServiceClicked(serviceName)
    }

    /**
     * This causes the live times to be refreshed from the endpoint.
     */
    private fun refreshLiveTimes() {
        viewModelScope.launch {
            refreshController.requestRefresh()
        }
    }

    /**
     * Given the currently displayed [items] and any [error], calculate the current top level state
     * of the UI.
     *
     * Progress is only shown when both the items and the error are unpopulated, to prevent jarring
     * progress UI.
     *
     * @param items The currently displayed items. If there is a non-empty list of items being
     * displayed, then the success state will be continue to be shown even if there is a new error.
     * This is because errors are shown differently in this state.
     * @param error The currently set error. This only matters if there is no success state
     * currently being shown.
     * @param isInProgress The current progress state.
     * @return The newly calculated [UiState]. If `null` is returned, don't perform a state
     * transition.
     */
    private fun calculateUiState(
        items: List<UiLiveTimesItem>?,
        error: ErrorType?,
        isInProgress: Boolean): UiState? {
        return when {
            !items.isNullOrEmpty() -> UiState.CONTENT
            error != null -> UiState.ERROR
            isInProgress -> UiState.PROGRESS
            else -> null
        }
    }

    /**
     * Calculate whether progress is enabled.
     *
     * @param stopCode The currently set stop code.
     * @param result The current [UiTransformedResult].
     * @return `true` when the stop code is valid and we're not currently in progress, otherwise
     * `false`.
     */
    private fun calculateIsProgressEnabled(stopCode: String?, result: UiTransformedResult) =
        isValidStopCode(stopCode) && result !is UiTransformedResult.InProgress

    /**
     * Is the stop code valid?
     *
     * @param stopCode The stop code to test.
     * @return `true` if the stop code is valid, otherwise `false`.
     */
    private fun isValidStopCode(stopCode: String?) = !stopCode.isNullOrEmpty()
}