/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopDetails
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [DisplayStopDataActivity].
 *
 * @param busStopsRepository The repository to obtain bus stop details from.
 * @param favouritesRepository The repository to obtain favourite details from.
 * @param alertsRepository The repository to obtain alert details from.
 * @author Niall Scott
 */
@HiltViewModel
class DisplayStopDataActivityViewModel @Inject constructor(
    private val busStopsRepository: BusStopsRepository,
    private val favouritesRepository: FavouritesRepository,
    private val alertsRepository: AlertsRepository,
    @param:ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    /**
     * This property is used to get and set the stop code which should be shown.
     */
    var stopIdentifier: StopIdentifier?
        get() = stopIdentifierFlow.value
        set(value) {
            stopIdentifierFlow.value = value
        }

    private val stopIdentifierFlow = MutableStateFlow<StopIdentifier?>(null)

    /**
     * This [LiveData] emits the set stop identifier.
     */
    val stopIdentifierLiveData = stopIdentifierFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stopDetailsFlow = stopIdentifierFlow
        .flatMapLatest(this::loadBusStopDetails)
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits [StopDetails] for the given [stopIdentifier].
     */
    val stopDetailsLiveData = stopDetailsFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    private val stopDetails get() = stopDetailsFlow.value

    @OptIn(ExperimentalCoroutinesApi::class)
    private val isFavouriteFlow = stopIdentifierFlow
        .flatMapLatest(this::loadIsFavourite)
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits whether the stop represented by [stopIdentifier] is added as a favourite
     * or not.
     */
    val isFavouriteLiveData = isFavouriteFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    private val isFavourite get() = isFavouriteFlow.value

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasArrivalAlertFlow = stopIdentifierFlow
        .flatMapLatest(this::loadHasArrivalAlert)
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits whether the stop represented by [stopIdentifier] is added as an arrival
     * alert or not.
     */
    val hasArrivalAlertLiveData = hasArrivalAlertFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    private val hasArrivalAlert get() = hasArrivalAlertFlow.value

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasProximityAlertFlow = stopIdentifierFlow
        .flatMapLatest(this::loadHasProximityAlert)
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits whether the stop represented by [stopIdentifier] is added as a proximity
     * alert or not.
     */
    val hasProximityAlertLiveData = hasProximityAlertFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    private val hasProximityAlert get() = hasProximityAlertFlow.value

    /**
     * This [LiveData] is invoked when the 'Add favourite stop' UI should be shown.
     */
    val showAddFavouriteLiveData: LiveData<StopIdentifier> get() = showAddFavourite
    private val showAddFavourite = SingleLiveEvent<StopIdentifier>()

    /**
     * This [LiveData] is invoked when the 'Remove favourite stop' UI should be shown.
     */
    val showRemoveFavouriteLiveData: LiveData<StopIdentifier> get() = showRemoveFavourite
    private val showRemoveFavourite = SingleLiveEvent<StopIdentifier>()

    /**
     * This [LiveData] is invoked when the 'Add arrival alert' UI should be shown.
     */
    val showAddArrivalAlertLiveData: LiveData<StopIdentifier> get() = showAddArrivalAlert
    private val showAddArrivalAlert = SingleLiveEvent<StopIdentifier>()

    /**
     * This [LiveData] is invoked when the 'Remove arrival alert' UI should be shown.
     */
    val showRemoveArrivalAlertLiveData: LiveData<StopIdentifier> get() = showRemoveArrivalAlert
    private val showRemoveArrivalAlert = SingleLiveEvent<StopIdentifier>()

    /**
     * This [LiveData] is invoked when the 'Add proximity alert' UI should be shown.
     */
    val showAddProximityAlertLiveData: LiveData<StopIdentifier> get() = showAddProximityAlert
    private val showAddProximityAlert = SingleLiveEvent<StopIdentifier>()

    /**
     * This [LiveData] is invoked when the 'Remove proximity alert' UI should be shown.
     */
    val showRemoveProximityAlertLiveData: LiveData<StopIdentifier> get() = showRemoveProximityAlert
    private val showRemoveProximityAlert = SingleLiveEvent<StopIdentifier>()

    /**
     * This [LiveData] is invoked when the 'Street View' UI should be shown.
     */
    val showStreetViewLiveData: LiveData<StopDetails> get() = showStreetView
    private val showStreetView = SingleLiveEvent<StopDetails>()

    /**
     * This is called when the favourite menu item has been clicked.
     */
    fun onFavouriteMenuItemClicked() {
        // Ignore stop code null or empty.
        stopIdentifier?.let { sc ->
            // Ignore when isFavourite is null.
            isFavourite?.let {
                if (it) {
                    showRemoveFavourite.value = sc
                } else {
                    showAddFavourite.value = sc
                }
            }
        }
    }

    /**
     * This is called when the arrival alert menu item has been clicked.
     */
    fun onArrivalAlertMenuItemClicked() {
        stopIdentifier?.let { sc ->
            hasArrivalAlert?.let {
                if (it) {
                    showRemoveArrivalAlert.value = sc
                } else {
                    showAddArrivalAlert.value = sc
                }
            }
        }
    }

    /**
     * This is called when the proximity alert menu item has been clicked.
     */
    fun onProximityAlertMenuItemClicked() {
        stopIdentifier?.let { sc ->
            hasProximityAlert?.let {
                if (it) {
                    showRemoveProximityAlert.value = sc
                } else {
                    showAddProximityAlert.value = sc
                }
            }
        }
    }

    /**
     * This is called when the street view menu item has been clicked.
     */
    fun onStreetViewMenuItemClicked() {
        stopDetails?.let {
            showStreetView.value = it
        }
    }

    /**
     * Load the bus stop details. If the stop code is set as `null`, then `null` details will be
     * set.
     *
     * @param stopIdentifier The stop to load.
     * @return A [kotlinx.coroutines.flow.Flow] which emits the details for the stop.
     */
    private fun loadBusStopDetails(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        busStopsRepository.getBusStopDetailsFlow(it)
            .onStart { emit(null) }
    } ?: flowOf(null)

    /**
     * Load whether the stop is added as a favourite or not. If the stop code is set as `null`, then
     * `null` will be set.
     *
     * @param stopIdentifier The stop to load.
     * @return A [kotlinx.coroutines.flow.Flow] which emits the favourite status of the stop.
     */
    private fun loadIsFavourite(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        favouritesRepository.isStopAddedAsFavouriteFlow(it)
            .onStart<Boolean?> { emit(null) }
    } ?: flowOf(null)

    /**
     * Load whether the stop is added as an arrival alert or not. If the stop code is set as `null`,
     * then `null` will be set.
     *
     * @param stopIdentifier The stop to load.
     * @return A [kotlinx.coroutines.flow.Flow] which emits the arrival alert status of the stop.
     */
    private fun loadHasArrivalAlert(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        alertsRepository.hasArrivalAlertFlow(it)
            .onStart<Boolean?> { emit(null) }
    } ?: flowOf(null)

    /**
     * Load whether the stop is added as a proximity alert or not. If the stop code is set as
     * `null`, then `null` will be set.
     *
     * @param stopIdentifier The stop to load.
     * @return A [kotlinx.coroutines.flow.Flow] which contains the proximity alert status of the
     * stop.
     */
    private fun loadHasProximityAlert(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        alertsRepository.hasProximityAlertFlow(it)
            .onStart<Boolean?> { emit(null) }
    } ?: flowOf(null)
}
