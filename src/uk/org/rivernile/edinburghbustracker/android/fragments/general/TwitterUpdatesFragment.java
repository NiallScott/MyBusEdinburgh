/*
 * Copyright (C) 2010 - 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fragments.general;

import static uk.org.rivernile.edinburghbustracker.android.twitter
        .TwitterUpdatesLoader.ERROR_NODATA;
import static uk.org.rivernile.edinburghbustracker.android.twitter
        .TwitterUpdatesLoader.ERROR_PARSEERR;
import static uk.org.rivernile.edinburghbustracker.android.twitter
        .TwitterUpdatesLoader.ERROR_IOERR;
import static uk.org.rivernile.edinburghbustracker.android.twitter
        .TwitterUpdatesLoader.ERROR_URLERR;
import static uk.org.rivernile.edinburghbustracker.android.twitter
        .TwitterUpdatesLoader.ERROR_URLMISMATCH;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.twitter.TwitterLoaderResult;
import uk.org.rivernile.edinburghbustracker.android.twitter.TwitterNewsItem;
import uk.org.rivernile.edinburghbustracker.android.twitter
        .TwitterUpdatesLoader;

/**
 * This Fragments displays a ListView of Tweets which informs users of things
 * that may affect their journey. No action can be taken on the ListView items
 * but URLs can be tapped.
 * 
 * @author Niall Scott
 */
public class TwitterUpdatesFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<TwitterLoaderResult> {
    
    private View layoutProgress, layoutError;
    private TextView txtError;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.newsupdates, container, false);
        
        layoutProgress = v.findViewById(R.id.layoutProgress);
        layoutError = v.findViewById(R.id.layoutError);
        txtError = (TextView)v.findViewById(R.id.txtError);
        
        final Button btnRetry = (Button)v.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Attempt a retry.
                getLoaderManager().restartLoader(0, null,
                        TwitterUpdatesFragment.this);
            }
        });
        
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
        getLoaderManager().initLoader(0, null, this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        // Inflate the options menu.
        inflater.inflate(R.menu.newsupdates_option_menu, menu);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        final MenuItem refreshItem = menu.findItem(
                R.id.newsupdates_option_menu_refresh);
        
        if(layoutProgress.getVisibility() == View.VISIBLE) {
            // Disable the refresh item if a refresh is in progress.
            refreshItem.setEnabled(false);
        } else {
            // Enable the refresh item otherwise.
            refreshItem.setEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.newsupdates_option_menu_refresh:
                // Fetch the data.
                getLoaderManager().restartLoader(0, null, this);
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
        showProgress();
        
        return new TwitterUpdatesLoader(getActivity());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<TwitterLoaderResult> loader,
            final TwitterLoaderResult result) {
        if(result.hasError()) {
            handleError(result.getError());
        } else {
            populateList(result.getResult());
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
     * Show the user progress on loading.
     */
    private void showProgress() {
        // Empty the ListView.
        setListAdapter(null);
        
        // Show the progress.
        layoutError.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.VISIBLE);
        
        // Invalidate so that the refresh item gets disabled.
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * Handle error codes.
     * 
     * @param errorCode The error code.
     */
    private void handleError(final int errorCode) {
        // Set the TextView depending on what the error code is.
        switch(errorCode) {
            case ERROR_NODATA:
                txtError.setText(R.string.newsupdates_err_nodata);
                break;
            case ERROR_PARSEERR:
                txtError.setText(R.string.newsupdates_err_parseerr);
                break;
            case ERROR_IOERR:
                txtError.setText(R.string.newsupdates_err_ioerr);
                break;
            case ERROR_URLERR:
                txtError.setText(R.string.newsupdates_err_urlerr);
                break;
            case ERROR_URLMISMATCH:
                txtError.setText(R.string.newsupdates_err_urlmismatch);
                break;
        }
        
        // Show the error layout.
        layoutProgress.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        // Invalidate the options menu so that the refresh item is shown again.
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * Populate the ListView with the Twitter news items.
     * 
     * @param items An ArrayList of {@link TwitterNewsItem}s.
     */
    private void populateList(final ArrayList<TwitterNewsItem> items) {
        // If there's 0 items, display an error.
        if(items == null || items.isEmpty()) {
            handleError(ERROR_NODATA);
            return;
        }
        
        // Ensure that the progress and error layouts are removed.
        layoutError.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
        
        // Fun and magic happens here.
        final ArrayList<HashMap<String, String>> list =
                new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        for(TwitterNewsItem item : items) {
            map = new HashMap<String, String>();
            map.put("TEXT", item.getBody());
            map.put("INFO", item.getPoster() + " - " + item.getDate());
            list.add(map);
        }
        
        // Create the ListAdapter.
        final NewsItemsAdapter adapter = new NewsItemsAdapter(getActivity(),
                list, R.layout.newsupdateslist, new String[] { "TEXT", "INFO" },
                new int[] { R.id.twitText, R.id.twitInfo });
        setListAdapter(adapter);
        
        // Invalidate the options menu to make sure that the refresh item is
        // shown.
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * This is the ListAdapter which is used to display the Twitter items.
     */
    private static class NewsItemsAdapter extends SimpleAdapter {
        
        /**
         * {@inheritDoc}
         */
        public NewsItemsAdapter(final Context context,
                final List<? extends Map<String, ?>> data, final int resource,
                final String[] from, final int[] to) {
            super(context, data, resource, from, to);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEnabled(final int index) {
            return false;
        }
    }
}