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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;

/**
 * This {@link android.support.v4.content.Loader} will load lists of service bus services for the
 * given array of stop codes and will return this data as a {@link Map} through
 * {@link ProcessedCursorLoader.ResultWrapper}.
 *
 * @author Niall Scott
 */
public class BusStopServicesLoader extends ProcessedCursorLoader<Map<String, String>> {

    /**
     * Create a new {@code BusStopServicesLoader}.
     *
     * @param context A {@link Context} instance.
     * @param busStops The array of bus stop codes to get service listings for.
     */
    public BusStopServicesLoader(@NonNull final Context context,
            @NonNull final String[] busStops) {
        super(context, BusStopContract.BusStops.CONTENT_URI,
                new String[] {
                        BusStopContract.BusStops.STOP_CODE,
                        BusStopContract.BusStops.SERVICE_LISTING
                },
                BusStopContract.BusStops.STOP_CODE + " IN (" +
                        BusStopDatabase.generateInPlaceholders(busStops.length) + ')',
                busStops, null);
    }

    @Nullable
    @Override
    public Map<String, String> processCursor(@Nullable final Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        final int count = cursor.getCount();

        if (count <= 0) {
            return null;
        }

        final int columnStopCode = cursor.getColumnIndex(BusStopContract.BusStops.STOP_CODE);
        final int columnServices = cursor.getColumnIndex(BusStopContract.BusStops.SERVICE_LISTING);
        final HashMap<String, String> stopServicesMapping = new HashMap<>(count);
        cursor.moveToPosition(-1);

        while (cursor.moveToNext()) {
            stopServicesMapping.put(cursor.getString(columnStopCode),
                    cursor.getString(columnServices));
        }

        return Collections.unmodifiableMap(stopServicesMapping);
    }
}
