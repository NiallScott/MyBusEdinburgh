/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.utils.Event
import uk.org.rivernile.android.bustracker.viewmodel.GenericSavedStateViewModelFactory
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.BustimesFragmentBinding
import javax.inject.Inject

/**
 * This [Fragment] shows bus times to the user in an expandable list.
 *
 * @author Niall Scott
 */
class BusTimesFragment : Fragment() {

    companion object {

        private const val EXTRA_STOP_CODE = "stopCode"

        /**
         * Creates a new instance of this [BusTimesFragment].
         *
         * @param stopCode The stop code to show bus times for.
         * @return A new instance of this [Fragment].
         */
        fun newInstance(stopCode: String?) =
                BusTimesFragment().apply {
                    arguments = Bundle().apply {
                        putString(EXTRA_STOP_CODE, stopCode)
                    }
                }
    }

    @Inject
    lateinit var viewModelFactory: BusTimesFragmentViewModelFactory
    @Inject
    lateinit var viewHolderFieldPopulator: ViewHolderFieldPopulator

    private val viewModel: BusTimesFragmentViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this)
    }
    private lateinit var adapter: LiveTimesAdapter

    private var _viewBinding: BustimesFragmentBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var errorSnackbar: Snackbar? = null

    private var menuItemRefresh: MenuItem? = null
    private var menuItemSort: MenuItem? = null
    private var menuItemAutoRefresh: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewModel.stopCode = arguments?.getString(EXTRA_STOP_CODE)

        adapter = LiveTimesAdapter(requireContext(), viewHolderFieldPopulator,
                viewModel::onParentItemClicked)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _viewBinding = BustimesFragmentBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

            swipeRefreshLayout.apply {
                setColorSchemeColors(MaterialColors.getColor(this, R.attr.colorPrimary))
                setOnRefreshListener {
                    viewModel.onSwipeToRefresh()
                }
            }
        }

        val viewLifecycle = viewLifecycleOwner
        viewModel.hasConnectivityLiveData.observe(viewLifecycle, this::handleConnectivityChange)
        viewModel.isSortedByTimeLiveData.observe(viewLifecycle,
                this::setSortedByTimeActionItemState)
        viewModel.isAutoRefreshLiveData.observe(viewLifecycle, this::setAutoRefreshActionItemState)
        viewModel.showProgressLiveData.observe(viewLifecycle, this::handleShowProgress)
        viewModel.errorLiveData.observe(viewLifecycle, this::handleError)
        viewModel.liveTimesLiveData.observe(viewLifecycle, adapter::submitList)
        viewModel.uiStateLiveData.observe(viewLifecycle, this::handleUiStateChanged)
        viewModel.errorWithContentLiveData.observe(viewLifecycle, this::handleErrorWithContent)
        viewModel.lastRefreshLiveData.observe(viewLifecycle, this::handleLastRefreshUpdated)
        viewModel.refreshLiveData.observe(viewLifecycle) { /* Nothing in here. */  }

        requireActivity().addMenuProvider(menuProvider, viewLifecycle, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    /**
     * Handle a change in top-level UI state. This displays either the empty progress view, the
     * empty error view, or the content view.
     *
     * @param uiState The new [UiState] to show. If this is `null`, then the progress view will be
     * shown, as this is the default state of the screen.
     */
    private fun handleUiStateChanged(uiState: UiState?) {
        // When transiting UI state, dismiss the existing error Snackbar.
        dismissErrorSnackbar()

        viewBinding.contentView.apply {
            when (uiState) {
                UiState.CONTENT -> showContentLayout()
                UiState.ERROR -> showErrorLayout()
                else -> showProgressLayout()
            }
        }
    }

    /**
     * Handle the progress state of the UI. This does not affect the top level progress state.
     * Namely, this affects the swipe-to-refresh layout and the refresh [MenuItem].
     *
     * @param showProgress Should progress be shown? If this is `null`, then the refresh [MenuItem]
     * and swipe-to-refresh layouts will be disabled.
     */
    private fun handleShowProgress(showProgress: Boolean?) {
        setRefreshActionItemLoadingState(showProgress)

        viewBinding.swipeRefreshLayout.apply {
            showProgress?.also {
                isEnabled = true
                isRefreshing = it
            } ?: run {
                isEnabled = false
                isRefreshing = false
            }
        }
    }

    /**
     * This handles an error by setting the error in the in-line UI. This is always called when an
     * error occurs, even if the error UI isn't currently showing.
     *
     * @param error The error to display.
     */
    private fun handleError(error: ErrorType?) {
        mapErrorToStringResource(error).let(viewBinding.layoutError.txtError.txtError::setText)
    }

    /**
     * This is called when an error occurs while live times are currently being shown. This is
     * handled differently from [handleError] because in this scenario, we display the error as a
     * [Snackbar] rather than in-line UI.
     *
     * @param event The [Event] containing the error information.
     */
    private fun handleErrorWithContent(event: Event<ErrorType>?) {
        dismissErrorSnackbar()

        event?.getContentIfNotHandled()?.let {
            errorSnackbar = Snackbar.make(viewBinding.root, mapErrorToStringResource(it),
                    Snackbar.LENGTH_LONG).apply {
                addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(snackbar: Snackbar, event: Int) {
                        errorSnackbar = null
                    }
                })

                show()
            }
        }
    }

    /**
     * Handle a change in the device's connectivity, to change the state of the "no connectivity"
     * icon.
     *
     * @param hasConnectivity Does the device have internet connectivity?
     */
    private fun handleConnectivityChange(hasConnectivity: Boolean) {
        val icon = if (hasConnectivity) 0 else R.drawable.ic_cloud_off
        viewBinding.txtLastRefresh.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
    }

    /**
     * Handle an update to the last refresh time.
     *
     * @param lastRefresh The last refresh time data.
     */
    private fun handleLastRefreshUpdated(lastRefresh: LastRefreshTime) {
        when (lastRefresh) {
            is LastRefreshTime.Never -> getString(R.string.times_never)
            is LastRefreshTime.MoreThanOneHour -> getString(R.string.times_greaterthanhour)
            is LastRefreshTime.Now -> getString(R.string.times_lessthanoneminago)
            is LastRefreshTime.Minutes -> {
                val minutes = lastRefresh.minutes
                resources.getQuantityString(R.plurals.times_minsago, minutes, minutes)
            }
        }.let {
            viewBinding.txtLastRefresh.text = getString(R.string.bustimes_lastupdated, it)
        }
    }

    /**
     * Update the state of the refresh [MenuItem].
     *
     * @param showProgress Should progress be shown? If this value is `null`, then the [MenuItem]
     * will be disabled.
     */
    private fun setRefreshActionItemLoadingState(showProgress: Boolean?) {
        menuItemRefresh?.apply {
            showProgress?.let {
                if (it) {
                    setActionView(R.layout.actionbar_indeterminate_progress)
                    isEnabled = false
                } else {
                    actionView = null
                    isEnabled = true
                }
            } ?: run {
                actionView = null
                isEnabled = false
            }
        }
    }

    /**
     * Update the state of the sorted by time/service [MenuItem].
     *
     * @param isSortedByTime The current sort by time/service state. `null` means the [MenuItem]
     * will be disabled.
     */
    private fun setSortedByTimeActionItemState(isSortedByTime: Boolean?) {
        menuItemSort?.apply {
            isEnabled = isSortedByTime != null

            if (isSortedByTime == true) {
                setTitle(R.string.bustimes_menu_sort_service)
                        .setIcon(R.drawable.ic_action_sort_by_size)
            } else {
                setTitle(R.string.bustimes_menu_sort_times)
                        .setIcon(R.drawable.ic_action_time)
            }
        }
    }

    /**
     * Update the state of the auto refresh [MenuItem].
     *
     * @param autoRefreshEnabled The current auto refresh state. `null` means the [MenuItem] will be
     * disabled.
     */
    private fun setAutoRefreshActionItemState(autoRefreshEnabled: Boolean?) {
        menuItemAutoRefresh?.apply {
            isEnabled = autoRefreshEnabled != null

            if (autoRefreshEnabled == true) {
                setTitle(R.string.bustimes_menu_turnautorefreshoff)
            } else {
                setTitle(R.string.bustimes_menu_turnautorefreshon)
            }
        }
    }

    /**
     * Given an [ErrorType], return the [String] resource for this error. If [ErrorType] is `null`
     * or is of a value not recognised by this method, a default error message will be used.
     *
     * @param error The error to return a [String] resource for.
     * @return A [String] resource ID for the given [error].
     */
    @StringRes
    private fun mapErrorToStringResource(error: ErrorType?) = when (error) {
        ErrorType.NO_STOP_CODE -> R.string.bustimes_err_nocode
        ErrorType.NO_CONNECTIVITY -> R.string.bustimes_err_noconn
        ErrorType.COMMUNICATION -> R.string.bustimes_err_connection_issue
        ErrorType.UNKNOWN_HOST -> R.string.bustimes_err_noresolv
        ErrorType.SERVER_ERROR -> R.string.bustimes_err_api_processing_error
        ErrorType.NO_DATA -> R.string.bustimes_err_nodata
        ErrorType.AUTHENTICATION -> R.string.bustimes_err_api_invalid_key
        ErrorType.SYSTEM_OVERLOADED -> R.string.bustimes_err_api_system_overloaded
        ErrorType.DOWN_FOR_MAINTENANCE -> R.string.bustimes_err_api_system_maintenance
        else -> R.string.bustimes_err_unknown
    }

    /**
     * If there is an error [Snackbar] currently showing, dismiss it and `null` out its reference.
     */
    private fun dismissErrorSnackbar() {
        errorSnackbar?.dismiss()
        errorSnackbar = null
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.bustimes_option_menu, menu)

            menuItemRefresh = menu.findItem(R.id.bustimes_option_menu_refresh)
            menuItemSort = menu.findItem(R.id.bustimes_option_menu_sort)
            menuItemAutoRefresh = menu.findItem(R.id.bustimes_option_menu_autorefresh)
        }

        override fun onPrepareMenu(menu: Menu) {
            setRefreshActionItemLoadingState(viewModel.showProgressLiveData.value)
            setSortedByTimeActionItemState(viewModel.isSortedByTimeLiveData.value)
            setAutoRefreshActionItemState(viewModel.isAutoRefreshLiveData.value)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
            R.id.bustimes_option_menu_refresh -> {
                viewModel.onRefreshMenuItemClicked()
                true
            }
            R.id.bustimes_option_menu_sort -> {
                viewModel.onSortMenuItemClicked()
                true
            }
            R.id.bustimes_option_menu_autorefresh -> {
                viewModel.onAutoRefreshMenuItemClicked()
                true
            }
            else -> false
        }
    }
}