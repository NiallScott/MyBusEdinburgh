/*
 * Copyright (C) 2010 - 2014 Niall 'Rivernile' Scott
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

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterLoaderResult;
import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterException;
import uk.org.rivernile.android.bustracker.parser.twitter
        .TwitterUpdatesLoader;
import uk.org.rivernile.android.fetchers.UrlMismatchException;

/**
 * This Fragments displays a ListView of Tweets which informs users of things
 * that may affect their journey. No action can be taken on the ListView items
 * but URLs can be tapped.
 * 
 * @author Niall Scott
 */
public class TwitterUpdatesFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<TwitterLoaderResult> {
    
    private TweetAdapter adapter;
    private ProgressBar progress;
    private TextView txtError;
    private MenuItem refreshItem;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        adapter = new TweetAdapter(getActivity());
        setListAdapter(adapter);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater
                .inflate(R.layout.twitterupdates, container, false);
        
        progress = (ProgressBar) v.findViewById(R.id.progress);
        txtError = (TextView) v.findViewById(R.id.txtError);
        
        return v;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Tell the underlying Activity that it should create an options menu
        // for this Fragment.
        setHasOptionsMenu(true);
        
        // Initialise the Loader for loading tweets.
        loadTweets(false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        // Inflate the options menu.
        inflater.inflate(R.menu.twitterupdates_option_menu, menu);
        
        refreshItem = menu.findItem(R.id.twitterupdates_option_menu_refresh);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        setRefreshActionItemAsLoading(progress.getVisibility() == View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<TwitterLoaderResult> onCreateLoader(final int id,
            final Bundle args) {
        return new TwitterUpdatesLoader(getActivity());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<TwitterLoaderResult> loader,
            final TwitterLoaderResult result) {
        if (isAdded()) {
            if(result.hasException()) {
                handleError(result.getException());
            } else {
                populateList(result.getTweets());
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<TwitterLoaderResult> loader) {
        // Nothing to do here.
    }
    
    /**
     * Start off the process of loading Tweets.
     * 
     * @param force true if a load should be forced, that is, scrap any previous
     * data loaded and load new data. false if previous data is to be used, if
     * available.
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
     * Set the status of the refresh ActionItem. Under normal circumstances,
     * the ActionItem will only display a refresh icon and is enabled. While
     * loading, this icon will be replaced with a progress indicator and will
     * be disabled.
     * 
     * @param loading true if data is currently loading, false if not.
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
        txtError.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        
        // Sort out the refresh menu item.
        setRefreshActionItemAsLoading(true);
    }
    
    /**
     * Show an error with the String of the given resId.
     * 
     * @param resId The resource ID of the String to show as the error.
     */
    private void showError(final int resId) {
        showError(getString(resId));
    }
    
    /**
     * Show an error with the given CharSequence.
     */
    private void showError(final CharSequence errorString) {
        txtError.setText(errorString);
        
        // Show the error layout.
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
     * @param exception The TwitterException containing details about the
     * exception.
     */
    private void handleError(final TwitterException exception) {
        final Throwable cause = exception != null ? exception.getCause() : null;
        
        if (cause instanceof UrlMismatchException) {
            showError(R.string.twitterupdates_err_urlmismatch);
        } else {
            showError(R.string.twitterupdates_err_load);
        }
    }
    
    /**
     * Populate the ListView with the Twitter news items.
     * 
     * @param items An ArrayList of {@link Tweet}s.
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
        
        // Sort out the refresh menu item.
        setRefreshActionItemAsLoading(false);
    }
}