/*
 * Copyright (C) 2012 - 2016 Niall 'Rivernile' Scott
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
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.MapsUtils;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link ProcessedCursorLoader} retrieves the bus stops for a given area from the bus stop
 * database and outputs the bus stops as a {@link Map} of {@link MarkerOptions} objects.
 * 
 * Optionally, a service filter can be specified where only services that are contained in the
 * filter are returned.
 * 
 * @author Niall Scott
 */
public class BusStopMarkerLoader extends ProcessedCursorLoader<Map<String, MarkerOptions>> {

    /**
     * Create a new {@code BusStopMarkerLoader}.
     *
     * @param context A {@link Context} instance.
     * @param minLatitude The minimum latitude to load bus stops for.
     * @param maxLatitude The maximum latitude to load bus stops for.
     * @param minLongitude The minimum longitude to load bus stops for.
     * @param maxLongitude The maxumum latitude to load bus stops for.
     * @param filteredServices An optional {@link String} array of services to filter for.
     */
    public BusStopMarkerLoader(@NonNull final Context context, final double minLatitude,
            final double maxLatitude, final double minLongitude, final double maxLongitude,
            @Nullable final String[] filteredServices) {
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

        String selection = '(' + BusStopContract.BusStops.LATITUDE + " BETWEEN ? AND ?) AND " +
                '(' + BusStopContract.BusStops.LONGITUDE + " BETWEEN ? AND ?)";
        final String[] baseArgs = new String[] {
                String.valueOf(minLatitude),
                String.valueOf(maxLatitude),
                String.valueOf(minLongitude),
                String.valueOf(maxLongitude),
        };

        if (filteredServices != null && filteredServices.length > 0) {
            selection += " AND " + BusStopContract.BusStops.STOP_CODE + " IN (" +
                    "SELECT " + BusStopContract.ServiceStops.STOP_CODE + " FROM " +
                    BusStopContract.ServiceStops.TABLE_NAME + " WHERE " +
                    BusStopContract.ServiceStops.SERVICE_NAME + " IN (" +
                    BusStopDatabase.generateInPlaceholders(filteredServices.length) + "))";
            final String[] selectionArgs = new String[baseArgs.length + filteredServices.length];
            System.arraycopy(baseArgs, 0, selectionArgs, 0, baseArgs.length);
            System.arraycopy(filteredServices, 0, selectionArgs, baseArgs.length,
                    filteredServices.length);
            setSelectionArgs(selectionArgs);
        } else {
            setSelectionArgs(baseArgs);
        }

        setSelection(selection);
    }

    @Nullable
    @Override
    public Map<String, MarkerOptions> processCursor(@Nullable final Cursor cursor) {
        if (cursor != null) {
            final HashMap<String, MarkerOptions> result = new HashMap<>(cursor.getCount());
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

                final MarkerOptions mo = new MarkerOptions();
                mo.draggable(false);
                mo.anchor(0.5f, 1.f);
                mo.position(new LatLng(cursor.getDouble(latitudeColumn),
                        cursor.getDouble(longitudeColumn)));
                mo.title(title);
                MapsUtils.applyStopDirectionToMarker(mo, cursor.getInt(orientationColumn));
                result.put(stopCode, mo);
            }

            return result;
        } else {
            return null;
        }
    }
}