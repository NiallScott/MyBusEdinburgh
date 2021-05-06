/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.error.txtError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddEditFavouriteStopListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddTimeAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusStopMapWithStopCodeListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteTimeAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmFavouriteDeletionListener
import uk.org.rivernile.android.bustracker.viewmodel.GenericSavedStateViewModelFactory
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.FavouritestopsBinding
import javax.inject.Inject

/**
 * This [Fragment] shows the user a list of their favourite stops.
 *
 * How this [Fragment] behaves depends on if the hosting [android.app.Activity] implements
 * [CreateShortcutCallbacks] or not. When a user selects a favourite stop, if this interface is
 * implemented, it asks this interface to create a shortcut. If this interface is not implemented,
 * it instead asks the hosting [android.app.Activity] to show the live times instead.
 *
 * This [Fragment] shows a content menu to the user when NOT in the create shortcut mode if they
 * long press on a favourite item, allowing them to perform operations such as edit or delete the
 * stop, among other things.
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class FavouriteStopsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: FavouriteStopsFragmentViewModelFactory
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private val viewModel: FavouriteStopsFragmentViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this)
    }

    private var callbacks: Callbacks? = null
    private var createShortcutCallbacks: CreateShortcutCallbacks? = null
    private lateinit var adapter: FavouriteStopsAdapter

    private var _viewBinding: FavouritestopsBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var actionMode: ActionMode? = null

    private var amMenuItemProxAlert: MenuItem? = null
    private var amMenuItemTimeAlert: MenuItem? = null
    private var amMenuItemShowOnMap: MenuItem? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = context as? Callbacks
        createShortcutCallbacks = context as? CreateShortcutCallbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        val isCreateShortcutMode = createShortcutCallbacks != null
        viewModel.isCreateShortcutMode = isCreateShortcutMode

        adapter = FavouriteStopsAdapter(
                requireContext(),
                favouriteItemClickListener,
                isCreateShortcutMode)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _viewBinding = FavouritestopsBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().apply {
            if (viewModel.isCreateShortcutMode) {
                setTitle(R.string.favouriteshortcut_title)
            } else {
                setTitle(R.string.favouritestops_title)
            }
        }

        viewBinding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@FavouriteStopsFragment.adapter
        }

        val lifecycle = viewLifecycleOwner
        viewModel.uiStateLiveData.observe(lifecycle, this::handleUiState)
        viewModel.favouritesLiveData.observe(lifecycle, adapter::submitList)
        viewModel.showContextMenuLiveData.observe(lifecycle, this::handleShowContextMenu)
        viewModel.selectedStopNameLiveData.observe(lifecycle, this::handleSelectedStopName)
        viewModel.isStopMapVisibleLiveData.observe(lifecycle, this::handleIsShowOnMapVisible)
        viewModel.isArrivalAlertVisibleLiveData.observe(lifecycle,
                this::handleIsArrivalAlertVisible)
        viewModel.isProximityAlertVisibleLiveData.observe(lifecycle,
                this::handleIsProximityAlertVisible)
        viewModel.isArrivalAlertEnabledLiveData.observe(lifecycle,
                this::handleIsArrivalAlertEnabled)
        viewModel.isProximityAlertEnabledLiveData.observe(lifecycle,
                this::handleIsProximityAlertEnabled)
        viewModel.hasArrivalAlertLiveData.observe(lifecycle, this::handleHasArrivalAlert)
        viewModel.hasProximityAlertLiveData.observe(lifecycle, this::handleHasProximityAlert)
        viewModel.showStopDataLiveData.observe(lifecycle, this::handleShowStopData)
        viewModel.createShortcutLiveData.observe(lifecycle, this::handleCreateShortcut)
        viewModel.showEditFavouriteStopLiveData.observe(lifecycle,
                this::handleShowEditFavouriteStop)
        viewModel.showConfirmDeleteFavouriteLiveData.observe(lifecycle,
                this::handleShowConfirmDeleteFavouriteStop)
        viewModel.showOnMapLiveData.observe(lifecycle, this::handleShowOnMap)
        viewModel.showAddArrivalAlertLiveData.observe(lifecycle, this::handleAddArrivalAlert)
        viewModel.showConfirmDeleteArrivalAlertLiveData.observe(lifecycle,
                this::handleConfirmDeleteArrivalAlert)
        viewModel.showAddProximityAlertLiveData.observe(lifecycle, this::handleAddProximityAlert)
        viewModel.showConfirmDeleteProximityAlertLiveData.observe(lifecycle,
                this::handleConfirmDeleteProximityAlert)
    }

    /**
     * Handle a new [UiState] by showing the correct top-level layout.
     *
     * @param state The new [UiState].
     */
    private fun handleUiState(state: UiState) {
        viewBinding.apply {
            when (state) {
                UiState.PROGRESS -> contentView.showProgressLayout()
                UiState.CONTENT -> contentView.showContentLayout()
                UiState.ERROR -> {
                    txtError.setText(R.string.favouritestops_nosavedstops)
                    contentView.showErrorLayout()
                }
            }
        }
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
    private fun handleSelectedStopName(name: UiFavouriteName?) {
        actionMode?.title = name?.let {
            textFormattingUtils.formatBusStopNameWithStopCode(it.stopCode, it.stopName)
        }
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
     * Handle the state of the arrival alert in the context menu.
     *
     * @param hasArrivalAlert Is there an arrival alert for the currently selected stop?
     */
    private fun handleHasArrivalAlert(hasArrivalAlert: Boolean) {
        amMenuItemTimeAlert?.apply {
            if (hasArrivalAlert) {
                setTitle(R.string.favouritestops_menu_time_rem)
                setIcon(R.drawable.ic_action_alarm_off)
            } else {
                setTitle(R.string.favouritestops_menu_time_add)
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
                setTitle(R.string.favouritestops_menu_prox_rem)
                setIcon(R.drawable.ic_action_location_off)
            } else {
                setTitle(R.string.favouritestops_menu_prox_add)
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
        callbacks?.onShowBusTimes(stopCode)
    }

    /**
     * Create a shortcut for the given [FavouriteStop].
     *
     * @param favouriteStop The favourite stop to create a shortcut for.
     */
    private fun handleCreateShortcut(favouriteStop: FavouriteStop) {
        createShortcutCallbacks?.onCreateShortcut(favouriteStop.stopCode, favouriteStop.stopName)
    }

    /**
     * Present UI to allow the user to edit the given stop.
     *
     * @param stopCode The favourite stop to edit.
     */
    private fun handleShowEditFavouriteStop(stopCode: String) {
        callbacks?.onShowAddEditFavouriteStop(stopCode)
    }

    /**
     * Present UI to ask the user to confirm if they wish to delete the given favourite stop.
     *
     * @param stopCode The stop to confirm deletion for.
     */
    private fun handleShowConfirmDeleteFavouriteStop(stopCode: String) {
        callbacks?.onShowConfirmFavouriteDeletion(stopCode)
    }

    /**
     * Show the given stop on the map.
     *
     * @param stopCode The stop to show on the map.
     */
    private fun handleShowOnMap(stopCode: String) {
        callbacks?.onShowBusStopMapWithStopCode(stopCode)
    }

    /**
     * Present UI to the user to allow them to add an arrival alert for the given stop.
     *
     * @param stopCode The stop to add an arrival alert for.
     */
    private fun handleAddArrivalAlert(stopCode: String) {
        callbacks?.onShowAddTimeAlert(stopCode, null)
    }

    /**
     * Present UI to the user to allow them to confirm if the arrival alert for the given stop
     * should be removed.
     *
     * @param stopCode The stop to confirm arrival alert deletion for.
     */
    private fun handleConfirmDeleteArrivalAlert(stopCode: String) {
        callbacks?.onShowConfirmDeleteTimeAlert(stopCode)
    }

    /**
     * Present UI to the user to allow them to add a proximity alert for the given stop.
     *
     * @param stopCode The stop to add a proximity alert for.
     */
    private fun handleAddProximityAlert(stopCode: String) {
        callbacks?.onShowAddProximityAlert(stopCode)
    }

    /**
     * Present UI to the user to allow them to confirm if the proximity alert for the given stop
     * should be removed.
     *
     * @param stopCode The stop to confirm proximity alert deletion for.
     */
    private fun handleConfirmDeleteProximityAlert(stopCode: String) {
        callbacks?.onShowConfirmDeleteProximityAlert(stopCode)
    }

    private val favouriteItemClickListener = object : OnFavouriteItemClickListener {
        override fun onFavouriteClicked(favouriteStop: FavouriteStop) {
            viewModel.onFavouriteStopClicked(favouriteStop)
        }

        override fun onFavouriteLongClicked(stopCode: String) =
                viewModel.onFavouriteStopLongClicked(stopCode)
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.favouritestops_context_menu, menu)
            amMenuItemProxAlert = menu.findItem(R.id.favouritestops_context_menu_prox_alert)
            amMenuItemTimeAlert = menu.findItem(R.id.favouritestops_context_menu_time_alert)
            amMenuItemShowOnMap = menu.findItem(R.id.favouritestops_context_menu_showonmap)

            handleSelectedStopName(viewModel.selectedStopNameLiveData.value)

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            handleIsArrivalAlertVisible(viewModel.isArrivalAlertVisibleLiveData.value ?: false)
            handleIsProximityAlertVisible(viewModel.isProximityAlertVisibleLiveData.value ?: false)
            handleIsShowOnMapVisible(viewModel.isStopMapVisibleLiveData.value ?: false)

            handleIsArrivalAlertEnabled(viewModel.isArrivalAlertEnabledLiveData.value ?: false)
            handleIsProximityAlertEnabled(viewModel.isProximityAlertEnabledLiveData.value ?: false)

            handleHasArrivalAlert(viewModel.hasArrivalAlertLiveData.value ?: false)
            handleHasProximityAlert(viewModel.hasProximityAlertLiveData.value ?: false)

            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = when (item.itemId) {
            R.id.favouritestops_context_menu_modify -> viewModel.onEditFavouriteClicked()
            R.id.favouritestops_context_menu_delete -> viewModel.onDeleteFavouriteClicked()
            R.id.favouritestops_context_menu_showonmap -> viewModel.onShowOnMapClicked()
            R.id.favouritestops_context_menu_prox_alert -> viewModel.onProximityAlertClicked()
            R.id.favouritestops_context_menu_time_alert -> viewModel.onArrivalAlertClicked()
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            amMenuItemProxAlert = null
            amMenuItemTimeAlert = null
            amMenuItemShowOnMap = null
            actionMode = null
            viewModel.onFavouriteStopUnselected()
        }
    }

    /**
     * Activities which host this [Fragment] in the normal (NOT create shortcut) mode should
     * implement this interface.
     */
    interface Callbacks :
            OnShowAddEditFavouriteStopListener,
            OnShowConfirmFavouriteDeletionListener,
            OnShowConfirmDeleteProximityAlertListener,
            OnShowConfirmDeleteTimeAlertListener,
            OnShowAddProximityAlertListener,
            OnShowAddTimeAlertListener,
            OnShowBusStopMapWithStopCodeListener,
            OnShowBusTimesListener

    /**
     * Activities which host this [Fragment] in the create shortcut mode should implement this
     * interface. When this is the case, this [Fragment] will run in create shortcut mode, and pass
     * create shortcut events through this interface back to the [android.app.Activity] when the
     * user has selected a stop.
     */
    interface CreateShortcutCallbacks {

        /**
         * This is called when the user has selected a stop and a shortcut should be created for it.
         *
         * @param stopCode The stop code to create a shortcut for.
         * @param stopName The user's name for the stop at the time they requested the shortcut be
         * created.
         */
        fun onCreateShortcut(stopCode: String, stopName: String)
    }
}