/*
 * Copyright (C) 2016 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.database.busstop;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains static methods to aid in dealing with the bus stop database and provides
 * consistency.
 *
 * @author Niall Scott
 */
public final class BusStopDatabase {

    /**
     * The constructor is private to prevent instantiation.
     */
    private BusStopDatabase() { }

    /**
     * Get a mapping of service to the colour attributed for the service.
     *
     * @param context A {@link Context} instance.
     * @param services A {@link String} array of services to get colours for, or {@code null} if
     * colours for all known services should be retrieved.
     * @return A {@link Map} of service name to service colour, or {@code null} if there was a
     * problem obtaining colours.
     */
    @WorkerThread
    @Nullable
    public static Map<String, String> getServiceColours(@NonNull final Context context,
            @Nullable final String[] services) {
        String selection = BusStopContract.Services.COLOUR + " IS NOT NULL";
        final String[] selectionArgs;

        if (services != null && services.length > 0) {
            selection += " AND " + BusStopContract.Services.NAME + " IN (" +
                    BusStopDatabase.generateInPlaceholders(services.length) + ')';
            selectionArgs = services;
        } else {
            selectionArgs = null;
        }

        final Cursor c = context.getContentResolver().query(BusStopContract.Services.CONTENT_URI,
                new String[] {
                        BusStopContract.Services.NAME,
                        BusStopContract.Services.COLOUR
                },
                selection, selectionArgs, null);

        if (c != null) {
            final HashMap<String, String> serviceColours = new HashMap<>(c.getCount());
            final int serviceNameColumn = c.getColumnIndex(BusStopContract.Services.NAME);
            final int serviceColourColumn = c.getColumnIndex(BusStopContract.Services.COLOUR);
            c.moveToPosition(-1);

            while (c.moveToNext()) {
                serviceColours.put(c.getString(serviceNameColumn),
                        c.getString(serviceColourColumn));
            }

            c.close();

            return Collections.unmodifiableMap(serviceColours);
        } else {
            return null;
        }
    }

    /**
     * Generate placeholders for a SQL 'IN' clause.
     *
     * @param count The number of placeholders to generate.
     * @return A {@link String} of placeholders, or empty {@link String} if {@code count} is less
     * than {@code 1}.
     */
    @NonNull
    public static String generateInPlaceholders(final int count) {
        if (count < 1) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(',');
            }

            sb.append('?');
        }

        return sb.toString();
    }
}
