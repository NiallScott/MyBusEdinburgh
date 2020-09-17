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
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.livedata.DistinctLiveData
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
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
 * @param timeUtils An implementation to provide timestamps.
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
        private val timeUtils: TimeUtils,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        private const val AUTO_REFRESH_INTERVAL_MILLIS = 60000L
    }

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
     * This [LiveData] exposes whether auto refresh is enabled or not. This is based on the user
     * preference from [PreferenceRepository].
     *
     * If there is no set stop code, this will return `null`.
     */
    val isAutoRefreshLiveData = distinctStopCodeLiveData.switchMap {
        if (it?.isNotEmpty() == true) {
            preferenceRepository.isLiveTimesAutoRefreshEnabledFlow()
                    .onEach { enabled ->
                        handleAutoRefreshPreferenceChanged(enabled)
                    }
                    .asLiveData(viewModelScope.coroutineContext)
        } else {
            MutableLiveData<Boolean>(null)
        }
    }

    /**
     * This is used as the refresh trigger, which causes the upstream [Flow]s to load new live times
     * from the endpoint.
     */
    private val refreshTriggerChannel = Channel<Unit>(Channel.CONFLATED).apply {
        offer(Unit)
    }

    /**
     * This is the [LiveData] which contains the result from loading live times. This does not use
     * a [liveData] builder as this would terminate the live times [Flow] when observers are removed
     * from the [LiveData]. Instead, it launches loading of live times in [viewModelScope] which
     * cancels when this [ViewModel] is cleared. This prevents the live times being reloaded every
     * time the app is brought back in to the foreground.
     */
    private val liveTimes = MutableLiveData<UiTransformedResult>().apply {
        viewModelScope.launch {
            createLiveTimesFlow().collect {
                value = it
            }
        }
    }

    /**
     * This [LiveData] is a proxy to [liveTimes], in order to be able to provide a [liveData]
     * builder where the auto-refresh can be performed.
     */
    private val liveTimesProxy = liveTimes.switchMap {
        liveData(viewModelScope.coroutineContext) {
            emit(it)
            performAutoRefreshDelay(it)
        }
    }

    /**
     * Show loading progress to the user. If there is no stop code, this will emit `null`.
     * Otherwise, it will emit the progress state of the loading live times.
     */
    val showProgressLiveData = distinctStopCodeLiveData.switchMap { stopCode ->
        if (stopCode?.isNotEmpty() == true) {
            liveTimesProxy.map {
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
        addSource(liveTimesProxy) {
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
        addSource(liveTimesProxy) {
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
        refreshTriggerChannel.offer(Unit)
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
                refreshTriggerChannel.consumeAsFlow())

        return liveTimesTransform.getLiveTimesTransformFlow(
                liveTimesFlow,
                expandedServicesTracker.expandedServicesLiveData.asFlow())
                .flowOn(defaultDispatcher)
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

    /**
     * Handle the auto-refresh preference being changed. If this preference has been changed to the
     * enabled state, the time of the last refresh, as reported by [liveTimesProxy], will be
     * inspected and if this is older than [AUTO_REFRESH_INTERVAL_MILLIS], a refresh will occur.
     *
     * @param enabled `true` if auto-refresh has been enabled, otherwise `false`.
     */
    private fun handleAutoRefreshPreferenceChanged(enabled: Boolean) {
        if (enabled) {
            liveTimesProxy.value?.let {
                getDelayUntilNextRefresh(it)?.let { delayMillis ->
                    if (delayMillis <= 0) {
                        refreshTriggerChannel.offer(Unit)
                    }
                }
            }
        }
    }

    /**
     * Perform the auto-refresh delay, and only issue the refresh request if auto-refresh is
     * enabled once the delay has expired. The auto-refresh period is defined as
     * [AUTO_REFRESH_INTERVAL_MILLIS], therefore the next refresh will happen at the time of the
     * last refresh + [AUTO_REFRESH_INTERVAL_MILLIS]. This method uses the amount of time between
     * now and the calculated timestamp to calculate the length of delay. If the calculated delay
     * is less than `0`, that is, the next refresh time is in the past, the next refresh happens
     * immediately.
     *
     * @param result The last loaded [UiTransformedResult], used to calculate when the next refresh
     * time should be.
     */
    private suspend fun performAutoRefreshDelay(result: UiTransformedResult) {
        getDelayUntilNextRefresh(result)?.let {
            delay(it)

            if (isAutoRefreshLiveData.value == true) {
                refreshTriggerChannel.send(Unit)
            }
        }
    }

    /**
     * Get the amount of time in milliseconds until the next refresh should occur (if auto-refresh
     * is enabled). A negative or `0` value means a refresh should occur immediately. A positive
     * value means there should be a delay until the next refresh happens. A `null` value means no
     * refresh or delay should happen.
     *
     * @param result The last loaded [UiTransformedResult], used to calculate when the next refresh
     * time should be.
     * @return The number of milliseconds until the next refresh, or `null` if a refresh should not
     * occur.
     */
    private fun getDelayUntilNextRefresh(result: UiTransformedResult): Long? {
        return when (result) {
            is UiTransformedResult.Success -> result.receiveTime
            is UiTransformedResult.Error -> result.receiveTime
            else -> null
        }?.let {
            it + AUTO_REFRESH_INTERVAL_MILLIS - timeUtils.getCurrentTimeMillis()
        }
    }
}