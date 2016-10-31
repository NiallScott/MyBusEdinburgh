/*
 * Copyright (C) 2009 - 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.org.rivernile.android.bustracker.database.busstop.loaders.ServiceColoursLoader;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimesLoader;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesResult;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} shows bus times to the user in an expandable list.
 *
 * @author Niall Scott
 */
public class BusTimesFragment extends Fragment implements LoaderManager.LoaderCallbacks {

    private static final String ARG_STOP_CODE = "stopCode";

    private static final int LOADER_BUS_TIMES = 1;
    private static final int LOADER_SERVICE_COLOURS = 2;

    private String stopCode;
    private SharedPreferences sp;
    private int numberOfDepartures;

    private RecyclerView recyclerView;

    /**
     * Create a new instance of this {@link Fragment}.
     *
     * @param stopCode The stop code to show bus times for.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static BusTimesFragment newInstance(@NonNull final String stopCode) {
        final BusTimesFragment fragment = new BusTimesFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOP_CODE, stopCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stopCode = getArguments().getString(ARG_STOP_CODE);
        sp = getContext().getSharedPreferences(PreferenceConstants.PREF_FILE, 0);

        try {
            numberOfDepartures = Integer.parseInt(
                    sp.getString(PreferenceConstants
                            .PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE, "4"));
        } catch (NumberFormatException e) {
            numberOfDepartures = 4;
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.bustimes_fragment, container, false);
        recyclerView = (RecyclerView) v.findViewById(android.R.id.list);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_SERVICE_COLOURS, null, this);
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BUS_TIMES:
                return new LiveBusTimesLoader(getContext(), new String[] { stopCode },
                        numberOfDepartures);
            case LOADER_SERVICE_COLOURS:
                return new ServiceColoursLoader(getContext(), null);
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(final Loader loader, final Object data) {
        switch (loader.getId()) {
            case LOADER_BUS_TIMES:
                handleBusTimesResult((LiveTimesResult<LiveBusTimes>) data);
                break;
            case LOADER_SERVICE_COLOURS:
                // TODO: handle.
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        // Nothing to do here.
    }

    /**
     * Begin loading bus times.
     *
     * @param forceLoad {@code true} if the load should be forced (i.e. the previous data should be
     * flushed). {@code false} if not.
     */
    private void loadBusTimes(final boolean forceLoad) {

    }

    /**
     * Handle the result of loading bus times.
     *
     * @param result The result of loading bus times.
     */
    private void handleBusTimesResult(@NonNull final LiveTimesResult<LiveBusTimes> result) {

    }

    /**
     * Show content views to the user.
     */
    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Show the progress view to the user.
     */
    private void showProgress() {
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * Show the error view to the user.
     */
    private void showError() {
        recyclerView.setVisibility(View.GONE);
    }
}
