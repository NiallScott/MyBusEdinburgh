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

package uk.org.rivernile.android.bustracker.ui.bustimes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
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
@ExperimentalCoroutinesApi
class DisplayStopDataActivityViewModel @Inject constructor(
        private val busStopsRepository: BusStopsRepository,
        private val favouritesRepository: FavouritesRepository,
        private val alertsRepository: AlertsRepository) : ViewModel() {

    private val stopCodeLiveData = MutableLiveData<String>()
    private val distinctStopCodeLiveData = stopCodeLiveData.distinctUntilChanged()

    /**
     * This [LiveData] contains [StopDetails] for the given [stopCode].
     */
    val busStopDetails = distinctStopCodeLiveData.switchMap(this::loadBusStopDetails)

    /**
     * This [LiveData] contains whether the stop represented by [stopCode] is added as a favourite
     * or not.
     */
    val isFavouriteLiveData = distinctStopCodeLiveData.switchMap(this::loadIsFavourite)
    /**
     * This [LiveData] contains whether the stop represented by [stopCode] is added as an arrival
     * alert or not.
     */
    val hasArrivalAlertLiveData = distinctStopCodeLiveData.switchMap(this::loadHasArrivalAlert)
    /**
     * This [LiveData] contains whether the stop represented by [stopCode] is added as a proximity
     * alert or not.
     */
    val hasProximityAlertLiveData = distinctStopCodeLiveData.switchMap(this::loadHasProximityAlert)

    /**
     * This [LiveData] is invoked when the 'Add favourite stop' UI should be shown.
     */
    val showAddFavouriteLiveData: LiveData<String> get() = showAddFavourite
    /**
     * This [LiveData] is invoked when the 'Remove favourite stop' UI should be shown.
     */
    val showRemoveFavouriteLiveData: LiveData<String> get() = showRemoveFavourite
    /**
     * This [LiveData] is invoked when the 'Add arrival alert' UI should be shown.
     */
    val showAddArrivalAlertLiveData: LiveData<String> get() = showAddArrivalAlert
    /**
     * This [LiveData] is invoked when the 'Remove arrival alert' UI should be shown.
     */
    val showRemoveArrivalAlertLiveData: LiveData<String> get() = showRemoveArrivalAlert
    /**
     * This [LiveData] is invoked when the 'Add proximity alert' UI should be shown.
     */
    val showAddProximityAlertLiveData: LiveData<String> get() = showAddProximityAlert
    /**
     * This [LiveData] is invoked when the 'Remove proximity alert' UI should be shown.
     */
    val showRemoveProximityAlertLiveData: LiveData<String> get() = showRemoveProximityAlert
    /**
     * This [LiveData] is invoked when the 'Street View' UI should be shown.
     */
    val showStreetViewLiveData: LiveData<StopDetails> get() = showStreetView

    /**
     * This property is used to get and set the stop code which should be shown.
     */
    var stopCode: String?
        get() = stopCodeLiveData.value
        set (value) {
            stopCodeLiveData.value = value
        }

    private val showAddFavourite = SingleLiveEvent<String>()
    private val showRemoveFavourite = SingleLiveEvent<String>()
    private val showAddArrivalAlert = SingleLiveEvent<String>()
    private val showRemoveArrivalAlert = SingleLiveEvent<String>()
    private val showAddProximityAlert = SingleLiveEvent<String>()
    private val showRemoveProximityAlert = SingleLiveEvent<String>()
    private val showStreetView = SingleLiveEvent<StopDetails>()

    /**
     * This is called when the favourite menu item has been clicked.
     */
    fun onFavouriteMenuItemClicked() {
        stopCode?.let {
            if (isFavouriteLiveData.value == true) {
                showRemoveFavourite.value = it
            } else {
                showAddFavourite.value = it
            }
        }
    }

    /**
     * This is called when the arrival alert menu item has been clicked.
     */
    fun onArrivalAlertMenuItemClicked() {
        stopCode?.let {
            if (hasArrivalAlertLiveData.value == true) {
                showRemoveArrivalAlert.value = it
            } else {
                showAddArrivalAlert.value = it
            }
        }
    }

    /**
     * This is called when the proximity alert menu item has been clicked.
     */
    fun onProximityAlertMenuItemClicked() {
        stopCode?.let {
            if (hasProximityAlertLiveData.value == true) {
                showRemoveProximityAlert.value = it
            } else {
                showAddProximityAlert.value = it
            }
        }
    }

    /**
     * This is called when the street view menu item has been clicked.
     */
    fun onStreetViewMenuItemClicked() {
        busStopDetails.value?.let {
            showStreetView.value = it
        }
    }

    /**
     * Load the bus stop details. If the stop code is set as `null`, then `null` details will be
     * set.
     *
     * @param stopCode The stop code to load.
     * @return A [LiveData] which contains the details for the stop.
     */
    private fun loadBusStopDetails(stopCode: String?): LiveData<StopDetails?> = stopCode?.let {
        busStopsRepository.getBusStopDetailsFlow(it)
                .distinctUntilChanged()
                .asLiveData()
    } ?: MutableLiveData<StopDetails?>(null)

    /**
     * Load whether the stop is added as a favourite or not. If the stop code is set as `null`, then
     * `false` will be set.
     *
     * @param stopCode The stop code to load.
     * @return A [LiveData] which contains the favourite status of the stop.
     */
    private fun loadIsFavourite(stopCode: String?): LiveData<Boolean> = stopCode?.let {
        favouritesRepository.isStopAddedAsFavouriteFlow(it)
                .distinctUntilChanged()
                .asLiveData()
    } ?: MutableLiveData(false)

    /**
     * Load whether the stop is added as an arrival alert or not. If the stop code is set as `null`,
     * then `false` will be set.
     *
     * @param stopCode The stop code to load.
     * @return A [LiveData] which contains the arrival alert status of the stop.
     */
    private fun loadHasArrivalAlert(stopCode: String?): LiveData<Boolean> = stopCode?.let {
        alertsRepository.hasArrivalAlertFlow(it)
                .distinctUntilChanged()
                .asLiveData()
    } ?: MutableLiveData(false)

    /**
     * Load whether the stop is added as a proximity alert or not. If the stop code is set as
     * `null`, then `false` will be set.
     *
     * @param stopCode The stop code to load.
     * @return A [LiveData] which contains the proximity alert status of the stop.
     */
    private fun loadHasProximityAlert(stopCode: String?): LiveData<Boolean> = stopCode?.let {
        alertsRepository.hasProximityAlertFlow(it)
                .distinctUntilChanged()
                .asLiveData()
    } ?: MutableLiveData(false)
}