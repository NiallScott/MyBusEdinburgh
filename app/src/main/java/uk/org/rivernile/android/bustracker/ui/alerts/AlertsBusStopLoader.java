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

package uk.org.rivernile.android.bustracker.ui.alerts;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;

/**
 * This {@link ProcessedCursorLoader} will load bus stop information for alerts.
 *
 * @author Niall Scott
 */
class AlertsBusStopLoader extends ProcessedCursorLoader<Map<String, BusStop>> {

    /**
     * Create a new {@code AlertsBusStopLoader}.
     *
     * @param context A {@link Context} instance.
     * @param stopCodes The stop codes to load information for.
     */
    AlertsBusStopLoader(@NonNull final Context context, @NonNull final String[] stopCodes) {
        super(context, BusStopContract.BusStops.CONTENT_URI,
                new String[] {
                        BusStopContract.BusStops.STOP_CODE,
                        BusStopContract.BusStops.STOP_NAME,
                        BusStopContract.BusStops.LATITUDE,
                        BusStopContract.BusStops.LONGITUDE,
                        BusStopContract.BusStops.ORIENTATION,
                        BusStopContract.BusStops.LOCALITY
                },
                BusStopContract.BusStops.STOP_CODE + " IN (" +
                        BusStopDatabase.generateInPlaceholders(stopCodes.length) + ')',
                stopCodes, null);
    }

    @Nullable
    @Override
    public Map<String, BusStop> processCursor(@Nullable final Cursor cursor) {
        if (cursor != null) {
            final int count = cursor.getCount();

            if (count == 0) {
                return null;
            }

            final int stopCodeColumn = cursor.getColumnIndex(BusStopContract.BusStops.STOP_CODE);
            final int stopNameColumn = cursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME);
            final int latitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LATITUDE);
            final int longitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LONGITUDE);
            final int orientationColumn = cursor.getColumnIndex(
                    BusStopContract.BusStops.ORIENTATION);
            final int localityColumn = cursor.getColumnIndex(BusStopContract.BusStops.LOCALITY);

            final HashMap<String, BusStop> busStops = new HashMap<>(count);
            cursor.moveToPosition(-1);

            while (cursor.moveToNext()) {
                final String stopCode = cursor.getString(stopCodeColumn);
                busStops.put(stopCode, new BusStop(stopCode, cursor.getString(stopNameColumn),
                        cursor.getDouble(latitudeColumn), cursor.getDouble(longitudeColumn),
                        cursor.getInt(orientationColumn), cursor.getString(localityColumn)));
            }

            return busStops;
        } else {
            return null;
        }
    }
}
