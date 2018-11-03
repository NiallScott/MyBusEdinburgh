/*
 * Copyright (C) 2016 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;

/**
 * This {@link Loader} will load nearby bus stops from the database. To
 * do this, the device's current location needs to be known.
 *
 * <p>
 *     Optionally, the bus stops can be filtered to only return bus stops served by certain
 *     services.
 * </p>
 *
 * @author Niall Scott
 */
class NearestStopsLoader extends ProcessedCursorLoader<List<SearchResult>> {

    /** If modifying for another city, check that this value is correct. */
    private static final double LATITUDE_SPAN = 0.004499;
    /** If modifying for another city, check that this value is correct. */
    private static final double LONGITUDE_SPAN = 0.008001;

    private final double latitude;
    private final double longitude;

    /**
     * Create a new {@code NearestStopsLoader}.
     *
     * @param context A {@link Context} instance.
     * @param latitude The latitude of the center point (i.e. the device location).
     * @param longitude The longitude of the center point (i.e. the device location).
     * @param filteredServices A {@link String} array of services to filter by. Only bus stops
     * served by these services will be returned. If no filtering is to take place, set to
     * {@code null}.
     */
    NearestStopsLoader(@NonNull final Context context, final double latitude,
            final double longitude, @Nullable final String[] filteredServices) {
        super(context);

        this.latitude = latitude;
        this.longitude = longitude;

        setUri(BusStopContract.BusStops.CONTENT_URI);
        setProjection(new String[] {
                BusStopContract.BusStops.STOP_CODE,
                BusStopContract.BusStops.STOP_NAME,
                BusStopContract.BusStops.LATITUDE,
                BusStopContract.BusStops.LONGITUDE,
                BusStopContract.BusStops.ORIENTATION,
                BusStopContract.BusStops.LOCALITY,
                BusStopContract.BusStops.SERVICE_LISTING
        });

        // Calculate the bounds.
        final double minLatitude = latitude - LATITUDE_SPAN;
        final double minLongitude = longitude - LONGITUDE_SPAN;
        final double maxLatitude = latitude + LATITUDE_SPAN;
        final double maxLongitude = longitude + LONGITUDE_SPAN;

        String selection = '(' + BusStopContract.BusStops.LATITUDE + " BETWEEN ? AND ?) AND " +
                '(' + BusStopContract.BusStops.LONGITUDE + " BETWEEN ? AND ?)";
        final String[] baseArgs = new String[] {
                String.valueOf(minLatitude),
                String.valueOf(maxLatitude),
                String.valueOf(minLongitude),
                String.valueOf(maxLongitude)
        };

        if (filteredServices != null && filteredServices.length > 0) {
            selection += " AND " + BusStopContract.BusStops.STOP_CODE + " IN (SELECT " +
                    BusStopContract.ServiceStops.STOP_CODE + " FROM " +
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
    public List<SearchResult> processCursor(@Nullable final Cursor cursor) {
        final ArrayList<SearchResult> result = new ArrayList<>();

        // Defensive programming!
        if (cursor != null) {
            final int stopCodeColumn = cursor.getColumnIndex(BusStopContract.BusStops.STOP_CODE);
            final int stopNameColumn = cursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME);
            final int latitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LATITUDE);
            final int longitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LONGITUDE);
            final int orientationColumn = cursor.getColumnIndex(
                    BusStopContract.BusStops.ORIENTATION);
            final int localityColumn = cursor.getColumnIndex(BusStopContract.BusStops.LOCALITY);
            final int servicesColumn = cursor.getColumnIndex(
                    BusStopContract.BusStops.SERVICE_LISTING);
            cursor.moveToPosition(-1);

            // We don't care about the bearings so a float array of only 1 in size is required.
            final float[] distance = new float[1];

            // Loop through all results.
            while (cursor.moveToNext()) {
                // Use the Location class in the Android framework to compute the distance
                // between the handset and the bus stop.
                Location.distanceBetween(latitude, longitude,
                        cursor.getDouble(latitudeColumn), cursor.getDouble(longitudeColumn),
                        distance);
                final String stopCode = cursor.getString(stopCodeColumn);

                try {
                    // Create a new SearchResult and add it to the results list.
                    result.add(new SearchResult(stopCode, cursor.getString(stopNameColumn),
                            cursor.getString(servicesColumn), distance[0],
                            cursor.getInt(orientationColumn), cursor.getString(localityColumn)));
                } catch (IllegalArgumentException ignored) {
                    // Nothing to do here. Don't add the item if it doesn't meet the minimum data
                    // requirements.
                }
            }
        }

        // Sort the bus stop results in order of distance from the device.
        Collections.sort(result);

        return Collections.unmodifiableList(result);
    }
}
