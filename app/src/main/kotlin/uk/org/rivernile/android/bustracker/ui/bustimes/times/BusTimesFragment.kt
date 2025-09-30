/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.time.ElapsedTimeMinutes
import uk.org.rivernile.android.bustracker.utils.Event
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.FragmentBusTimesBinding
import javax.inject.Inject

/**
 * This [Fragment] shows bus times to the user in an expandable list.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class BusTimesFragment : Fragment() {

    companion object {

        /**
         * Creates a new instance of this [BusTimesFragment].
         *
         * @param stopCode The stop code to show bus times for.
         * @return A new instance of this [Fragment].
         */
        fun newInstance(stopCode: String?) = BusTimesFragment().apply {
            arguments = Bundle().apply {
                putString(BusTimesFragmentViewModel.STATE_STOP_CODE, stopCode)
            }
        }
    }

    @Inject
    lateinit var viewHolderFieldPopulator: ViewHolderFieldPopulator

    private val viewModel by viewModels<BusTimesFragmentViewModel>()

    private lateinit var adapter: LiveTimesAdapter

    private var _viewBinding: FragmentBusTimesBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var errorSnackbar: Snackbar? = null

    private var menuItemRefresh: MenuItem? = null
    private var menuItemSort: MenuItem? = null
    private var menuItemAutoRefresh: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = LiveTimesAdapter(requireContext(), viewHolderFieldPopulator,
                viewModel::onParentItemClicked)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        return FragmentBusTimesBinding.inflate(inflater, container, false).also {
            _viewBinding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

            swipeRefreshLayout.apply {
                setColorSchemeColors(
                    MaterialColors.getColor(
                        this,
                        android.R.attr.colorPrimary
                    )
                )
                setProgressBackgroundColorSchemeColor(
                    MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorSurfaceContainerHigh
                    )
                )
                setOnRefreshListener {
                    viewModel.onSwipeToRefresh()
                }
            }
        }

        val viewLifecycle = viewLifecycleOwner
        viewModel.hasConnectivityLiveData.observe(viewLifecycle, this::handleConnectivityChange)
        viewModel.isSortedByTimeLiveData.observe(viewLifecycle, this::handleSortedByTimeChanged)
        viewModel.isSortedByTimeEnabledLiveData.observe(viewLifecycle,
            this::handleSortedByTimeEnabledChanged)
        viewModel.isAutoRefreshLiveData.observe(viewLifecycle, this::handleAutoRefreshChanged)
        viewModel.isAutoRefreshEnabledLiveData.observe(viewLifecycle,
            this::handleAutoRefreshEnabledChanged)
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
        viewBinding.txtError.apply {
            setText(mapErrorToStringResource(error))
            setCompoundDrawablesWithIntrinsicBounds(0, mapErrorToDrawableResource(error), 0, 0)
        }
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

        event?.getContentIfNotHandled()?.let { errorType ->
            // There have been some crashes caused by this function being called when our layout is
            // not yet attached to the parent yet. Snackbar looks for a parent View to attach to,
            // and will throw an Exception if it cannot find a parent. So in this case, if we have
            // a parent, we attempt to show the Snackbar normally. If we do not have a parent, we
            // post to the root View's Handler in the hope that we'll have attached by the time the
            // enqueued Runnable is executed.
            viewBinding.root.let { rootView ->
                if (rootView.parent != null) {
                    showErrorSnackbar(rootView, errorType)
                } else {
                    // Defer execution by posting to the end of the run queue.
                    rootView.handler.post {
                        // Perform a dismiss again incase another Snackbar was already posted.
                        dismissErrorSnackbar()
                        showErrorSnackbar(rootView, errorType)
                    }
                }
            }
        }
    }

    /**
     * Show the error [Snackbar] to the user.
     *
     * @param view The [View] to attach the [Snackbar] to.
     * @param errorType The [ErrorType] to display.
     */
    private fun showErrorSnackbar(
        view: View,
        errorType: ErrorType) {
        if (view.parent != null) {
            errorSnackbar = Snackbar.make(
                view,
                mapErrorToStringResource(errorType),
                Snackbar.LENGTH_LONG)
                .apply {
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
    private fun handleLastRefreshUpdated(lastRefresh: ElapsedTimeMinutes) {
        when (lastRefresh) {
            is ElapsedTimeMinutes.None -> getString(R.string.times_never)
            is ElapsedTimeMinutes.MoreThanOneHour -> getString(R.string.times_greaterthanhour)
            is ElapsedTimeMinutes.Now -> getString(R.string.times_lessthanoneminago)
            is ElapsedTimeMinutes.Minutes -> {
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
     * @param isSortedByTime The current sort by time/service state.
     */
    private fun handleSortedByTimeChanged(isSortedByTime: Boolean) {
        menuItemSort?.apply {
            if (isSortedByTime) {
                setTitle(R.string.bustimes_menu_sort_service)
                setIcon(R.drawable.ic_action_sort_by_size)
            } else {
                setTitle(R.string.bustimes_menu_sort_times)
                setIcon(R.drawable.ic_action_time)
            }
        }
    }

    /**
     * Handle the enabled state changing of the sorted by time option.
     *
     * @param isEnabled Is the sorted by time option enabled?
     */
    private fun handleSortedByTimeEnabledChanged(isEnabled: Boolean) {
        menuItemSort?.isEnabled = isEnabled
    }

    /**
     * Update the state of the auto refresh [MenuItem].
     *
     * @param autoRefresh The current auto refresh state.
     */
    private fun handleAutoRefreshChanged(autoRefresh: Boolean) {
        menuItemAutoRefresh?.isChecked = autoRefresh
    }

    /**
     * Handle the enabled state changing of the auto refresh option.
     *
     * @param isEnabled Is the auto refresh option enabled?
     */
    private fun handleAutoRefreshEnabledChanged(isEnabled: Boolean) {
        menuItemAutoRefresh?.isEnabled = isEnabled
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
     * Given an [ErrorType] return the drawable resource for this error.
     *
     * @param error The error to return a drawable resource for.
     * @return A drawable resource ID for the given [error].
     */
    @DrawableRes
    private fun mapErrorToDrawableResource(error: ErrorType?) = when (error) {
        ErrorType.NO_CONNECTIVITY -> R.drawable.ic_error_cloud_off
        ErrorType.NO_DATA -> R.drawable.ic_error_directions_bus
        else -> R.drawable.ic_error_generic
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
            handleSortedByTimeChanged(viewModel.isSortedByTimeLiveData.value ?: false)
            handleSortedByTimeEnabledChanged(viewModel.isSortedByTimeEnabledLiveData.value ?: false)
            handleAutoRefreshChanged(viewModel.isAutoRefreshLiveData.value ?: false)
            handleAutoRefreshEnabledChanged(viewModel.isAutoRefreshEnabledLiveData.value ?: false)
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
