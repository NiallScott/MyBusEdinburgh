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
 * 1. This notice may not be removed or altered from any file it appears in.
 *
 * 2. Any modifications made to this software, except those defined in
 *    clause 3 of this agreement, must be released under this license, and
 *    the source code of any modifications must be made available on a
 *    publically accessible (and locateable) website, or sent to the
 *    original author of this software.
 *
 * 3. Software modifications that do not alter the functionality of the
 *    software but are simply adaptations to a specific environment are
 *    exempt from clause 2.
 */

package uk.org.rivernile.android.bustracker.database.busstop.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;

/**
 * This class is used to load the names of all services that are in the bus stop database. The
 * returned {@link android.database.Cursor} will only have the
 * {@link BusStopContract.Services#NAME} column. The rows will be ordered alphanumerically.
 *
 * @author Niall Scott
 */
public class AllServiceNamesLoader extends CursorLoader {

    private String[] services;

    /**
     * Create a new {@code AllServiceNamesLoader}.
     *
     * @param context A {@link Context} instance.
     */
    public AllServiceNamesLoader(@NonNull final Context context) {
        super(context, BusStopContract.Services.CONTENT_URI,
                new String[] { BusStopContract.Services.NAME }, null, null,
                BusStopDatabase.getServicesSortByCondition(BusStopContract.Services.NAME));
    }

    @Override
    public Cursor loadInBackground() {
        final Cursor c = super.loadInBackground();

        if (c != null) {
            final int count = c.getCount();
            final int serviceNameColumn = c.getColumnIndex(BusStopContract.Services.NAME);
            services = new String[c.getCount()];

            for (int i = 0; i < count; i++) {
                if (c.moveToPosition(i)) {
                    services[i] = c.getString(serviceNameColumn);
                }
            }
        } else {
            services = null;
        }

        return c;
    }

    /**
     * Get the pre-processed services array. This could be {@code null}.
     *
     * @return The pre-processed services array.
     */
    @Nullable
    public String[] getServices() {
        return services;
    }
}
