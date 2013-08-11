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
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .AddEditFavouriteStopFragment;

/**
 * This Activity allows the user to edit or add a new favourite bus stop. The
 * work is done inside {@link AddEditFavouriteStopFragment}. This Activity
 * simply just adds that fragment to the container.
 * 
 * @author Niall Scott
 * @see AddEditFavouriteStopFragment
 */
public class AddEditFavouriteStopActivity extends ActionBarActivity {
    
    /** The stopCode argument name. */
    public static final String ARG_STOPCODE =
            AddEditFavouriteStopFragment.ARG_STOPCODE;
    /** The stopName argument name. */
    public static final String ARG_STOPNAME =
            AddEditFavouriteStopFragment.ARG_STOPNAME;

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
            final AddEditFavouriteStopFragment fragment =
                    AddEditFavouriteStopFragment.newInstance(
                    intent.getStringExtra(ARG_STOPCODE),
                    intent.getStringExtra(ARG_STOPNAME));

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