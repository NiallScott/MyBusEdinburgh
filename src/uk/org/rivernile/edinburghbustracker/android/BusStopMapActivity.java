/*
 * Copyright (C) 2012 - 2013 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .IndeterminateProgressDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .MapTypeChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .ServicesChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .BusStopMapFragment;

public class BusStopMapActivity extends ActionBarActivity
        implements BusStopMapFragment.Callbacks,
        MapTypeChooserDialogFragment.Callbacks,
        ServicesChooserDialogFragment.Callbacks,
        IndeterminateProgressDialogFragment.Callbacks {
    
    /** The stopCode argument for the Intent. */
    public static final String ARG_STOPCODE = BusStopMapFragment.ARG_STOPCODE;
    /** The latitude argument for the Intent. */
    public static final String ARG_LATITUDE = BusStopMapFragment.ARG_LATITUDE;
    /** The longitude argument for the Intent. */
    public static final String ARG_LONGITUDE = BusStopMapFragment.ARG_LONGITUDE;
    
    private static final String DIALOG_MAP_TYPE_CHOOSER = "mapTypeDialog";
    private static final String DIALOG_SERVICES_CHOOSER =
            "servicesChooserDialog";
    private static final String DIALOG_PROGRESS = "progressDialog";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_container);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Only add the fragment if there was no previous instance of this
        // Activity, otherwise this fragment will appear multiple times.
        if(savedInstanceState == null) {
            final Intent intent = getIntent();
            BusStopMapFragment f;
            
            if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
                f = BusStopMapFragment.newInstanceWithSearch(
                        intent.getStringExtra(SearchManager.QUERY));
            } else if(intent.hasExtra(ARG_STOPCODE)) {
                f = BusStopMapFragment.newInstance(
                        intent.getStringExtra(ARG_STOPCODE));
            } else if(intent.hasExtra(ARG_LATITUDE) &&
                    intent.hasExtra(ARG_LONGITUDE)) {
                f = BusStopMapFragment.newInstance(
                        intent.getDoubleExtra(ARG_LATITUDE, 0),
                        intent.getDoubleExtra(ARG_LONGITUDE, 0));
            } else {
                f = new BusStopMapFragment();
            }
            
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, f)
                    .commit();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        
        final BusStopMapFragment f = (BusStopMapFragment)
                getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if(f == null) {
            return;
        }
        
        if(Intent.ACTION_SEARCH.equals(newIntent.getAction())) {
            f.onSearch(newIntent.getStringExtra(SearchManager.QUERY));
        } else if(newIntent.hasExtra(ARG_STOPCODE)) {
            f.moveCameraToBusStop(newIntent.getStringExtra(ARG_STOPCODE));
        } else if(newIntent.hasExtra(ARG_LATITUDE) &&
                newIntent.hasExtra(ARG_LONGITUDE)) {
            f.moveCameraToLocation(
                    new LatLng(newIntent.getDoubleExtra(ARG_LATITUDE, 0),
                        newIntent.getDoubleExtra(ARG_LONGITUDE, 0)),
                    BusStopMapFragment.DEFAULT_SEARCH_ZOOM, false);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavigationUtils
                        .navigateUpOnActivityWithMultipleEntryPoints(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        final BusStopMapFragment f = (BusStopMapFragment)
                getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        
        if(!f.onBackPressed()) {
            super.onBackPressed();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowMapTypeSelection() {
        new MapTypeChooserDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_MAP_TYPE_CHOOSER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowServicesChooser(final String[] services,
                final String[] selectedServices, final String title) {
        ServicesChooserDialogFragment
                .newInstance(services, selectedServices, title)
                .show(getSupportFragmentManager(), DIALOG_SERVICES_CHOOSER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowSearchProgress(final String message) {
        IndeterminateProgressDialogFragment.newInstance(message)
                .show(getSupportFragmentManager(), DIALOG_PROGRESS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismissSearchProgress() {
        final IndeterminateProgressDialogFragment progressDialog =
                (IndeterminateProgressDialogFragment)
                        getSupportFragmentManager()
                                .findFragmentByTag(DIALOG_PROGRESS);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowBusStopDetails(final String stopCode) {
        final Intent intent = new Intent(this, BusStopDetailsActivity.class);
        intent.putExtra(BusStopDetailsActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapTypeChosen(final int mapType) {
        try {
            final MapTypeChooserDialogFragment.Callbacks child =
                    (MapTypeChooserDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id.fragmentContainer);
            if (child != null) {
                child.onMapTypeChosen(mapType);
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onServicesChosen(final String[] chosenServices) {
        try {
            final ServicesChooserDialogFragment.Callbacks child =
                    (ServicesChooserDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id.fragmentContainer);
            if (child != null) {
                child.onServicesChosen(chosenServices);
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }

    @Override
    public void onProgressCancel() {
        try {
            final IndeterminateProgressDialogFragment.Callbacks child =
                    (IndeterminateProgressDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id.fragmentContainer);
            if (child != null) {
                child.onProgressCancel();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
}