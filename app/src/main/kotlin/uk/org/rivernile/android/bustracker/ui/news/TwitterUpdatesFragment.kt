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

package uk.org.rivernile.android.bustracker.ui.news

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.ui.scroll.HasScrollableContent
import uk.org.rivernile.android.bustracker.utils.Event
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.FragmentTwitterUpdatesBinding
import javax.inject.Inject

/**
 * This [Fragment] shows a [List] of [Tweet]s to the user, which gives them live traffic updates
 * from the relevant authorities.
 *
 * @author Niall Scott
 */
class TwitterUpdatesFragment : Fragment(), HasScrollableContent {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var avatarImageLoader: TweetAvatarImageLoader

    private val viewModel: TwitterUpdatesFragmentViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: TweetAdapter

    private var _viewBinding: FragmentTwitterUpdatesBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var refreshMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        adapter = TweetAdapter(requireContext(), avatarImageLoader, this::handleItemClicked)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return FragmentTwitterUpdatesBinding.inflate(inflater, container, false).also {
            _viewBinding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            recyclerView.apply {
                setHasFixedSize(true)
                addItemDecoration(NewsItemDecoration(requireContext(),
                        resources.getDimensionPixelSize(R.dimen.news_divider_inset_start)))
                adapter = this@TwitterUpdatesFragment.adapter
            }

            swipeRefreshLayout.setColorSchemeColors(
                    MaterialColors.getColor(swipeRefreshLayout, R.attr.colorPrimary))
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onSwipeToRefresh()
            }
        }

        val lifecycle = viewLifecycleOwner
        viewModel.tweetsLiveData.observe(lifecycle, adapter::submitList)
        viewModel.uiStateLiveData.observe(lifecycle, this::handleUiStateChanged)
        viewModel.errorLiveData.observe(lifecycle, this::handleError)
        viewModel.snackbarErrorLiveData.observe(lifecycle, this::handleSnackbarError)
        viewModel.isRefreshMenuItemEnabledLiveData.observe(lifecycle,
                this::handleIsRefreshMenuItemEnabled)
        viewModel.isRefreshMenuItemRefreshingLiveData.observe(lifecycle,
                this::handleIsRefreshMenuItemRefreshing)
        viewModel.isSwipeToRefreshRefreshingLiveData.observe(lifecycle) {
            viewBinding.swipeRefreshLayout.isRefreshing = it
        }

        requireActivity().addMenuProvider(menuProvider, lifecycle)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    override val scrollableContentIdRes get() = R.id.recyclerView

    /**
     * Handle a change in [UiState].
     *
     * @param uiState The new [UiState] to apply.
     */
    private fun handleUiStateChanged(uiState: UiState) {
        viewBinding.contentView.apply {
            when (uiState) {
                UiState.PROGRESS -> showProgressLayout()
                UiState.CONTENT -> showContentLayout()
                UiState.ERROR -> showErrorLayout()
            }
        }
    }

    /**
     * Handle a new error state.
     *
     * @param error The [Error] to handle.
     */
    private fun handleError(error: Error?) {
        viewBinding.txtError.apply {
            error?.let {
                setText(mapToErrorString(it))
                setCompoundDrawablesWithIntrinsicBounds(0, mapToErrorDrawable(it), 0, 0)
            } ?: run {
                text = null
            }
        }
    }

    /**
     * Handle a new snackbar error.
     *
     * @param event The event.
     */
    private fun handleSnackbarError(event: Event<Error>) {
        event.getContentIfNotHandled()?.let {
            Snackbar.make(viewBinding.root, mapToErrorString(it), Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    /**
     * Handle the refresh menu item enabled state changing.
     *
     * @param isEnabled Is the refresh menu item enabled?
     */
    private fun handleIsRefreshMenuItemEnabled(isEnabled: Boolean) {
        refreshMenuItem?.isEnabled = isEnabled
    }

    /**
     * Handle the refresh menu item refresh state changing.
     *
     * @param isRefreshing Is the refresh menu item refreshing?
     */
    private fun handleIsRefreshMenuItemRefreshing(isRefreshing: Boolean) {
        refreshMenuItem?.apply {
            if (isRefreshing) {
                setActionView(R.layout.actionbar_indeterminate_progress)
            } else {
                actionView = null
            }
        }
    }

    /**
     * Given an [Error], map it to the relevant [String] resource.
     *
     * @param error The [Error] to map.
     * @return The [String] resource for the given [Error].
     */
    @StringRes
    private fun mapToErrorString(error: Error) = when (error) {
        Error.NO_CONNECTIVITY -> R.string.twitterupdates_err_no_connectivity
        Error.NO_DATA -> R.string.twitterupdates_err_nodata
        Error.COMMUNICATION -> R.string.twitterupdates_err_connection
        Error.SERVER -> R.string.twitterupdates_err_server
    }

    /**
     * Given an [Error], map it to the relevent drawable resource.
     *
     * @param error The [Error] to map.
     * @return The drawable resource for the given [Error].
     */
    @DrawableRes
    private fun mapToErrorDrawable(error: Error) = when (error) {
        Error.NO_CONNECTIVITY -> R.drawable.ic_error_cloud_off
        Error.NO_DATA -> R.drawable.ic_error_newspaper
        else -> R.drawable.ic_error_generic
    }

    /**
     * Handle the avatar in an item being clicked.
     *
     * @param tweet The [Tweet] which was clicked.
     */
    private fun handleItemClicked(tweet: Tweet) {
        val intent = Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(tweet.profileUrl))

        try {
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(
                    requireContext(),
                    R.string.twitterupdates_err_no_activity_for_click,
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.twitterupdates_option_menu, menu)
            refreshMenuItem = menu.findItem(R.id.twitterupdates_option_menu_refresh)
        }

        override fun onPrepareMenu(menu: Menu) {
            handleIsRefreshMenuItemEnabled(
                    viewModel.isRefreshMenuItemEnabledLiveData.value ?: false)
            handleIsRefreshMenuItemRefreshing(
                    viewModel.isRefreshMenuItemRefreshingLiveData.value ?: false)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
            R.id.twitterupdates_option_menu_refresh -> {
                viewModel.onRefreshMenuItemClicked()
                true
            }
            else -> false
        }
    }
}