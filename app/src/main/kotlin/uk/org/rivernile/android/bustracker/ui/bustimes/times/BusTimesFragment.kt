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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.bustimes_fragment.contentView
import kotlinx.android.synthetic.main.bustimes_fragment.layoutContent
import kotlinx.android.synthetic.main.bustimes_fragment.recyclerView
import kotlinx.android.synthetic.main.bustimes_fragment.swipeRefreshLayout
import kotlinx.android.synthetic.main.bustimes_fragment.txtLastRefresh
import kotlinx.android.synthetic.main.error.txtError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.utils.Event
import uk.org.rivernile.android.bustracker.viewmodel.GenericSavedStateViewModelFactory
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [Fragment] shows bus times to the user in an expandable list.
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
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

    private lateinit var viewModel: BusTimesFragmentViewModel
    private lateinit var adapter: LiveTimesAdapter

    private var errorSnackbar: Snackbar? = null

    private var menuItemRefresh: MenuItem? = null
    private var menuItemSort: MenuItem? = null
    private var menuItemAutoRefresh: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        val savedStateViewModelFactory = GenericSavedStateViewModelFactory(viewModelFactory, this)
        viewModel = ViewModelProvider(this, savedStateViewModelFactory)
                .get(BusTimesFragmentViewModel::class.java)
        viewModel.stopCode = arguments?.getString(EXTRA_STOP_CODE)

        adapter = LiveTimesAdapter(requireContext(), viewHolderFieldPopulator,
                viewModel::onParentItemClicked)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.bustimes_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener {
                viewModel.onSwipeToRefresh()
            }
        }

        val viewLifecycle = viewLifecycleOwner
        viewModel.hasConnectivityLiveData.observe(viewLifecycle,
                Observer(this::handleConnectivityChange))
        viewModel.isSortedByTimeLiveData.observe(viewLifecycle,
                Observer(this::setSortedByTimeActionItemState))
        viewModel.showProgressLiveData.observe(viewLifecycle, Observer(this::handleShowProgress))
        viewModel.errorLiveData.observe(viewLifecycle, Observer(this::handleError))
        viewModel.liveTimesLiveData.observe(viewLifecycle, Observer(adapter::submitList))
        viewModel.uiStateLiveState.observe(viewLifecycle, Observer(this::handleUiStateChanged))
        viewModel.errorWithContentLiveData.observe(viewLifecycleOwner,
                Observer(this::handleErrorWithContent))
        viewModel.lastRefreshLiveData.observe(viewLifecycle,
                Observer(this::handleLastRefreshUpdated))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bustimes_option_menu, menu)

        menuItemRefresh = menu.findItem(R.id.bustimes_option_menu_refresh)
        menuItemSort = menu.findItem(R.id.bustimes_option_menu_sort)
        menuItemAutoRefresh = menu.findItem(R.id.bustimes_option_menu_autorefresh)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        setRefreshActionItemLoadingState(viewModel.showProgressLiveData.value)
        setSortedByTimeActionItemState(viewModel.isSortedByTimeLiveData.value)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        else -> super.onOptionsItemSelected(item)
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

        when (uiState) {
            UiState.CONTENT -> contentView.showContentLayout()
            UiState.ERROR -> contentView.showErrorLayout()
            else -> contentView.showProgressLayout()
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

        showProgress?.also {
            swipeRefreshLayout.isEnabled = true
            swipeRefreshLayout.isRefreshing = it
        } ?: run {
            swipeRefreshLayout.isEnabled = false
            swipeRefreshLayout.isRefreshing = false
        }
    }

    /**
     * This handles an error by setting the error in the in-line UI. This is always called when an
     * error occurs, even if the error UI isn't currently showing.
     *
     * @param error The error to display.
     */
    private fun handleError(error: ErrorType?) {
        mapErrorToStringResource(error).let(txtError::setText)
    }

    /**
     * This is called when an error occurs while live times are currently being shown. This is
     * handled differently from [handleError] because in this scenario, we display the error as a
     * [Snackbar] rather than in-line UI.
     *
     * @param event The [Event] containing the error information.
     */
    private fun handleErrorWithContent(event: Event<ErrorType>?) {
        event?.getContentIfNotHandled()?.let {
            dismissErrorSnackbar()
            errorSnackbar = Snackbar.make(layoutContent, mapErrorToStringResource(it),
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
        txtLastRefresh.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
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
            txtLastRefresh.text = getString(R.string.bustimes_lastupdated, it)
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
     * @param isSortedByTime THe current sort by time/service state. `null` means the [MenuItem]
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
}