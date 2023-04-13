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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
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
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        /**
         * State key for the stop code.
         */
        const val STATE_STOP_CODE = Arguments.STATE_STOP_CODE
    }

    /**
     * This [LiveData] exposes whether the device currently has connectivity or not. This will emit
     * distinct values.
     */
    val hasConnectivityLiveData = connectivityRepository.hasInternetConnectivityFlow
        .distinctUntilChanged()
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
     * This exposes the stop code as a [LiveData]. It is distinct as it only delivers stop code
     * changes when an actual change occurs. If an update is made to the stop code that is identical
     * to the previously held code, this won't be delivered in this [LiveData].
     */
    private val distinctStopCodeLiveData = arguments
        .stopCodeFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    /**
     * This [LiveData] exposes whether the live times are currently sorted by time or by service
     * name. This is based on the user preference from [PreferenceRepository]. This will emit
     * distinct values.
     */
    val isSortedByTimeLiveData = preferenceRepository
        .isLiveTimesSortByTimeFlow()
        .distinctUntilChanged()
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether the sorted by time item is enabled or not.
     */
    val isSortedByTimeEnabledLiveData = arguments
        .stopCodeFlow
        .map { !it.isNullOrEmpty() }
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] exposes whether auto refresh is enabled or not. This is based on the user
     * preference from [PreferenceRepository]. This will emit distinct values.
     */
    val isAutoRefreshLiveData = preferenceRepository
        .isLiveTimesAutoRefreshEnabledFlow()
        .distinctUntilChanged()
        .onEach {
            refreshController.onAutoRefreshPreferenceChanged(liveTimes.value, it)
        }
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether the auto refresh item is enabled or not.
     */
    val isAutoRefreshEnabledLiveData = arguments
        .stopCodeFlow
        .map { !it.isNullOrEmpty() }
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This is the [LiveData] which contains the result from loading live times. This uses the
     * [liveData] builder, with a timeout set to [Long.MAX_VALUE] so that a reload does not happen
     * on configuration change, but has the [viewModelScope] applied so the live times [Flow] is
     * cancelled when the [ViewModel] is cleared.
     */
    private val liveTimes = liveData(
            viewModelScope.coroutineContext + defaultDispatcher,
            Long.MAX_VALUE) {
        liveTimesFlow.collect {
            emit(it)
        }
    }

    /**
     * Show loading progress to the user. If there is no stop code, this will emit `null`.
     * Otherwise, it will emit the progress state of the loading live times.
     */
    val showProgressLiveData = distinctStopCodeLiveData.switchMap { stopCode ->
        if (!stopCode.isNullOrEmpty()) {
            liveTimes.map {
                it is UiTransformedResult.InProgress
            }
        } else {
            MutableLiveData<Boolean>(null)
        }
    }

    /**
     * This contains the last successful load of the live times. This data populates the live times
     * list the user sees. We also hold the last successful load to calculate the UI state, because
     * after the first successful load, we never return to the progress state, and we only go to the
     * error state when the live times is empty.
     */
    private val lastSuccess = MediatorLiveData<UiTransformedResult.Success>().apply {
        // We don't use LiveData.map() here because we only wish to apply the transformation when
        // the result is UiTransformedResult.Success, otherwise the value is left untouched.
        addSource(liveTimes) {
            if (it is UiTransformedResult.Success) {
                value = it
            }
        }
    }

    /**
     * This [LiveData] exposes errors which occur when loading live times. Additionally, if the
     * state is successful but there is no data, this will generate an [ErrorType.NO_DATA].
     */
    val errorLiveData: LiveData<ErrorType?> = MediatorLiveData<ErrorType?>().apply {
        // We don't use LiveData.map() here because we don't wish to apply any new state when
        // liveTimes is UiTransformedResult.InProgress - the last value should remain in this case.
        addSource(liveTimes) {
            when (it) {
                is UiTransformedResult.Success -> {
                    value = if (it.items.isEmpty()) {
                        ErrorType.NO_DATA
                    } else {
                        null
                    }
                }
                is UiTransformedResult.Error -> value = it.error
                else -> { /* Suppress exhaustive warning. */ }
            }
        }
    }

    /**
     * This [LiveData] exposes the live times to be shown in the UI. It contains the last
     * successfully loaded data, but only if it's non-empty. Otherwise, it will yield `null`.
     */
    val liveTimesLiveData = lastSuccess.map {
        it.items.ifEmpty {
            null
        }
    }

    /**
     * This [LiveData] exposes the calculated top-level [UiState]. This will emit distinct values.
     *
     * - [UiState.PROGRESS]: this is only shown when the screen is first shown (other states are
     * unpopulated). After the next state ([UiState.CONTENT] or [UiState.ERROR]), this is never
     * shown again.
     * - [UiState.CONTENT]: this is always shown when [liveTimesLiveData] contains non-`null` live
     * times after the initial first [UiState.PROGRESS] state.
     * - [UiState.ERROR]: this is only shown after the initial [UiState.PROGRESS] when
     * [liveTimesLiveData] is `null`.
     */
    val uiStateLiveData: LiveData<UiState> = MediatorLiveData<UiState>().apply {
        // showProgressLiveData is listened to so that we recalculate state when the progress state
        // changes.
        addSource(showProgressLiveData) {
            calculateUiState(liveTimesLiveData.value, errorLiveData.value, it)?.let { s ->
                value = s
            }
        }
        addSource(liveTimesLiveData) {
            calculateUiState(it, errorLiveData.value, showProgressLiveData.value)?.let { s ->
                value = s
            }
        }
        addSource(errorLiveData) {
            calculateUiState(liveTimesLiveData.value, it, showProgressLiveData.value)?.let { s ->
                value = s
            }
        }
    }.distinctUntilChanged()

    /**
     * This [LiveData] exposes errors when there is previous content loaded which should be
     * currently shown, but a new error has occured. Instead, a special type of object is emitted,
     * an [Event], which can only be consumed once. This is because in the content loaded
     * scenario, errors should be shown as snackbars rather than in-line UI, so that the user is
     * still able to see the previously loaded live times whilst being shown the new error.
     */
    val errorWithContentLiveData = errorLiveData.map { error ->
        if (liveTimesLiveData.value?.isNotEmpty() == true) {
            error?.let {
                Event(it)
            }
        } else {
            null
        }
    }

    /**
     * This [LiveData] emits last-refresh updates, to inform the user when the data was last
     * successfully loaded. Once a successful load takes place, the last refresh times is emitted on
     * a continual basis. This emits distinct values.
     */
    val lastRefreshLiveData = lastSuccess.switchMap {
        lastRefreshTimeCalculator.getLastRefreshTimeFlow(it.receiveTime)
                .distinctUntilChanged()
                .asLiveData(viewModelScope.coroutineContext + defaultDispatcher)
    }

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
        preferenceRepository.toggleSortByTime()
    }

    /**
     * This is called when the auto refresh menu item has been clicked by the user.
     */
    fun onAutoRefreshMenuItemClicked() {
        preferenceRepository.toggleAutoRefresh()
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
     * Create a [Flow] which produces the [UiTransformedResult], containing states of either
     * progress, error or success.
     *
     * @return A [Flow] which produces live times.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val liveTimesFlow: Flow<UiTransformedResult> get() =
        liveTimesLoader.liveTimesFlow
            .transformLatest {
                emit(it) // Emit first so that the value is immediately available.
                refreshController.performAutoRefreshDelay(it) {
                    isAutoRefreshLiveData.value == true
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
            isInProgress: Boolean?): UiState? {
        return items?.ifEmpty { null }?.let {
            UiState.CONTENT
        } ?: error?.let {
            UiState.ERROR
        } ?: if (isInProgress == true) UiState.PROGRESS else null
    }
}