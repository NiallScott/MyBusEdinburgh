/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.ImmutableList
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddArrivalAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddOrEditFavouriteStopListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusStopMapWithStopIdentifierListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmFavouriteRemovalListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmRemoveArrivalAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmRemoveProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowSystemLocationPreferencesListener
import uk.org.rivernile.android.bustracker.ui.formatters.LocalNumberFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberNumberFormatter
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * Show a list of the nearest bus stops to the device. If a location could not be found or the
 * user is too far away, an error message will be shown. The user is able to filter the shown bus
 * stops by what bus services stop there. Tapping the stop shows bus times for that stop. Each stop
 * has a contextual popup menu to perform more actions.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
public class NearestStopsFragment : Fragment() {

    private var callbacks: Callbacks? = null

    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        this::handleLocationPermissionsResult
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = context as? Callbacks
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        MyBusTheme {
            CompositionLocalProvider(
                LocalNumberFormatter provides rememberNumberFormatter()
            ) {
                NearestStopsScreen(
                    modifier = Modifier
                        .consumeWindowInsets(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
                        ),
                    onShowStopData = ::handleOnShowStopData,
                    onShowAddFavouriteStop = ::handleOnShowAddFavouriteStop,
                    onShowRemoveFavouriteStop = ::handleOnShowConfirmRemoveFavouriteStop,
                    onShowAddArrivalAlert = ::handleOnShowAddArrivalAlert,
                    onShowRemoveArrivalAlert = ::handleOnShowConfirmRemoveArrivalAlert,
                    onShowAddProximityAlert = ::handleOnShowAddProximityAlert,
                    onShowRemoveProximityAlert = ::handleOnShowConfirmRemoveProximityAlert,
                    onShowOnMap = ::handleOnShowStopOnMap,
                    onRequestLocationPermissions = ::handleRequestLocationPermissions,
                    onShowServicesChooser = ::handleShowServicesChooser,
                    onShowLocationSettings = ::handleShowLocationSettings,
                    onShowTurnOnGps = ::handleShowTurnOnGps
                )
            }
        }
    }

    override fun onDetach() {
        super.onDetach()

        callbacks = null
    }

    private fun handleOnShowStopData(stopIdentifier: StopIdentifier) {
        callbacks?.onShowBusTimes(stopIdentifier)
    }

    private fun handleOnShowAddFavouriteStop(stopIdentifier: StopIdentifier) {
        callbacks?.onShowAddOrEditFavouriteStop(stopIdentifier)
    }

    private fun handleOnShowConfirmRemoveFavouriteStop(stopIdentifier: StopIdentifier) {
        callbacks?.onShowConfirmFavouriteRemoval(stopIdentifier)
    }

    private fun handleOnShowAddArrivalAlert(stopIdentifier: StopIdentifier) {
        callbacks?.onShowAddArrivalAlert(stopIdentifier, null)
    }

    private fun handleOnShowConfirmRemoveArrivalAlert(stopIdentifier: StopIdentifier) {
        callbacks?.onShowConfirmRemoveArrivalAlert(stopIdentifier)
    }

    private fun handleOnShowAddProximityAlert(stopIdentifier: StopIdentifier) {
        callbacks?.onShowAddProximityAlert(stopIdentifier)
    }

    private fun handleOnShowConfirmRemoveProximityAlert(stopIdentifier: StopIdentifier) {
        callbacks?.onShowConfirmRemoveProximityAlert(stopIdentifier)
    }

    private fun handleOnShowStopOnMap(stopIdentifier: StopIdentifier) {
        callbacks?.onShowBusStopMapWithStopIdentifier(stopIdentifier)
    }

    private fun handleRequestLocationPermissions() {
        requestLocationPermissionsLauncher
            .launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
    }

    private fun handleShowServicesChooser(selectedServices: ImmutableList<ServiceDescriptor>?) {

    }

    private fun handleShowLocationSettings() {
        callbacks?.let { cb ->
            if (!cb.onShowSystemLocationPreferences()) {
                Toast
                    .makeText(
                        requireContext(),
                        R.string.neareststops_error_no_location_settings,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }
    }

    private fun handleShowTurnOnGps() {
        callbacks?.onAskTurnOnGps()
    }

    private fun handleLocationPermissionsResult(states: Map<String, Boolean>) {

    }

    /**
     * Activities which host this [Fragment] should implement this interface.
     */
    public interface Callbacks :
        OnShowConfirmFavouriteRemovalListener,
        OnShowConfirmRemoveProximityAlertListener,
        OnShowConfirmRemoveArrivalAlertListener,
        OnShowAddOrEditFavouriteStopListener,
        OnShowAddProximityAlertListener,
        OnShowAddArrivalAlertListener,
        OnShowBusTimesListener,
        OnShowBusStopMapWithStopIdentifierListener,
        OnShowSystemLocationPreferencesListener {

        /**
         * This is called when the user should be asked if they want to turn on GPS or not.
         */
        public fun onAskTurnOnGps()
    }
}
