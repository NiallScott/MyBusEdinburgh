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

package uk.org.rivernile.android.bustracker.ui.neareststops;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.org.rivernile.android.fetchutils.loaders.support.SimpleAsyncTaskLoader;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;

/**
 * This {@link android.support.v4.content.Loader} will load nearby bus stops from the database. To
 * do this, the device's current location needs to be known.
 *
 * <p>
 *     Optionally, the bus stops can be filtered to only return bus stops served by certain
 *     services.
 * </p>
 *
 * @author Niall Scott
 */
public class NearestStopsLoader extends SimpleAsyncTaskLoader<List<SearchResult>> {

    /** If modifying for another city, check that this value is correct. */
    private static final double LATITUDE_SPAN = 0.004499;
    /** If modifying for another city, check that this value is correct. */
    private static final double LONGITUDE_SPAN = 0.008001;

    private final BusStopDatabase bsd;

    private final double latitude;
    private final double longitude;
    private final String[] filteredServices;

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
    public NearestStopsLoader(@NonNull final Context context, final double latitude,
            final double longitude, @Nullable final String[] filteredServices) {
        super(context);

        bsd = BusStopDatabase.getInstance(context.getApplicationContext());
        this.latitude = latitude;
        this.longitude = longitude;
        this.filteredServices = filteredServices;
    }

    @Override
    public List<SearchResult> loadInBackground() {
        // Create the List where the results will be placed. If no results have been found, this
        // list will be empty.
        final ArrayList<SearchResult> result = new ArrayList<>();

        // Calculate the bounds.
        final double minX = latitude - LATITUDE_SPAN;
        final double minY = longitude - LONGITUDE_SPAN;
        final double maxX = latitude + LATITUDE_SPAN;
        final double maxY = longitude + LONGITUDE_SPAN;

        final Cursor c;
        // What query is executed depends on whether services are being filtered or not.
        if (filteredServices != null && filteredServices.length > 0) {
            c = bsd.getFilteredStopsByCoords(minX, minY, maxX, maxY, filteredServices);
        } else {
            c = bsd.getBusStopsByCoords(minX, minY, maxX, maxY);
        }

        // Defensive programming!
        if (c != null) {
            // We don't care about the bearings so a float array of only 1 in size is required.
            final float[] distance = new float[1];
            distance[0] = 0f;

            // Loop through all results.
            while (c.moveToNext()) {
                // Use the Location class in the Android framework to compute the
                // distance between the handset and the bus stop.
                Location.distanceBetween(latitude, longitude, c.getDouble(2), c.getDouble(3),
                        distance);
                final String stopCode = c.getString(0);

                try {
                    // Create a new SearchResult and add it to the results list.
                    result.add(new SearchResult(stopCode, c.getString(1),
                            bsd.getBusServicesForStopAsString(stopCode), distance[0], c.getInt(4),
                            c.getString(5)));
                } catch (IllegalArgumentException ignored) {
                    // Nothing to do here. Don't add the item if it doesn't meet the minimum data
                    // requirements.
                }
            }

            // Cursor is no longer needed, free the resource.
            c.close();
        }

        // Sort the bus stop results in order of distance from the device.
        Collections.sort(result);

        return Collections.unmodifiableList(result);
    }
}
