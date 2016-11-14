/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.search;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopSearchLoader;
import uk.org.rivernile.android.bustracker.database.search.SearchSuggestionsProvider;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link android.app.Activity} allows the user to perform a search for bus stops.
 *
 * @author Niall Scott
 */
public class SearchActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, SearchAdapter.OnItemClickedListener,
        SearchView.OnQueryTextListener, InstallBarcodeScannerDialogFragment.Callbacks {

    /**
     * If this {@link android.app.Activity} was started with
     * {@link #startActivityForResult(Intent, int)}, an {@link Intent} extra with this key will be
     * included in the result which contains the selected bus stop code.
     */
    public static final String EXTRA_STOP_CODE = "stopCode";

    private static final String BARCODE_APP_PACKAGE =
            "market://details?id=com.google.zxing.client.android";
    private static final String BARCODE_ACTION = "com.google.zxing.client.android.SCAN";
    private static final String BARCODE_EXTRA_QR_CODE_MODE = "QR_CODE_MODE";
    private static final String BARCODE_EXTRA_SCAN_RESULT = "SCAN_RESULT";

    private static final String DIALOG_INSTALL_QR_SCANNER = "installQrScanner";

    private static final int REQUEST_CODE_SCAN_QR = 1;

    private static final int LOADER_SEARCH = 1;

    private static final String LOADER_ARG_SEARCH_TERM = "searchTerm";

    private static final String URI_QUERY_PARAMETER_STOP_CODE = "busStopCode";

    private SearchRecentSuggestions recentSuggestions;
    private SearchAdapter adapter;

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressBar progress;
    private TextView txtError;

    private MenuItem menuItemScan;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        searchView = (SearchView) findViewById(R.id.searchView);
        recyclerView = (RecyclerView) findViewById(android.R.id.list);
        progress = (ProgressBar) findViewById(R.id.progress);
        txtError = (TextView) findViewById(R.id.txtError);

        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        adapter = new SearchAdapter(this);
        adapter.setOnItemClickedListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        recentSuggestions = new SearchRecentSuggestions(this, SearchSuggestionsProvider.AUTHORITY,
                SearchSuggestionsProvider.MODE);

        showEmptySearchTermError();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.search_option_menu, menu);
        menuItemScan = menu.findItem(R.id.search_option_menu_scan);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            menuItemScan.setVisible(getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY));
        } else {
            menuItemScan.setVisible(getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_option_menu_scan:
                handleQrCodeButtonClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        if (requestCode == REQUEST_CODE_SCAN_QR && resultCode == RESULT_OK) {
            // The data is delivered as an Intent, so if it's null the scan has not been successful,
            // despite the resultCode.
            if (data == null) {
                Toast.makeText(this, R.string.search_scan_error, Toast.LENGTH_SHORT).show();
                return;
            }

            // The scanned data is a URL, so parse it in to a Uri so we can easily extract the
            // stop code parameter.
            final Uri uri = Uri.parse(data.getStringExtra(BARCODE_EXTRA_SCAN_RESULT));

            // Only hierarchical URIs can possibly be valid, so this is a useful sanity check.
            if (!uri.isHierarchical()) {
                Toast.makeText(this, R.string.search_invalid_qrcode, Toast.LENGTH_SHORT).show();
                return;
            }

            final String stopCode = uri.getQueryParameter(URI_QUERY_PARAMETER_STOP_CODE);

            if (!TextUtils.isEmpty(stopCode)) {
                onStopCodeSelected(stopCode);
            } else {
                Toast.makeText(this, R.string.search_invalid_qrcode, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_SEARCH:
                final String searchTerm =
                        args != null ? args.getString(LOADER_ARG_SEARCH_TERM) : null;

                return !TextUtils.isEmpty(searchTerm) ?
                        new BusStopSearchLoader(this, searchTerm) : null;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {
            case LOADER_SEARCH:
                handleSearchResultsLoaded(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_SEARCH:
                handleSearchResultsLoaded(null);
                break;
        }
    }

    @Override
    public void onItemClicked(@NonNull final Cursor cursor) {
        onStopCodeSelected(cursor.getString(
                cursor.getColumnIndex(BusStopContract.BusStops.STOP_CODE)));
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (TextUtils.isEmpty(newText)) {
            showEmptySearchTermError();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onShowInstallBarcodeScanner() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(BARCODE_APP_PACKAGE));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Toast.makeText(this, R.string.barcodescannerdialog_noplaystore, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Handle the {@link Intent} that this {@link android.app.Activity} was created with or a new
     * {@link Intent} that has since come in.
     *
     * @param intent The {@link Intent} to handle.
     */
    private void handleIntent(@NonNull final Intent intent) {
        if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
            return;
        }

        final String searchTerm = intent.getStringExtra(SearchManager.QUERY);
        searchView.setQuery(searchTerm, false);
        final Bundle loaderArgs = new Bundle();
        loaderArgs.putString(LOADER_ARG_SEARCH_TERM, searchTerm);

        final LoaderManager loaderManager = getSupportLoaderManager();
        final Loader<Cursor> loader = loaderManager.getLoader(LOADER_SEARCH);
        final BusStopSearchLoader searchLoader = (BusStopSearchLoader) loader;
        showProgress();

        if (loader != null && searchLoader.getSearchTerm().equals(searchTerm)) {
            loaderManager.initLoader(LOADER_SEARCH, loaderArgs, this);
        } else {
            recentSuggestions.saveRecentQuery(searchTerm, null);
            loaderManager.restartLoader(LOADER_SEARCH, loaderArgs, this);
        }
    }

    /**
     * Handle the result of loading search results.
     *
     * @param cursor The {@link Cursor} containing the search results.
     */
    private void handleSearchResultsLoaded(@Nullable final Cursor cursor) {
        adapter.swapCursor(cursor);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                showContent();
            } else {
                showNoResultsError();
            }
        } else {
            showEmptySearchTermError();
        }
    }

    /**
     * Show an error to the user when the search term is empty.
     */
    private void showEmptySearchTermError() {
        showError(R.string.search_error_empty);
    }

    /**
     * Show an error to the user when there were no search results.
     */
    private void showNoResultsError() {
        showError(R.string.search_error_no_results);
    }

    /**
     * Show a progress indicator to the user.
     */
    private void showProgress() {
        txtError.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    /**
     * Show the content to the user, that is, the search results.
     */
    private void showContent() {
        txtError.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Show an error to the user.
     *
     * @param errorResId The resource ID of the error string.
     */
    private void showError(@StringRes final int errorResId) {
        txtError.setText(errorResId);
        progress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        txtError.setVisibility(View.VISIBLE);
    }

    /**
     * Handle a click on the scan QR code button.
     */
    private void handleQrCodeButtonClicked() {
        final Intent intent = new Intent(BARCODE_ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(BARCODE_EXTRA_QR_CODE_MODE, true);
        final List<ResolveInfo> packages = getPackageManager().queryIntentActivities(intent, 0);

        if (packages != null && !packages.isEmpty()) {
            try {
                startActivityForResult(intent, REQUEST_CODE_SCAN_QR);
            } catch (ActivityNotFoundException e) {
                showInstallQrScannerDialog();
            }
        } else {
            showInstallQrScannerDialog();
        }
    }

    /**
     * Show the dialog which asks the user if they wish to install a QR code scanning application.
     */
    private void showInstallQrScannerDialog() {
        new InstallBarcodeScannerDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_INSTALL_QR_SCANNER);
    }

    /**
     * Handle a bus stop being selected by the user.
     *
     * @param stopCode The selected stop code.
     */
    private void onStopCodeSelected(@NonNull final String stopCode) {
        if (getCallingActivity() != null) {
            finishWithStopCode(stopCode);
        } else {
            showDisplayStopDetails(stopCode);
        }
    }

    /**
     * This is called if this {@link android.app.Activity} was started normally, i.e. not for a
     * result. This will start {@link DisplayStopDataActivity}.
     *
     * @param stopCode The selected stop code.
     */
    private void showDisplayStopDetails(@NonNull final String stopCode) {
        final Intent intent = new Intent(this, DisplayStopDataActivity.class);
        intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    /**
     * This is called if this {@link android.app.Activity} was started with
     * {@link #startActivityForResult(Intent, int)}. This will set the result and {@link #finish()}.
     *
     * @param stopCode The selected stop code.
     */
    private void finishWithStopCode(@NonNull final String stopCode) {
        final Intent result = new Intent();
        result.putExtra(EXTRA_STOP_CODE, stopCode);
        setResult(RESULT_OK, result);
        finish();
    }
}
