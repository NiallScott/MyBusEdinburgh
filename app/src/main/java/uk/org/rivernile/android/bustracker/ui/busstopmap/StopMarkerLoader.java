/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This loader loads stops for display on the stop map.
 *
 * @author Niall Scott
 */
class StopMarkerLoader extends ProcessedCursorLoader<Map<String, Stop>> {

    /**
     * Create a new {@code StopMarkerLoader}.
     *
     * @param context A {@link Context} instance.
     * @param filteredServices Services to filter for. Set as {@code null} or empty if no filtering
     * should be applied.
     */
    StopMarkerLoader(@NonNull final Context context, @Nullable final String[] filteredServices) {
        super(context);

        setUri(BusStopContract.BusStops.CONTENT_URI);
        setProjection(new String[] {
                BusStopContract.BusStops.STOP_CODE,
                BusStopContract.BusStops.STOP_NAME,
                BusStopContract.BusStops.LATITUDE,
                BusStopContract.BusStops.LONGITUDE,
                BusStopContract.BusStops.ORIENTATION,
                BusStopContract.BusStops.LOCALITY
        });

        if (filteredServices != null && filteredServices.length > 0) {
            setSelection(BusStopContract.BusStops.STOP_CODE + " IN (" +
                    "SELECT " + BusStopContract.ServiceStops.STOP_CODE + " FROM " +
                    BusStopContract.ServiceStops.TABLE_NAME + " WHERE " +
                    BusStopContract.ServiceStops.SERVICE_NAME + " IN (" +
                    BusStopDatabase.generateInPlaceholders(filteredServices.length) + "))");
            setSelectionArgs(filteredServices);
        }
    }

    @Nullable
    @Override
    public Map<String, Stop> processCursor(@Nullable final Cursor cursor) {
        if (cursor != null) {
            final HashMap<String, Stop> result = new HashMap<>(cursor.getCount());
            final int stopCodeColumn = cursor.getColumnIndex(BusStopContract.BusStops.STOP_CODE);
            final int stopNameColumn = cursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME);
            final int latitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LATITUDE);
            final int longitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LONGITUDE);
            final int orientationColumn = cursor.getColumnIndex(
                    BusStopContract.BusStops.ORIENTATION);
            final int localityColumn = cursor.getColumnIndex(BusStopContract.BusStops.LOCALITY);
            cursor.moveToPosition(-1);

            while (cursor.moveToNext()) {
                final String stopCode = cursor.getString(stopCodeColumn);
                final String stopName = cursor.getString(stopNameColumn);
                final String locality = cursor.getString(localityColumn);
                final String title = !TextUtils.isEmpty(locality)
                        ? getContext().getString(R.string.busstop_locality, stopName, locality,
                        stopCode)
                        : getContext().getString(R.string.busstop, stopName, stopCode);
                final LatLng position = new LatLng(cursor.getDouble(latitudeColumn),
                        cursor.getDouble(longitudeColumn));
                result.put(stopCode, new Stop(position, title, stopCode,
                        cursor.getInt(orientationColumn)));
            }

            return Collections.unmodifiableMap(result);
        } else {
            return null;
        }
    }
}
