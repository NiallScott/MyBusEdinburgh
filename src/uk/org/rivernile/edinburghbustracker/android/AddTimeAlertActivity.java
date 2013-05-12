/*
 * Copyright (C) 2011 - 2013 Niall 'Rivernile' Scott
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
        .AddTimeAlertFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .AlertFragmentEvent;

/**
 * Add a new time alert. This allows the user to specify a list of services to
 * watch and the time trigger in which to alert them. If this activity is
 * started with no stopCode in the Intent, then an exception will be thrown.
 * 
 * @author Niall Scott
 * @see AddTimeAlertFragment
 */
public class AddTimeAlertActivity extends FragmentActivity
        implements AlertFragmentEvent {
    
    /** The stopCode argument.*/
    public static final String ARG_STOPCODE = AddTimeAlertFragment.ARG_STOPCODE;
    /** The default service argument. */
    public static final String ARG_DEFAULT_SERVICE = AddTimeAlertFragment
            .ARG_DEFAULT_SERVICE;
    
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
        
        // Only add the fragment if there was no previous instance of this
        // Activity, otherwise this fragment will appear multiple times.
        if(savedInstanceState == null) {
            final Intent intent = getIntent();
            AddTimeAlertFragment fragment;
            
            if(intent.hasExtra(ARG_DEFAULT_SERVICE)) {
                fragment = AddTimeAlertFragment.newInstance(
                        intent.getStringExtra(ARG_STOPCODE),
                        intent.getStringExtra(ARG_DEFAULT_SERVICE));
            } else {
                fragment = AddTimeAlertFragment.newInstance(
                        intent.getStringExtra(ARG_STOPCODE));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAlertAdded() {
        finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancel() {
        finish();
    }
}