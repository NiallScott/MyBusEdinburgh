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

package uk.org.rivernile.edinburghbustracker.android;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import uk.org.rivernile.android.utils.ActionBarCompat;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .DisplayStopDataFragment;

/**
 * This Activity hosts a DisplayStopDataFragment which shows the user live bus
 * times.
 * 
 * This Activity can be started by any other application with the following
 * Intent action;
 * 
 * uk.org.rivernile.edinburghbustracker.android.ACTION_VIEW_STOP_DATA
 * 
 * A String parameter called "stopCode" must be added as an extra to the Intent
 * which contains the bus stop code to be loaded.
 * 
 * @author Niall Scott
 * @see DisplayStopDataFragment
 */
public class DisplayStopDataActivity extends FragmentActivity {
    
    /** The ACTION_VIEW_STOP_DATA intent action name. */
    public static final String ACTION_VIEW_STOP_DATA =
            "uk.org.rivernile.edinburghbustracker.android."
            + "ACTION_VIEW_STOP_DATA";
    
    /** The Intent argument for the stop code. */
    public static final String ARG_STOPCODE =
            DisplayStopDataFragment.ARG_STOPCODE;
    /** The Intent argument to force a load. */
    public static final String ARG_FORCELOAD =
            DisplayStopDataFragment.ARG_FORCELOAD;
    
    private static final boolean IS_HONEYCOMB_OR_GREATER =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.single_fragment_container);
        
        if(IS_HONEYCOMB_OR_GREATER) {
            ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
        }
        
        if(savedInstanceState == null) {
            DisplayStopDataFragment fragment;
            final Intent intent = getIntent();
            
            if(Intent.ACTION_VIEW.equals(intent.getAction())) {
                fragment = DisplayStopDataFragment.newInstance(
                        intent.getData().getQueryParameter("busStopCode"));
            } else {
                if(!intent.hasExtra(ARG_STOPCODE)) {
                    throw new IllegalArgumentException(
                            "A stopCode MUST be provided.");
                }
                
                final String stopCode = intent.getStringExtra(ARG_STOPCODE);
                
                if(intent.hasExtra(ARG_FORCELOAD)) {
                    fragment = DisplayStopDataFragment.newInstance(stopCode,
                            intent.getBooleanExtra(ARG_FORCELOAD, false));
                } else {
                    fragment = DisplayStopDataFragment.newInstance(stopCode);
                }
            }
            
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, fragment).commit();
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
}