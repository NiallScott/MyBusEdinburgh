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
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.twitterupdates.contentView
import kotlinx.android.synthetic.main.twitterupdates.recyclerView
import kotlinx.android.synthetic.main.twitterupdates.swipeRefreshLayout
import kotlinx.android.synthetic.main.twitterupdates.txtError
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [Fragment] shows a [List] of [Tweet]s to the user, which gives them live traffic updates
 * from the relevant authorities.
 *
 * @author Niall Scott
 */
class TwitterUpdatesFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var avatarImageLoader: TweetAvatarImageLoader

    private lateinit var viewModel: TwitterUpdatesFragmentViewModel
    private lateinit var adapter: TweetAdapter

    private var refreshMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(TwitterUpdatesFragmentViewModel::class.java)
        adapter = TweetAdapter(requireContext(), avatarImageLoader, this::handleItemClicked)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.twitterupdates, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(NewsItemDecoration(requireContext(),
                    resources.getDimensionPixelSize(R.dimen.news_divider_inset_start)))
            adapter = this@TwitterUpdatesFragment.adapter
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.onSwipeToRefresh()
        }

        val lifecycle = viewLifecycleOwner
        viewModel.uiStateLiveData.observe(lifecycle, Observer(this::handleUiStateChanged))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().setTitle(R.string.twitterupdates_title)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.twitterupdates_option_menu, menu)
        refreshMenuItem = menu.findItem(R.id.twitterupdates_option_menu_refresh)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        setRefreshActionItemLoadingState(viewModel.uiStateLiveData.value)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.twitterupdates_option_menu_refresh -> {
            viewModel.onRefreshMenuItemClicked()

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Handle a change in [UiState]. This propagates the change to the UI components held in this
     * [Fragment].
     *
     * @param uiState The new [UiState] to apply.
     */
    private fun handleUiStateChanged(uiState: UiState) {
        setRefreshActionItemLoadingState(uiState)

        when (uiState) {
            is UiState.ShowEmptyProgress -> handleShowEmptyProgress()
            is UiState.ShowPopulatedProgress -> handleShowPopulatedProgress()
            is UiState.ShowContent -> handleShowContent(uiState.tweets)
            is UiState.ShowEmptyError -> handleShowEmptyError(uiState.error)
            is UiState.ShowRefreshError -> handleShowRefreshError(uiState.error)
        }
    }

    /**
     * Handle a new state of showing the empty state progress.
     */
    private fun handleShowEmptyProgress() {
        adapter.submitList(null)
        contentView.showProgressLayout()
        swipeRefreshLayout.isRefreshing = true
    }

    /**
     * Handle a new state of showing the populated state progress.
     */
    private fun handleShowPopulatedProgress() {
        swipeRefreshLayout.isRefreshing = true
    }

    /**
     * Handle a new state of showing the content.
     *
     * @param tweets The [List] of [Tweet]s to show.
     */
    private fun handleShowContent(tweets: List<Tweet>) {
        swipeRefreshLayout.isRefreshing = false
        adapter.submitList(tweets)
        contentView.showContentLayout()
    }

    /**
     * Handle a new state of showing an error to the user, when the previous state was empty.
     *
     * @param error The error to show to the user.
     */
    private fun handleShowEmptyError(error: Error) {
        swipeRefreshLayout.isRefreshing = false
        adapter.submitList(null)
        contentView.showErrorLayout()
        txtError.setText(mapToErrorString(error))
    }

    /**
     * Handle a new state of showing an error to the user, when the previous state was a populated
     * list.
     *
     * @param error The error to show to the user.
     */
    private fun handleShowRefreshError(error: Error) {
        swipeRefreshLayout.isRefreshing = false
        Snackbar.make(contentView, mapToErrorString(error), Snackbar.LENGTH_SHORT)
                .show()
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
     * Set the status of the refresh [MenuItem] based on the current [UiState].
     *
     * @param uiState The current [UiState].
     */
    private fun setRefreshActionItemLoadingState(uiState: UiState?) {
        refreshMenuItem?.let {
            if (uiState == UiState.ShowEmptyProgress || uiState == UiState.ShowPopulatedProgress) {
                it.isEnabled = false
                it.setActionView(R.layout.actionbar_indeterminate_progress)
            } else {
                it.isEnabled = true
                it.setActionView(null)
            }
        }
    }

    /**
     * Handle the avatar in an item being clicked.
     *
     * @param tweet The [Tweet] which was clicked.
     */
    private fun handleItemClicked(tweet: Tweet) {
        Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(tweet.profileUrl))
                .let {
                    try {
                        startActivity(it)
                    } catch (ignored: ActivityNotFoundException) {
                        Toast.makeText(
                                requireContext(),
                                R.string.twitterupdates_err_no_activity_for_click,
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }
}