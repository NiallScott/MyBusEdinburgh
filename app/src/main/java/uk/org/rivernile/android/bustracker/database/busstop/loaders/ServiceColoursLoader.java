/*
 * Copyright (C) 2016 - 2017 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.database.busstop.loaders;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link ProcessedCursorLoader} is used to get a {@link Map} of service name to a colour for
 * the service. It is the same implementation as
 * {@link BusStopDatabase#getServiceColours(Context, String[])}, except suitable for a
 * {@link android.support.v4.content.Loader} which manages threading and automatic reloading of the
 * {@link Cursor} upon a data change.
 *
 * @author Niall Scott
 */
public class ServiceColoursLoader extends ProcessedCursorLoader<Map<String, Integer>> {

    private final int defaultColour;

    /**
     * Create a new {@code ServiceColoursLoader}.
     *
     * @param context A {@link Context} instance.
     * @param services A {@link String} array of services to get colours for. If this is
     * {@code null} or empty, colours for all known services will be retrieved.
     */
    public ServiceColoursLoader(@NonNull final Context context, @Nullable final String[] services) {
        super(context);

        defaultColour = ContextCompat.getColor(getContext(), R.color.colorAccent);

        setUri(BusStopContract.Services.CONTENT_URI);
        setProjection(new String[] {
                BusStopContract.Services.NAME,
                BusStopContract.Services.COLOUR
        });

        String selection = BusStopContract.Services.COLOUR + " IS NOT NULL";
        final String[] selectionArgs;

        if (services != null && services.length > 0) {
            selection += " AND " + BusStopContract.Services.NAME + " IN (" +
                    BusStopDatabase.generateInPlaceholders(services.length) + ')';
            selectionArgs = services;
        } else {
            selectionArgs = null;
        }

        setSelection(selection);
        setSelectionArgs(selectionArgs);
        setSortOrder(null);
    }

    @Nullable
    @Override
    public Map<String, Integer> processCursor(@Nullable final Cursor cursor) {
        if (cursor != null) {
            final HashMap<String, Integer> serviceColours = new HashMap<>(cursor.getCount());
            final int serviceNameColumn = cursor.getColumnIndex(BusStopContract.Services.NAME);
            final int serviceColourColumn = cursor.getColumnIndex(BusStopContract.Services.COLOUR);
            cursor.moveToPosition(-1);

            while (cursor.moveToNext()) {
                final String colourHex = cursor.getString(serviceColourColumn);
                int colour;

                if (!TextUtils.isEmpty(colourHex)) {
                    try {
                        colour = Color.parseColor(colourHex);
                    } catch (IllegalArgumentException e) {
                        colour = defaultColour;
                    }
                } else {
                    colour = defaultColour;
                }

                serviceColours.put(cursor.getString(serviceNameColumn), colour);
            }

            cursor.close();

            return Collections.unmodifiableMap(serviceColours);
        } else {
            return null;
        }
    }
}
