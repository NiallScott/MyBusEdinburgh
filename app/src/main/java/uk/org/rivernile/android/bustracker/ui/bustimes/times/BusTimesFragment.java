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
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.loaders.ServiceColoursLoader;
import uk.org.rivernile.android.bustracker.parser.livetimes.AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimesLoader;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesResult;
import uk.org.rivernile.android.bustracker.parser.livetimes.MaintenanceException;
import uk.org.rivernile.android.bustracker.parser.livetimes.SystemOverloadedException;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.android.fetchutils.fetchers.UrlMismatchException;
import uk.org.rivernile.android.utils.DividerItemDecoration;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} shows bus times to the user in an expandable list.
 *
 * @author Niall Scott
 */
public class BusTimesFragment extends Fragment implements LoaderManager.LoaderCallbacks,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String ARG_STOP_CODE = "stopCode";

    private static final String STATE_AUTO_REFRESH = "autoRefresh";
    private static final String STATE_LAST_REFRESH = "lastRefresh";

    private static final int LOADER_BUS_TIMES = 1;
    private static final int LOADER_SERVICE_COLOURS = 2;

    private static final int AUTO_REFRESH_PERIOD = 60000; // 60 seconds.

    private final Handler handler = new Handler();
    private SharedPreferences sp;
    private BusTimesAdapter adapter;

    private String stopCode;
    private int numberOfDepartures;
    private long lastRefresh;
    private boolean autoRefresh;
    private boolean busTimesLoading;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View layoutError;
    private TextView txtError;
    private Button btnErrorResolve;

    private MenuItem menuItemRefresh;
    private MenuItem menuItemSort;
    private MenuItem menuItemAutoRefresh;

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
        adapter = new BusTimesAdapter(getContext());

        if (savedInstanceState != null) {
            lastRefresh = savedInstanceState.getLong(STATE_LAST_REFRESH, 0);
            autoRefresh = savedInstanceState.getBoolean(STATE_AUTO_REFRESH, false);
        } else {
            autoRefresh = sp.getBoolean(PreferenceConstants.PREF_AUTO_REFRESH, false);
        }

        adapter.setSortMode(sp.getBoolean(PreferenceConstants.PREF_SERVICE_SORTING, false)
                ? BusTimesAdapter.SORT_ARRIVAL_TIME : BusTimesAdapter.SORT_SERVICE_NAME);

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
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        layoutError = v.findViewById(R.id.layoutError);
        txtError = (TextView) v.findViewById(R.id.txtError);
        btnErrorResolve = (Button) v.findViewById(R.id.btnErrorResolve);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        btnErrorResolve.setOnClickListener(this);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_SERVICE_COLOURS, null, this);

        if (!TextUtils.isEmpty(stopCode)) {
            loadBusTimes(false);
            setHasOptionsMenu(true);
        } else {
            showError(getString(R.string.bustimes_err_nocode), null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setUpAutoRefresh();
    }

    @Override
    public void onStop() {
        super.onStop();

        handler.removeCallbacks(autoRefreshRunnable);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(STATE_LAST_REFRESH, lastRefresh);
        outState.putBoolean(STATE_AUTO_REFRESH, autoRefresh);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.bustimes_option_menu, menu);

        menuItemRefresh = menu.findItem(R.id.bustimes_option_menu_refresh);
        menuItemSort = menu.findItem(R.id.bustimes_option_menu_sort);
        menuItemAutoRefresh = menu.findItem(R.id.bustimes_option_menu_autorefresh);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        configureRefreshActionItem();
        configureSortActionItem();
        configureAutoRefreshActionItem();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bustimes_option_menu_refresh:
                performRefreshSelected();
                return true;
            case R.id.bustimes_option_menu_sort:
                performSortSelected();
                return true;
            case R.id.bustimes_option_menu_autorefresh:
                performAutoRefreshSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                adapter.setServiceColours(
                        ((ProcessedCursorLoader.ResultWrapper<Map<String,String>>) data)
                                .getResult());
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        // Nothing to do here.
    }

    @Override
    public void onRefresh() {
        loadBusTimes(true);
    }

    @Override
    public void onClick(final View v) {
        if (v == btnErrorResolve) {
            loadBusTimes(true);
        }
    }

    /**
     * Begin loading bus times.
     *
     * @param forceLoad {@code true} if the load should be forced (i.e. the previous data should be
     * flushed). {@code false} if not.
     */
    private void loadBusTimes(final boolean forceLoad) {
        busTimesLoading = true;
        showProgress();

        if (forceLoad) {
            getLoaderManager().restartLoader(LOADER_BUS_TIMES, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_BUS_TIMES, null, this);
        }
    }

    /**
     * Handle the result of loading bus times.
     *
     * @param result The result of loading bus times.
     */
    private void handleBusTimesResult(@NonNull final LiveTimesResult<LiveBusTimes> result) {
        busTimesLoading = false;
        lastRefresh = result.getLoadTime();

        if (result.isError()) {
            handleBusTimesError(result.getError());
        } else {
            handleBusTimesSuccess(result.getSuccess());
        }
    }

    /**
     * Handle bus times being successfully loaded from bus tracker endpoint.
     *
     * @param busTimes The live bus times data.
     */
    private void handleBusTimesSuccess(@Nullable final LiveBusTimes busTimes) {
        if (busTimes == null) {
            showError(getString(R.string.bustimes_err_nodata),
                    getString(R.string.bustimes_btn_error_retry));
            adapter.setServices(null);
            return;
        }

        final LiveBusStop busStop = busTimes.getBusStop(stopCode);

        if (busStop == null) {
            showError(getString(R.string.bustimes_err_nodata),
                    getString(R.string.bustimes_btn_error_retry));
            adapter.setServices(null);
            return;
        }

        final List<LiveBusService> services = busStop.getServices();
        adapter.setServices(services);

        if (services.isEmpty()) {
            showError(getString(R.string.bustimes_err_nodata),
                    getString(R.string.bustimes_btn_error_retry));
            return;
        }

        showContent();
    }

    /**
     * Handle errors caused while loading bus times.
     *
     * @param error The {@link LiveTimesException} from the model.
     */
    private void handleBusTimesError(@NonNull final LiveTimesException error) {
        final Throwable cause = error.getCause();
        final Throwable e = cause != null ? cause : error;
        final String errorMessage;
        final String resolveButton;

        if (e instanceof UrlMismatchException) {
            errorMessage = getString(R.string.bustimes_err_urlmismatch);
            resolveButton = getString(R.string.bustimes_btn_error_retry);
        } else if (e instanceof JSONException) {
            errorMessage = getString(R.string.bustimes_err_parseerr);
            resolveButton = null;
        } else if (e instanceof AuthenticationException) {
            errorMessage = getString(R.string.bustimes_err_api_invalid_key);
            resolveButton = null;
        } else if (e instanceof MaintenanceException) {
            errorMessage = getString(R.string.bustimes_err_api_system_maintenance);
            resolveButton = null;
        } else if (e instanceof SystemOverloadedException) {
            errorMessage = getString(R.string.bustimes_err_api_system_overloaded);
            resolveButton = null;
        } else {
            errorMessage = getString(R.string.displaystopdata_err_unknown);
            resolveButton = null;
        }

        showError(errorMessage, resolveButton);
    }

    /**
     * Schedule the auto-refresh to execute again 60 seconds after the data was last refreshed.
     */
    private void setUpAutoRefresh() {
        handler.removeCallbacks(autoRefreshRunnable);

        if (!autoRefresh || busTimesLoading) {
            return;
        }

        final long time = (lastRefresh + AUTO_REFRESH_PERIOD) - SystemClock.elapsedRealtime();

        if (time > 0) {
            handler.postDelayed(autoRefreshRunnable, time);
        } else {
            handler.post(autoRefreshRunnable);
        }
    }

    /**
     * Show content views to the user.
     */
    private void showContent() {
        layoutError.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);

        configureRefreshActionItem();
    }

    /**
     * Show the progress view to the user.
     */
    private void showProgress() {
        layoutError.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        configureRefreshActionItem();
    }

    /**
     * Show the error view to the user.
     */
    private void showError(@NonNull final String errorText,
            @Nullable final String resolveButtonText) {
        txtError.setText(errorText);
        btnErrorResolve.setText(resolveButtonText);
        btnErrorResolve.setVisibility(!TextUtils.isEmpty(resolveButtonText)
                ? View.VISIBLE : View.GONE);

        recyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        layoutError.setVisibility(View.VISIBLE);

        configureRefreshActionItem();
    }

    /**
     * Configure the refresh menu item with the correct state.
     */
    private void configureRefreshActionItem() {
        if (menuItemRefresh != null) {
            if (busTimesLoading) {
                menuItemRefresh.setEnabled(false);
                MenuItemCompat.setActionView(menuItemRefresh,
                        R.layout.actionbar_indeterminate_progress);
            } else {
                menuItemRefresh.setEnabled(true);
                MenuItemCompat.setActionView(menuItemRefresh, null);
            }
        }
    }

    /**
     * Configure the sort menu item with the correct state.
     */
    private void configureSortActionItem() {
        configureSortActionItem(sp.getBoolean(PreferenceConstants.PREF_SERVICE_SORTING, false));
    }

    /**
     * Configure the sort menu item with the correct state.
     *
     * @param sortByTime {@code true} if sorting by time is enabled, {@code false} if sorting by
     * service name.
     */
    private void configureSortActionItem(final boolean sortByTime) {
        if (menuItemSort != null) {
            // Sort by time or service?
            if (sortByTime) {
                menuItemSort.setTitle(R.string.bustimes_menu_sort_service);
                menuItemSort.setIcon(R.drawable.ic_action_sort_by_size);
            } else {
                menuItemSort.setTitle(R.string.bustimes_menu_sort_times);
                menuItemSort.setIcon(R.drawable.ic_action_time);
            }

            menuItemSort.setEnabled(!busTimesLoading);
        }
    }

    /**
     * Configure the auto refresh menu item with the correct state.
     */
    private void configureAutoRefreshActionItem() {
        if (menuItemAutoRefresh != null) {
            menuItemAutoRefresh.setTitle(autoRefresh
                    ? R.string.bustimes_menu_turnautorefreshoff
                    : R.string.bustimes_menu_turnautorefreshon);
        }
    }

    /**
     * This is called when the refresh action item is selected.
     */
    private void performRefreshSelected() {
        loadBusTimes(true);
    }

    /**
     * This is called when the sort action item is selected.
     */
    private void performSortSelected() {
        // Change the sort preference and ask for a data redisplay.
        final boolean sortByTime = !sp.getBoolean(PreferenceConstants.PREF_SERVICE_SORTING, false);
        final SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(PreferenceConstants.PREF_SERVICE_SORTING, sortByTime);
        edit.apply();

        adapter.setSortMode(sortByTime
                ? BusTimesAdapter.SORT_ARRIVAL_TIME : BusTimesAdapter.SORT_SERVICE_NAME);

        configureSortActionItem(sortByTime);
    }

    /**
     * This is called when the auto refresh action item is selected.
     */
    private void performAutoRefreshSelected() {
        // Turn auto-refresh on or off.
        if (autoRefresh) {
            autoRefresh = false;
            handler.removeCallbacks(autoRefreshRunnable);
        } else {
            autoRefresh = true;
            setUpAutoRefresh();
        }

        configureAutoRefreshActionItem();
    }

    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadBusTimes(true);
        }
    };
}
