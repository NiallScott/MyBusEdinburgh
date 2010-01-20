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

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The DisplayStopDataActivity displays the bus stop information to the user
 * once they have selected a bus stop to view data for in an ExpandableListView.
 *
 * @author Niall Scott
 */
public class DisplayStopDataActivity extends ExpandableListActivity {

    private final static int AUTO_REFRESH_ID = Menu.FIRST;
    private final static int REFRESH_ID = Menu.FIRST + 1;

    public final static String ACTION_VIEW_STOP_DATA =
            "uk.org.rivernile.edinburghbustracker.android." +
            "ACTION_VIEW_STOP_DATA";

    private boolean autoRefresh = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.displaystopdata_title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, AUTO_REFRESH_ID, 1,
                R.string.displaystopdata_menu_turnautorefreshon);
        menu.add(0, REFRESH_ID, 2, R.string.displaystopdata_menu_refresh);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case AUTO_REFRESH_ID:
                handleAutoRefreshMenuItem(item);
                break;
            case REFRESH_ID:
                // TODO: Add refresh code
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle the auto-refresh menu item click event. This alternates between
     * turning it on and off.
     *
     * @param item The menu item object of the auto-refresh menu item. Used to
     * edit its text content.
     */
    private void handleAutoRefreshMenuItem(final MenuItem item) {
        if(autoRefresh) {
            autoRefresh = false;
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshon);
        } else {
            autoRefresh = true;
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshoff);
        }
    }
}