/*
 * Copyright (C) 2010 - 2018 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.ui.news;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import uk.org.rivernile.android.fetchutils.fetchers.UrlMismatchException;
import uk.org.rivernile.android.fetchutils.loaders.Result;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterException;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterUpdatesLoader;

/**
 * This {@link Fragment} displays a list of {@link Tweet}s which informs the user of events that may
 * affect their journey.
 * 
 * @author Niall Scott
 */
public class TwitterUpdatesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Result<List<Tweet>, TwitterException>>,
        TweetAdapter.OnItemClickListener {
    
    private TweetAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar progress;
    private TextView txtError;

    private MenuItem refreshItem;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        adapter = new TweetAdapter(getActivity());
        adapter.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.twitterupdates, container, false);

        recyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        txtError = (TextView) v.findViewById(R.id.txtError);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new NewsItemDecoration(getActivity(),
                getResources().getDimensionPixelSize(R.dimen.news_divider_inset_start)));
        recyclerView.setAdapter(adapter);
        
        return v;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getActivity().setTitle(R.string.twitterupdates_title);
        
        // Tell the underlying Activity that it should create an options menu for this Fragment.
        setHasOptionsMenu(true);
        
        // Initialise the Loader for loading tweets.
        loadTweets(false);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Inflate the options menu.
        inflater.inflate(R.menu.twitterupdates_option_menu, menu);
        
        refreshItem = menu.findItem(R.id.twitterupdates_option_menu_refresh);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        setRefreshActionItemAsLoading(progress.getVisibility() == View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.twitterupdates_option_menu_refresh:
                // Fetch the data.
                loadTweets(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Result<List<Tweet>, TwitterException>> onCreateLoader(
            final int id, final Bundle args) {
        return new TwitterUpdatesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<Result<List<Tweet>, TwitterException>> loader,
                               final Result<List<Tweet>, TwitterException> result) {
        if (isAdded()) {
            if(result.isError()) {
                handleError(result.getError());
            } else {
                populateList(result.getSuccess());
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<Result<List<Tweet>, TwitterException>> loader) {
        // Nothing to do here.
    }

    @Override
    public void onAvatarClicked(@NonNull final Tweet tweet) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(tweet.getProfileUrl()));
        startActivity(intent);
    }

    /**
     * Start the process of loading Tweets.
     * 
     * @param force {@code true} if a load should be forced, that is, scrap any previous data loaded
     *              and load new data. {@code false} if previous data is to be used, if available.
     */
    private void loadTweets(final boolean force) {
        if (adapter.isEmpty()) {
            // Only show the middle progress if the adapter is empty.
            showProgress();
        } else {
            setRefreshActionItemAsLoading(true);
        }
        
        if (force) {
            getLoaderManager().restartLoader(0, null, this);
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
    }
    
    /**
     * Set the status of the refresh ActionItem. Under normal circumstances, the ActionItem will
     * only display a refresh icon and is enabled. While loading, this icon will be replaced with a
     * progress indicator and will be disabled.
     * 
     * @param loading {@code true} if data is currently loading, {@code false} if not.
     */
    private void setRefreshActionItemAsLoading(final boolean loading) {
        if (refreshItem != null) {
            if (loading) {
                refreshItem.setEnabled(false);
                MenuItemCompat.setActionView(refreshItem,
                        R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setEnabled(true);
                MenuItemCompat.setActionView(refreshItem, null);
            }
        }
    }
    
    /**
     * Show the user progress on loading.
     */
    private void showProgress() {
        // Show the progress.
        recyclerView.setVisibility(View.GONE);
        txtError.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        
        // Sort out the refresh menu item.
        setRefreshActionItemAsLoading(true);
    }
    
    /**
     * Show an error with the {@link String} of the given {@code resId}.
     * 
     * @param resId The resource ID of the {@link String} to show as the error.
     */
    private void showError(@StringRes final int resId) {
        showError(getString(resId));
    }
    
    /**
     * Show an error with the given {@link CharSequence}.
     *
     * @param errorString The error to show.
     */
    private void showError(@NonNull final CharSequence errorString) {
        txtError.setText(errorString);
        
        // Show the error layout.
        recyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        txtError.setVisibility(View.VISIBLE);
        
        // Sort out the refresh menu item.
        setRefreshActionItemAsLoading(false);
        
        // Clear out the List of Tweets from the Adapter as we want to show an
        // error. This List will be set again on the next successful load.
        adapter.setTweets(null);
    }
    
    /**
     * Handle errors.
     * 
     * @param exception The {@link TwitterException} containing details about the exception.
     */
    private void handleError(@Nullable final TwitterException exception) {
        if (exception != null && exception.getCause() instanceof UrlMismatchException) {
            showError(R.string.twitterupdates_err_urlmismatch);
        } else {
            showError(R.string.twitterupdates_err_load);
        }
    }
    
    /**
     * Populate the {@link RecyclerView} with the Twitter news items.
     * 
     * @param items A {@link List} of {@link Tweet}s.
     */
    private void populateList(final List<Tweet> items) {
        // If there's 0 items, display an error.
        if(items == null || items.isEmpty()) {
            showError(R.string.twitterupdates_err_nodata);
            return;
        }
        
        // Ensure that the progress and error layouts are removed.
        txtError.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        
        // Give the Adapter the List of Tweets to show.
        adapter.setTweets(items);
        recyclerView.setVisibility(View.VISIBLE);
        
        // Sort out the refresh menu item.
        setRefreshActionItemAsLoading(false);
    }
}