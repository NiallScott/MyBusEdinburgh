/*
 * Copyright (C) 2009 - 2013 Niall 'Rivernile' Scott
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

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import uk.org.rivernile.android.utils.GraphicsUtils;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The MainDashboardFragment contains the main menu to the application. It
 * displays a grid of icons where the user can select which part of the app they
 * wish to navigate to.
 * 
 * @author Niall Scott
 */
public class MainDashboardFragment extends Fragment
        implements View.OnClickListener {
    
    private Callbacks callbacks;
    private Button favouriteButton;
    private Button stopCodeButton;
    private Button stopMapButton;
    private Button nearestButton;
    private Button newsButton;
    private Button alertButton;
    private Button resolveButton;
    private View layoutPlayServicesBar;
    private ConnectionResult connectionResult;
    private TextView txtPlayServicesError;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the parent that this Fragment offers an options menu.
        setHasOptionsMenu(true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.home, container, false);
        
        favouriteButton = (Button)v.findViewById(R.id.home_btn_favourites);
        stopCodeButton = (Button)v.findViewById(R.id.home_btn_entercode);
        stopMapButton = (Button)v.findViewById(R.id.home_btn_map);
        nearestButton = (Button)v.findViewById(R.id.home_btn_nearest);
        newsButton = (Button)v.findViewById(R.id.home_btn_news);
        alertButton = (Button)v.findViewById(R.id.home_btn_alerts);
        resolveButton = (Button)v.findViewById(R.id.btnResolve);
        layoutPlayServicesBar = v.findViewById(R.id.layoutPlayServicesBar);
        txtPlayServicesError = (TextView)v.findViewById(
                R.id.txtPlayServicesError);
        
        favouriteButton.setOnClickListener(this);
        stopCodeButton.setOnClickListener(this);
        stopMapButton.setOnClickListener(this);
        nearestButton.setOnClickListener(this);
        newsButton.setOnClickListener(this);
        alertButton.setOnClickListener(this);
        resolveButton.setOnClickListener(this);
        
        return v;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        // Check the OpenGL ES version.
        final Context context = getActivity();
        if (GraphicsUtils.getOpenGLESVersion(context) < 2) {
            txtPlayServicesError.setText(R.string.main_maps_incompatible);
            layoutPlayServicesBar.setVisibility(View.VISIBLE);
            stopMapButton.setEnabled(false);
            resolveButton.setVisibility(View.GONE);
            return;
        }
        
        // Check to see if the Google Play Store services are available and up
        // to date.
        final int errorCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);
        if(errorCode == ConnectionResult.SUCCESS) {
            // All is okay.
            layoutPlayServicesBar.setVisibility(View.GONE);
            stopMapButton.setEnabled(true);
            connectionResult = null;
            return;
        } else {
            // Otherwise don't let the user try to use the map, as there is no
            // point. It won't work.
            layoutPlayServicesBar.setVisibility(View.VISIBLE);
            stopMapButton.setEnabled(false);
        }
        
        // There's no point in displaying the resolve button if the issue
        // cannot be resolved.
        boolean playStoreExists = false;
        try {
            final PackageInfo playStoreInfo = context.getPackageManager()
                    .getPackageInfo(
                        GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, 0);
            if(playStoreInfo != null) {
                playStoreExists = true;
            }
        } catch(PackageManager.NameNotFoundException e) {
            // playStoreExists will already be false.
        }
        
        // If the problem is user recoverable, then show a button which lets the
        // user take action.
        if(GooglePlayServicesUtil.isUserRecoverableError(errorCode) &&
                playStoreExists) {
            resolveButton.setVisibility(View.VISIBLE);
            connectionResult = new ConnectionResult(errorCode,
                    GooglePlayServicesUtil.getErrorPendingIntent(errorCode,
                        context, 0));
        } else {
            resolveButton.setVisibility(View.GONE);
            connectionResult = new ConnectionResult(errorCode, null);
        }
        
        // Customise the text for the error.
        switch(errorCode) {
            case ConnectionResult.SERVICE_MISSING:
                txtPlayServicesError.setText(R.string
                        .main_play_services_missing_text);
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                txtPlayServicesError.setText(R.string
                        .main_play_services_update_text);
                break;
            case ConnectionResult.SERVICE_DISABLED:
                txtPlayServicesError.setText(R.string
                        .main_play_services_disabled_text);
                break;
            case ConnectionResult.SERVICE_INVALID:
                txtPlayServicesError.setText(R.string
                        .main_play_services_invalid_text);
                break;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        if(v == favouriteButton) {
            callbacks.onShowFavourites();
        } else if(v == stopCodeButton) {
            callbacks.onShowStopCodeEntry();
        } else if(v == stopMapButton) {
            callbacks.onShowBusStopMap();
        } else if(v == nearestButton) {
            callbacks.onShowNearestStops();
        } else if(v == newsButton) {
            callbacks.onShowNewsUpdates();
        } else if(v == alertButton) {
            callbacks.onShowAlertManager();
        } else if(v == resolveButton) {
            if(connectionResult != null && connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(getActivity(), 0);
                } catch(SendIntentException e) {
                    
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        inflater.inflate(R.menu.main_option_menu, menu);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_option_menu_preferences:
                callbacks.onShowAppPreferences();
                return true;
            case R.id.main_option_menu_about:
                callbacks.onShowAboutInformation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Any Activities which host this Fragment must implement this interface to
     * handle navigation events.
     */
    public static interface Callbacks {
        
        /**
         * This is called when the user wants to navigate to their favourites.
         */
        public void onShowFavourites();
        
        /**
         * This is called when the user wants to enter a bus stop code.
         */
        public void onShowStopCodeEntry();
        
        /**
         * This is called when the user wants to navigate to the bus stop map.
         */
        public void onShowBusStopMap();
        
        /**
         * This is called when the user wants to navigate to the nearest bus
         * stops.
         */
        public void onShowNearestStops();
        
        /**
         * This is called when the user wants to navigate to news updates.
         */
        public void onShowNewsUpdates();
        
        /**
         * This is called when the user wants to navigate to the alert manager.
         */
        public void onShowAlertManager();
        
        /**
         * This is called when the user wants to navigate to the application
         * preferences.
         */
        public void onShowAppPreferences();
        
        /**
         * This is called when the user wants to navigate to the application
         * About information.
         */
        public void onShowAboutInformation();
    }
}