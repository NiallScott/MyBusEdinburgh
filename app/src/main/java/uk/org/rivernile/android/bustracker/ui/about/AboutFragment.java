/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.about;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import java.util.List;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} shows application 'about' information to the user.
 *
 * @author Niall Scott
 */
public class AboutFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<AboutItem>>, AbsListView.OnItemClickListener {

    private AboutAdapter adapter;
    private Callbacks callbacks;

    private ListView listView;
    private ProgressBar progress;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() + " does not implement "
                    + Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new AboutAdapter(getActivity());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.about_fragment, container, false);
        listView = (ListView) v.findViewById(android.R.id.list);
        progress = (ProgressBar) v.findViewById(R.id.progress);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<AboutItem>> onCreateLoader(final int id, final Bundle args) {
        return new AboutItemLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<AboutItem>> loader, final List<AboutItem> data) {
        adapter.setAboutItems(data);
        progress.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(final Loader<List<AboutItem>> loader) {
        // Nothing to do here.
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                            final long id) {
        final AboutItem item = adapter.getItem(position);

        if (item != null) {
            item.doAction(getActivity(), callbacks);
        }
    }

    /**
     * Any {@link Activity Activities} which host this {@link Fragment} must implement this
     * interface to handle navigation events.
     */
    public static interface Callbacks {

        /**
         * This is called when the user wants to see credits.
         */
        public void onShowCredits();

        /**
         * This is called when the user wants to see the open source licences.
         */
        public void onShowLicences();
    }
}
