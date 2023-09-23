/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [StopDetailsFragment].
 *
 * @param savedState Used to save transient state and access it again.
 * @param uiItemRetriever Used to retrieve the items for display on the UI.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class StopDetailsFragmentViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    uiItemRetriever: UiItemRetriever,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        /**
         * State key for stop code.
         */
        const val STATE_STOP_CODE = "stopCode"
        private const val STATE_ASKED_FOR_PERMISSIONS = "askedForPermissions"

        private const val LIVE_DATA_TIMEOUT_MILLIS = 1000L
    }

    /**
     * The stop code to show details for.
     */
    private val stopCode get() = stopCodeFlow.value

    private val stopCodeFlow = savedState.getStateFlow<String?>(STATE_STOP_CODE, null)

    /**
     * The current state of permissions pertaining to this view.
     */
    var permissionsState: PermissionsState
        get() = permissionsStateFlow.value ?: PermissionsState()
        set(value) {
            permissionsStateFlow.value = value
            handlePermissionsSet(value)
        }

    private val permissionsStateFlow = MutableStateFlow<PermissionsState?>(null)

    /**
     * This [LiveData] emits when the user should be asked to grant location permissions.
     */
    val askForLocationPermissionsLiveData: LiveData<Unit> get() = askForLocationPermissions
    private val askForLocationPermissions = SingleLiveEvent<Unit>()

    private val itemsFlow = uiItemRetriever
            .createUiItemFlow(
                    stopCodeFlow,
                    permissionsStateFlow.filterNotNull(),
                    viewModelScope)
            .flowOn(defaultDispatcher)
            .onStart<List<UiItem>?> { emit(null) }
            .distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This [LiveData] emits the [List] of [UiItem]s to show on the UI.
     */
    val itemsLiveData = itemsFlow
        .asLiveData(viewModelScope.coroutineContext, LIVE_DATA_TIMEOUT_MILLIS)

    /**
     * This [LiveData] emits the current state of the UI, as defined by [UiState].
     */
    val uiStateLiveData = itemsFlow
            .map(this::calculateState)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext, LIVE_DATA_TIMEOUT_MILLIS)

    /**
     * This [LiveData] emits when the user should be shown a map with the given stop highlighted.
     */
    val showStopMapLiveData: LiveData<String> get() = showStopMap
    private val showStopMap = SingleLiveEvent<String>()

    /**
     * This is called when the map has been clicked on the UI.
     */
    fun onMapClicked() {
        stopCode?.let {
            showStopMap.value = it
        }
    }

    /**
     * Handle the permissions being updated. The logic in here determines if the user should be
     * asked to grant permission(s).
     *
     * @param permissionsState The newly-set [PermissionsState].
     */
    private fun handlePermissionsSet(permissionsState: PermissionsState) {
        val askedForPermissions: Boolean? = savedState[STATE_ASKED_FOR_PERMISSIONS]

        if (askedForPermissions != true) {
            savedState[STATE_ASKED_FOR_PERMISSIONS] = true

            if (permissionsState.fineLocationPermission == PermissionState.UNGRANTED &&
                    permissionsState.coarseLocationPermission == PermissionState.UNGRANTED) {
                askForLocationPermissions.call()
            }
        }
    }

    /**
     * Given the [List] of [UiItem]s to be shown, calculate the [UiState].
     *
     * A `null` or empty [List] implies data is still being loaded, and a populated [List] implies
     * there is content to be shown.
     *
     * @param items The items to be shown on the UI.
     * @return The current [UiState] based on the input [items] [List].
     */
    private fun calculateState(items: List<UiItem>?) = items?.ifEmpty { null }?.let {
        UiState.CONTENT
    } ?: UiState.PROGRESS
}