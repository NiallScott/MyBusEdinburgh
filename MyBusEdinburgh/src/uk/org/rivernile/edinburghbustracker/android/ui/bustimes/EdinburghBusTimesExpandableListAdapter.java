/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.ui.bustimes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.widget.TextView;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.ui.bustimes
        .BusTimesExpandableListAdapter;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;

/**
 * This is an Edinburgh-specific implementation of
 * {@link BusTimesExpandableListAdapter}, which shows bus times in an expandable
 * list. This specific implementation colours night buses properly.
 * 
 * @author Niall Scott
 */
public class EdinburghBusTimesExpandableListAdapter
        extends BusTimesExpandableListAdapter {
    
    /**
     * Create a new EdinburghBusTimesExpandableListAdapter.
     * 
     * @param context A Context instance.
     */
    public EdinburghBusTimesExpandableListAdapter(final Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateServiceName(final TextView txtBusService,
            final LiveBusService busService) {
        super.populateServiceName(txtBusService, busService);
        
        final String serviceName = busService.getServiceName();
        if (isNightService(serviceName)) {
            final GradientDrawable background;
            
            try {
                background = (GradientDrawable) txtBusService.getBackground();
            } catch (ClassCastException e) {
                return;
            }
            
            background.setColor(Color.BLACK);
            txtBusService.setText(BusStopDatabase
                    .getColouredServiceListString(serviceName));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isNightService(final String serviceName) {
        return !TextUtils.isEmpty(serviceName) ?
                serviceName.startsWith("N") : false;
    }
}