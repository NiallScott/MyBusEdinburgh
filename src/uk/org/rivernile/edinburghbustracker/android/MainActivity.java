/*
 * Copyright (C) 2009 Niall 'Rivernile' Scott
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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 *
 * @author Niall Scott
 */
public class MainActivity extends ListActivity {

    /**
     * The entry point in to this activity.
     *
     * @param savedInstanceState The saved state of the application if it is
     * being relaunched.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter ad = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        ad.add(getString(R.string.main_favourite_stops));
        ad.add(getString(R.string.main_enter_stop_code));
        ad.add(getString(R.string.main_bus_stop_map));
        ad.add(getString(R.string.preferences));
        ad.add(getString(R.string.exit));
        setListAdapter(ad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        switch(position) {
            case 0:
                break;
            case 1:
                startActivity(new Intent(this, EnterStopCodeActivity.class));
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                finish();
                break;
        }
    }
}
