/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.livedata.DistinctLiveData
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.utils.Event

/**
 * This is the [ViewModel] for [BusTimesFragment].
 *
 * @param expandedServicesTracker This implementation tracks the expanded/collapse state of the
 * services, for the purpose of showing the user services in the style of an expandable list.
 * @param liveTimesLoader This is used to load live times.
 * @param liveTimesTransform This is used to transform the live times in to a form consumable by the
 * UI.
 * @param lastRefreshTimeCalculator This is used to calculate the amount of time since the last
 * refresh on a continual basis for the purpose of showing this to the user.
 * @param preferenceRepository This contains the user's preferences.
 * @param connectivityRepository This informs us about the device's connectivity status. This status
 * is shown to the user.
 * @param defaultDispatcher Computation is run on this [CoroutineDispatcher].
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class BusTimesFragmentViewModel(
        private val expandedServicesTracker: ExpandedServicesTracker,
        private val liveTimesLoader: LiveTimesLoader,
        private val liveTimesTransform: LiveTimesTransform,
        private val lastRefreshTimeCalculator: LastRefreshTimeCalculator,
        private val preferenceRepository: PreferenceRepository,
        connectivityRepository: ConnectivityRepository,
        @ForDefaultDispatcher defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    /**
     * This [LiveData] exposes whether the device currently has connectivity or not.
     */
    val hasConnectivityLiveData = connectivityRepository.hasInternetConnectivityFlow().asLiveData()

    /**
     * This exposes the stop code as a [LiveData]. It is distinct as it only delivers stop code
     * changes when an actual change occurs. If an update is made to the stop code that is identical
     * to the previously held code, this won't be delivered in this [LiveData].
     */
    private val distinctStopCodeLiveData = DistinctLiveData<String?>()

    /**
     * This [LiveData] exposes whether the live times are currently sorted by time or by service
     * name. This is based on the user preference from [PreferenceRepository].
     *
     * If there is no set stop code, this will return `null`.
     */
    val isSortedByTimeLiveData = distinctStopCodeLiveData.switchMap {
        if (it?.isNotEmpty() == true) {
            preferenceRepository.isLiveTimesSortByTimeFlow().asLiveData()
        } else {
            MutableLiveData<Boolean>(null)
        }
    }

    /**
     * This is used as the refresh trigger, which causes the upstream [Flow]s to load new live times
     * from the endpoint. This is exposed as a [LiveData] for ease.
     */
    private val refreshTrigger = MutableLiveData(Unit)

    /**
     * This is the [LiveData] which contains the result from loading live times.
     */
    private val liveTimes = createLiveTimesFlow()
            .asLiveData(context = viewModelScope.coroutineContext + defaultDispatcher)

    /**
     * Show loading progress to the user. If there is no stop code, this will emit `null`.
     * Otherwise, it will emit the progress state of the loading live times.
     */
    val showProgressLiveData = distinctStopCodeLiveData.switchMap { stopCode ->
        if (stopCode?.isNotEmpty() == true) {
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
     * after the first successful load, we never return to the progress or error UI states - the
     * last successful load is shown and progress and errors are shown in different ways.
     */
    private val lastSuccess = MediatorLiveData<UiTransformedResult.Success>().apply {
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
            }
        }
    }

    /**
     * This [LiveData] exposes the live times to be shown in the UI. It contains the last
     * successfully loaded data, but only if it's non-empty. Otherwise, it will yield `null`.
     */
    val liveTimesLiveData = lastSuccess.map {
        if (it.items.isNotEmpty()) {
            it.items
        } else {
            null
        }
    }

    /**
     * This [LiveData] exposes the calculated top-level [UiState].
     */
    val uiStateLiveState: LiveData<UiState> = MediatorLiveData<UiState>().apply {
        addSource(liveTimesLiveData) {
            value = calculateUiState(it, errorLiveData.value)
        }
        addSource(errorLiveData) {
            value = calculateUiState(liveTimesLiveData.value, it)
        }
    }.distinctUntilChanged()

    /**
     * This [LiveData] exposes errors when there is previous content loaded which should be
     * currently shown. Instead, a special type of object is emitted, an [Event], which can only
     * be consumed once. This is because in the content loaded scenario, errors should be shown as
     * snackbars rather than in-line UI, so that the user is still able to see the previously loaded
     * live times.
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
     * a continual basis.
     */
    val lastRefreshLiveData = lastSuccess.switchMap {
        it?.let {
            lastRefreshTimeCalculator.getLastRefreshTimeFlow(it.receiveTime).asLiveData()
        } ?: MutableLiveData<LastRefreshTime>(LastRefreshTime.Never)
    }

    /**
     * This property is used to get and set the stop code which should be shown.
     */
    var stopCode: String?
        get() = distinctStopCodeLiveData.value
        set (value) {
            distinctStopCodeLiveData.setValue(value)
        }

    /**
     * This is called when the refresh menu item has been clicked by the user.
     */
    fun onRefreshMenuItemClicked() {
        refreshTrigger.value = Unit
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

    }

    /**
     * This is called when the user performs the swipe to refresh action.
     */
    fun onSwipeToRefresh() {
        refreshTrigger.value = Unit
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
     * Create a [Flow] which produces the [UiTransformedResult], containing states of either
     * progress, error or success. This merges together the [Flow]s of loading the live times, and
     * the necessary transformations on the loaded live times.
     *
     * @return A [Flow] which produces live times.
     */
    private fun createLiveTimesFlow(): Flow<UiTransformedResult> {
        val liveTimesFlow = liveTimesLoader.loadLiveTimesFlow(
                distinctStopCodeLiveData.asFlow(),
                refreshTrigger.asFlow())

        return liveTimesTransform.getLiveTimesTransformFlow(
                liveTimesFlow,
                expandedServicesTracker.expandedServicesLiveData.asFlow())
    }

    /**
     * Given the currently displayed [items] and any [error], calculate the current top level state
     * of the UI.
     *
     * @param items The currently displayed items. If there is a non-empty list of items being
     * displayed, then the success state will be continue to be shown even if there is a new error.
     * This is because errors are shown differently in this state.
     * @param error The currently set error. This only matters if there is no success state
     * currently being shown.
     * @return The newly calculated [UiState].
     */
    private fun calculateUiState(
            items: List<UiLiveTimesItem>?,
            error: ErrorType?): UiState {
        return items?.ifEmpty { null }?.let {
            UiState.CONTENT
        } ?: error?.let {
            UiState.ERROR
        } ?: UiState.PROGRESS
    }
}