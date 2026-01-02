/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddOrEditFavouriteStopListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddArrivalAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusStopMapWithStopCodeListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmRemoveProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmRemoveArrivalAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmFavouriteRemovalListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowSystemLocationPreferencesListener
import uk.org.rivernile.android.bustracker.ui.HasScrollableContent
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserParams
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.FragmentNearestStopsBinding
import javax.inject.Inject

/**
 * Show a list of the nearest bus stops to the device. If a location could not be found or the
 * user is too far away, an error message will be shown. The user is able to filter the shown bus
 * stops by what bus services stop there. Long pressing on a bus stop shows a contextual action bar
 * where the user can perform various actions on that stop. Tapping the stop shows bus times for
 * that stop.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class NearestStopsFragment : Fragment(), HasScrollableContent {

    companion object {

        private const val DIALOG_SELECT_SERVICES = "selectServicesDialog"
    }

    @Inject
    lateinit var stopMapMarkerDecorator: StopMapMarkerDecorator
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private val viewModel by viewModels<NearestStopsFragmentViewModel>()

    private lateinit var callbacks: Callbacks
    private lateinit var adapter: NearestStopsAdapter

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: FragmentNearestStopsBinding? = null

    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        this::handleLocationPermissionsResult
    )

    private var menuItemFilter: MenuItem? = null

    private var actionMode: ActionMode? = null

    private var amMenuItemFavourite: MenuItem? = null
    private var amMenuItemProxAlert: MenuItem? = null
    private var amMenuItemTimeAlert: MenuItem? = null
    private var amMenuItemShowOnMap: MenuItem? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (_: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                    Callbacks::class.java.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = NearestStopsAdapter(
            requireContext(),
            itemClickListener,
            stopMapMarkerDecorator,
            textFormattingUtils
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentNearestStopsBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
                val insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout()
                )

                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                    rightMargin = insets.right
                }

                WindowInsetsCompat.CONSUMED
            }

            recyclerView.adapter = adapter

            btnErrorResolve.setOnClickListener {
                viewModel.onResolveErrorButtonClicked()
            }
        }

        val viewLifecycleOwner = viewLifecycleOwner
        childFragmentManager.setFragmentResultListener(
            ServicesChooserDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            handleServicesChosen(result)
        }

        viewModel.itemsLiveData.observe(viewLifecycleOwner, adapter::submitList)
        viewModel.uiStateLiveData.observe(viewLifecycleOwner, this::handleUiStateChanged)
        viewModel.askForLocationPermissionsLiveData.observe(viewLifecycleOwner) {
            handleAskLocationForPermissions()
        }
        viewModel.isFilterEnabledLiveData.observe(viewLifecycleOwner, this::handleIsFilterEnabled)
        viewModel.showContextMenuLiveData.observe(viewLifecycleOwner, this::handleShowContextMenu)
        viewModel.selectedStopNameLiveData.observe(viewLifecycleOwner, this::handleSelectedStopName)
        viewModel.isStopMapVisibleLiveData.observe(viewLifecycleOwner,
            this::handleIsShowOnMapVisible)
        viewModel.isArrivalAlertVisibleLiveData.observe(viewLifecycleOwner,
                this::handleIsArrivalAlertVisible)
        viewModel.isProximityAlertVisibleLiveData.observe(viewLifecycleOwner,
                this::handleIsProximityAlertVisible)
        viewModel.isFavouriteEnabledLiveData.observe(viewLifecycleOwner,
                this::handleIsFavouriteStopEnabled)
        viewModel.isArrivalAlertEnabledLiveData.observe(viewLifecycleOwner,
                this::handleIsArrivalAlertEnabled)
        viewModel.isProximityAlertEnabledLiveData.observe(viewLifecycleOwner,
                this::handleIsProximityAlertEnabled)
        viewModel.isAddedAsFavouriteStopLiveData.observe(viewLifecycleOwner,
                this::handleIsFavouriteStop)
        viewModel.hasArrivalAlertLiveData.observe(viewLifecycleOwner, this::handleHasArrivalAlert)
        viewModel.hasProximityAlertLiveData.observe(viewLifecycleOwner,
                this::handleHasProximityAlert)
        viewModel.showStopDataLiveData.observe(viewLifecycleOwner, this::handleShowStopData)
        viewModel.showAddFavouriteStopLiveData.observe(viewLifecycleOwner,
                this::handleShowAddFavouriteStop)
        viewModel.showConfirmDeleteFavouriteLiveData.observe(viewLifecycleOwner,
                this::handleShowConfirmDeleteFavouriteStop)
        viewModel.showOnMapLiveData.observe(viewLifecycleOwner, this::handleShowOnMap)
        viewModel.showAddArrivalAlertLiveData.observe(viewLifecycleOwner,
                this::handleAddArrivalAlert)
        viewModel.showConfirmDeleteArrivalAlertLiveData.observe(viewLifecycleOwner,
                this::handleConfirmDeleteArrivalAlert)
        viewModel.showAddProximityAlertLiveData.observe(viewLifecycleOwner,
                this::handleAddProximityAlert)
        viewModel.showConfirmDeleteProximityAlertLiveData.observe(viewLifecycleOwner,
                this::handleConfirmDeleteProximityAlert)
        viewModel.showServicesChooserLiveData.observe(viewLifecycleOwner, this::showServicesChooser)
        viewModel.showLocationSettingsLiveData.observe(viewLifecycleOwner) {
            handleShowLocationSettings()
        }
        viewModel.showTurnOnGpsLiveData.observe(viewLifecycleOwner) {
            callbacks.onAskTurnOnGps()
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    override fun onResume() {
        super.onResume()

        updatePermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    override val scrollableContentIdRes get() = R.id.recyclerView

    /**
     * Update [NearestStopsFragmentViewModel] with the current state of permissions.
     */
    private fun updatePermissions() {
        viewModel.permissionsState = PermissionsState(
            getPermissionState(Manifest.permission.ACCESS_FINE_LOCATION),
            getPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    /**
     * Handle asking the user to grant permissions.
     */
    private fun handleAskLocationForPermissions() {
        requestLocationPermissionsLauncher
            .launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
    }

    /**
     * Handle the result of asking for location permission access.
     *
     * @param states A [Map] of the permission to a boolean which informs us if the permission was
     * granted or not.
     */
    private fun handleLocationPermissionsResult(states: Map<String, Boolean>) {
        val fineLocationState = states[Manifest.permission.ACCESS_FINE_LOCATION]
            ?.let(this::getPermissionState)
            ?: getPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationState = states[Manifest.permission.ACCESS_COARSE_LOCATION]
            ?.let(this::getPermissionState)
            ?: getPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)

        viewModel.permissionsState = PermissionsState(fineLocationState, coarseLocationState)
    }

    /**
     * For a given [permission], determine the [PermissionState].
     *
     * @param permission The permission to obtain the [PermissionState] for.
     * @return The determined [PermissionState].
     */
    private fun getPermissionState(permission: String) =
        getPermissionState(
            ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED
        )

    /**
     * Maps the permission granted status in to a [PermissionState].
     *
     * @param isGranted Has the permission been granted? `true` implies [PermissionState.GRANTED],
     * otherwise [PermissionState.UNGRANTED].
     * @return The determined [PermissionState].
     */
    private fun getPermissionState(isGranted: Boolean) = if (isGranted) {
        PermissionState.GRANTED
    } else {
        PermissionState.UNGRANTED
    }

    /**
     * Handle whether the context menu should be shown or not.
     *
     * @param showContextMenu Should the context menu be shown?
     */
    private fun handleShowContextMenu(showContextMenu: Boolean) {
        if (showContextMenu) {
            // Only show ActionMode when it isn't already showing.
            if (actionMode == null) {
                actionMode = (requireActivity() as? AppCompatActivity)
                    ?.startSupportActionMode(actionModeCallback)
            }
        } else {
            actionMode?.finish()
            actionMode = null
        }
    }

    /**
     * Handle the stop name to show in the title of the context menu.
     *
     * @param name The name data to show in the title of the context menu.
     */
    private fun handleSelectedStopName(name: UiNearestStopName?) {
        actionMode?.title = name?.let {
            textFormattingUtils.formatBusStopNameWithStopCode(it.stopCode, it.stopName)
        }
    }

    /**
     * Handle the enabled state of the filter menu item.
     *
     * @param isEnabled Is the filter menu item enabled?
     */
    private fun handleIsFilterEnabled(isEnabled: Boolean) {
        menuItemFilter?.isEnabled = isEnabled
    }

    /**
     * Handle the visibility status of the "Show on map" context menu item.
     *
     * @param isVisible Is "Show on map" visible in the context menu?
     */
    private fun handleIsShowOnMapVisible(isVisible: Boolean) {
        amMenuItemShowOnMap?.isVisible = isVisible
    }

    /**
     * Handle the visibility status of the arrival alert context menu item.
     *
     * @param isVisible Is the arrival alert item visible in the context menu?
     */
    private fun handleIsArrivalAlertVisible(isVisible: Boolean) {
        amMenuItemTimeAlert?.isVisible = isVisible
    }

    /**
     * Handle the visibility status of the proximity alert context menu item.
     *
     * @param isVisible Is the proximity alert item visible in the context menu?
     */
    private fun handleIsProximityAlertVisible(isVisible: Boolean) {
        amMenuItemProxAlert?.isVisible = isVisible
    }

    /**
     * Handle the enabled status of the favourite context menu item.
     *
     * @param isEnabled Is the favourite item enabled in the context menu?
     */
    private fun handleIsFavouriteStopEnabled(isEnabled: Boolean) {
        amMenuItemFavourite?.isEnabled = isEnabled
    }

    /**
     * Handle the enabled status of the arrival alert context menu item.
     *
     * @param isEnabled Is the arrival alert item enabled in the context menu?
     */
    private fun handleIsArrivalAlertEnabled(isEnabled: Boolean) {
        amMenuItemTimeAlert?.isEnabled = isEnabled
    }

    /**
     * Handle the enabled status of the proximity alert context menu item.
     *
     * @param isEnabled Is the proximity alert item enabled in the context menu?
     */
    private fun handleIsProximityAlertEnabled(isEnabled: Boolean) {
        amMenuItemProxAlert?.isEnabled = isEnabled
    }

    /**
     * Handle the state of the favourite stop int he context menu.
     *
     * @param isFavouriteStop Is the selected stop added as a user favourite?
     */
    private fun handleIsFavouriteStop(isFavouriteStop: Boolean) {
        amMenuItemFavourite?.apply {
            if (isFavouriteStop) {
                setTitle(R.string.neareststops_context_remasfav)
                setIcon(R.drawable.ic_action_star)
            } else {
                setTitle(R.string.neareststops_context_addasfav)
                setIcon(R.drawable.ic_action_star_border)
            }
        }
    }

    /**
     * Handle the state of the arrival alert in the context menu.
     *
     * @param hasArrivalAlert Is there an arrival alert for the currently selected stop?
     */
    private fun handleHasArrivalAlert(hasArrivalAlert: Boolean) {
        amMenuItemTimeAlert?.apply {
            if (hasArrivalAlert) {
                setTitle(R.string.neareststops_menu_time_rem)
                setIcon(R.drawable.ic_action_alarm_off)
            } else {
                setTitle(R.string.neareststops_menu_time_add)
                setIcon(R.drawable.ic_action_alarm_add)
            }
        }
    }

    /**
     * Handle the state of the proximity alert in the context menu.
     *
     * @param hasProximityAlert Is there a proximity alert for the currently selected stop?
     */
    private fun handleHasProximityAlert(hasProximityAlert: Boolean) {
        amMenuItemProxAlert?.apply {
            if (hasProximityAlert) {
                setTitle(R.string.neareststops_menu_prox_rem)
                setIcon(R.drawable.ic_action_location_off)
            } else {
                setTitle(R.string.neareststops_menu_prox_add)
                setIcon(R.drawable.ic_action_location_on)
            }
        }
    }

    /**
     * Show stop data.
     *
     * @param stopCode The stop code to show stop data for.
     */
    private fun handleShowStopData(stopCode: String) {
        callbacks.onShowBusTimes(stopCode)
    }

    /**
     * Present UI to allow the user to add the given stop as a favourite.
     *
     * @param stopCode The stop to add as a favourite.
     */
    private fun handleShowAddFavouriteStop(stopCode: String) {
        callbacks.onShowAddOrEditFavouriteStop(stopCode)
    }

    /**
     * Present UI to ask the user to confirm if they wish to delete the given favourite stop.
     *
     * @param stopCode The stop to confirm deletion for.
     */
    private fun handleShowConfirmDeleteFavouriteStop(stopCode: String) {
        callbacks.onShowConfirmFavouriteRemoval(stopCode)
    }

    /**
     * Show the given stop on the map.
     *
     * @param stopCode The stop to show on the map.
     */
    private fun handleShowOnMap(stopCode: String) {
        callbacks.onShowBusStopMapWithStopCode(stopCode)
    }

    /**
     * Present UI to the user to allow them to add an arrival alert for the given stop.
     *
     * @param stopCode The stop to add an arrival alert for.
     */
    private fun handleAddArrivalAlert(stopCode: String) {
        callbacks.onShowAddArrivalAlert(stopCode, null)
    }

    /**
     * Present UI to the user to allow them to confirm if the arrival alert for the given stop
     * should be removed.
     *
     * @param stopCode The stop to confirm arrival alert deletion for.
     */
    private fun handleConfirmDeleteArrivalAlert(stopCode: String) {
        callbacks.onShowConfirmRemoveArrivalAlert(stopCode)
    }

    /**
     * Present UI to the user to allow them to add a proximity alert for the given stop.
     *
     * @param stopCode The stop to add a proximity alert for.
     */
    private fun handleAddProximityAlert(stopCode: String) {
        callbacks.onShowAddProximityAlert(stopCode)
    }

    /**
     * Present UI to the user to allow them to confirm if the proximity alert for the given stop
     * should be removed.
     *
     * @param stopCode The stop to confirm proximity alert deletion for.
     */
    private fun handleConfirmDeleteProximityAlert(stopCode: String) {
        callbacks.onShowConfirmRemoveProximityAlert(stopCode)
    }

    /**
     * Show a dialog to the user which allows them to choose which services to filter nearby stops
     * with.
     *
     * @param selectedServices The existing selected services.
     */
    private fun showServicesChooser(selectedServices: List<String>?) {
        ServicesChooserParams.AllServices(
            R.string.neareststops_service_chooser_title,
            selectedServices)
            .let {
                ServicesChooserDialogFragment.newInstance(it)
                    .show(childFragmentManager, DIALOG_SELECT_SERVICES)
            }
    }

    /**
     * Handle a request to show the location settings.
     */
    private fun handleShowLocationSettings() {
        if (!callbacks.onShowSystemLocationPreferences()) {
            Toast
                .makeText(
                    requireContext(),
                    R.string.neareststops_error_no_location_settings,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    /**
     * Handle the UI state changing.
     *
     * @param state The new [UiState].
     */
    private fun handleUiStateChanged(state: UiState) {
        viewBinding.contentView.apply {
            when (state) {
                is UiState.InProgress -> showProgressLayout()
                is UiState.Success -> showContentLayout()
                is UiState.Error -> {
                    handleError(state)
                    showErrorLayout()
                }
            }
        }
    }

    /**
     * Handle an error [UiState].
     *
     * @param error The error to handle.
     */
    private fun handleError(error: UiState.Error) {
        actionMode?.finish()
        actionMode = null

        viewBinding.apply {
            btnErrorResolve.visibility = if (error.showResolveButton) {
                View.VISIBLE
            } else {
                View.GONE
            }

            when (error) {
                is UiState.Error.NoLocationFeature -> {
                    txtError.apply {
                        setText(R.string.neareststops_error_no_location_feature)
                        setCompoundDrawablesWithIntrinsicBounds(
                                0, R.drawable.ic_error_location_disabled, 0, 0)
                    }
                }
                is UiState.Error.InsufficientLocationPermissions -> {
                    txtError.apply {
                        setText(R.string.neareststops_error_permission_required)
                        setCompoundDrawablesWithIntrinsicBounds(
                                0, R.drawable.ic_error_perm_device_information, 0, 0)
                    }

                    btnErrorResolve.setText(R.string.neareststops_error_permission_required_button)
                }
                is UiState.Error.LocationOff -> {
                    txtError.apply {
                        setText(R.string.neareststops_error_location_sources)
                        setCompoundDrawablesWithIntrinsicBounds(
                                0, R.drawable.ic_error_location_disabled, 0, 0)
                    }

                    btnErrorResolve.setText(R.string.neareststops_error_location_sources_button)
                }
                is UiState.Error.LocationUnknown -> {
                    txtError.apply {
                        setText(R.string.neareststops_error_location_unknown)
                        setCompoundDrawablesWithIntrinsicBounds(
                                0, R.drawable.ic_error_location_disabled, 0, 0)
                    }
                }
                is UiState.Error.NoNearestStops -> {
                    txtError.apply {
                        setText(R.string.neareststops_error_empty)
                        setCompoundDrawablesWithIntrinsicBounds(
                                0, R.drawable.ic_error_my_location, 0, 0)
                    }
                }
            }
        }
    }

    /**
     * Handle the user selecting services to filter nearby results by.
     *
     * @param result The [Bundle] result generated by [ServicesChooserDialogFragment].
     */
    private fun handleServicesChosen(result: Bundle) {
        viewModel.selectedServices = result
            .getStringArrayList(ServicesChooserDialogFragment.RESULT_CHOSEN_SERVICES)
    }

    private val itemClickListener = object : OnNearStopItemClickListener {
        override fun onNearestStopClicked(item: UiNearestStop) {
            viewModel.onNearestStopClicked(item)
        }

        override fun onNearestStopLongClicked(stopCode: String) =
            viewModel.onNearestStopLongClicked(stopCode)
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.neareststops_context_menu, menu)
            amMenuItemFavourite = menu.findItem(R.id.neareststops_context_menu_favourite)
            amMenuItemProxAlert = menu.findItem(R.id.neareststops_context_menu_prox_alert)
            amMenuItemTimeAlert = menu.findItem(R.id.neareststops_context_menu_time_alert)
            amMenuItemShowOnMap = menu.findItem(R.id.neareststops_context_menu_showonmap)

            handleSelectedStopName(viewModel.selectedStopNameLiveData.value)

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            handleIsArrivalAlertVisible(viewModel.isArrivalAlertVisibleLiveData.value ?: false)
            handleIsProximityAlertVisible(viewModel.isProximityAlertVisibleLiveData.value ?: false)
            handleIsShowOnMapVisible(viewModel.isStopMapVisibleLiveData.value ?: false)

            handleIsFavouriteStopEnabled(viewModel.isFavouriteEnabledLiveData.value ?: false)
            handleIsArrivalAlertEnabled(viewModel.isArrivalAlertEnabledLiveData.value ?: false)
            handleIsProximityAlertEnabled(viewModel.isProximityAlertEnabledLiveData.value ?: false)

            handleIsFavouriteStop(viewModel.isAddedAsFavouriteStopLiveData.value ?: false)
            handleHasArrivalAlert(viewModel.hasArrivalAlertLiveData.value ?: false)
            handleHasProximityAlert(viewModel.hasProximityAlertLiveData.value ?: false)

            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = when (item.itemId) {
            R.id.neareststops_context_menu_favourite -> viewModel.onFavouriteMenuItemClicked()
            R.id.neareststops_context_menu_prox_alert -> viewModel.onProximityAlertMenuItemClicked()
            R.id.neareststops_context_menu_time_alert -> viewModel.onTimeAlertMenuItemClicked()
            R.id.neareststops_context_menu_showonmap -> viewModel.onShowOnMapMenuItemClicked()
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            amMenuItemFavourite = null
            amMenuItemProxAlert = null
            amMenuItemTimeAlert = null
            amMenuItemShowOnMap = null
            actionMode = null
            viewModel.onNearestStopUnselected()
        }
    }

    /**
     * Activities which host this [Fragment] must implement this interface to handle navigation
     * events.
     */
    interface Callbacks :
            OnShowConfirmFavouriteRemovalListener,
            OnShowConfirmRemoveProximityAlertListener,
            OnShowConfirmRemoveArrivalAlertListener,
            OnShowAddOrEditFavouriteStopListener,
            OnShowAddProximityAlertListener,
            OnShowAddArrivalAlertListener,
            OnShowBusTimesListener,
            OnShowBusStopMapWithStopCodeListener,
            OnShowSystemLocationPreferencesListener {

        /**
         * This is called when the user should be asked if they want to turn on GPS or not.
         */
        fun onAskTurnOnGps()
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.neareststops_option_menu, menu)
            menuItemFilter = menu.findItem(R.id.neareststops_option_menu_filter)
        }

        override fun onPrepareMenu(menu: Menu) {
            handleIsFilterEnabled(viewModel.isFilterEnabledLiveData.value ?: false)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
            R.id.neareststops_option_menu_filter -> {
                viewModel.onFilterMenuItemClicked()
                true
            }
            else -> false
        }
    }
}
