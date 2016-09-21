/*
 * Copyright (C) 2013 - 2016 Niall 'Rivernile' Scott
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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;

/**
 * This {@link ProcessedCursorLoader} loads all the route lines for given route names and creates
 * {@link PolylineOptions} objects for each polyline. Each bus service can have more than one
 * polyline (known as "chainages"). This is why each service has a {@link List} of
 * {@link PolylineOptions} objects.
 * 
 * @author Niall Scott
 */
public class RouteLineLoader extends ProcessedCursorLoader<Map<String, List<PolylineOptions>>> {
    
    private final String[] services;
    
    /**
     * Create a new {@code RouteLineLoader}.
     * 
     * @param context A {@link Context} instance.
     * @param services A {@link String} array of service names to load route lines for.
     */
    public RouteLineLoader(@NonNull final Context context, @NonNull final String[] services) {
        super(context, BusStopContract.ServicePoints.CONTENT_URI,
                new String[] {
                        BusStopContract.ServicePoints.SERVICE_NAME,
                        BusStopContract.ServicePoints.CHAINAGE,
                        BusStopContract.ServicePoints.LATITUDE,
                        BusStopContract.ServicePoints.LONGITUDE
                },
                BusStopContract.ServicePoints.SERVICE_NAME + " IN (?)",
                new String[] {
                        BusStopDatabase.convertArrayToInParameter(services)
                },
                BusStopContract.ServicePoints.SERVICE_NAME + " ASC, "
                        + BusStopContract.ServicePoints.CHAINAGE + " ASC, "
                        + BusStopContract.ServicePoints.ORDER_VALUE + " ASC");
        
        this.services = services;
    }

    @Nullable
    @Override
    public Map<String, List<PolylineOptions>> processCursor(@Nullable final Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        final int count = cursor.getCount();

        if (count < 1) {
            return null;
        }

        final HashMap<String, List<PolylineOptions>> result = new HashMap<>();
        final int columnServiceName = cursor.getColumnIndex(
                BusStopContract.ServicePoints.SERVICE_NAME);
        final int columnChainage = cursor.getColumnIndex(BusStopContract.ServicePoints.CHAINAGE);
        final int columnLatitude = cursor.getColumnIndex(BusStopContract.ServicePoints.LATITUDE);
        final int columnLongitude = cursor.getColumnIndex(BusStopContract.ServicePoints.LONGITUDE);
        cursor.moveToPosition(-1);
        final Map<String, String> serviceColours =
                BusStopDatabase.getServiceColours(getContext(), services);

        String currentService = "";
        LinkedList<PolylineOptions> currentPolylineList = null;
        PolylineOptions polylineOptions = null;
        int currentColour = Color.BLACK;
        int currentChainage = -1;

        while (cursor.moveToNext()) {
            final String service = cursor.getString(columnServiceName);

            if (!currentService.equals(service)) {
                currentService = service;
                currentPolylineList = new LinkedList<>();
                result.put(currentService, Collections.unmodifiableList(currentPolylineList));
                currentColour = getColourForService(serviceColours, service);
                currentChainage = -1;
            }

            final int chainage = cursor.getInt(columnChainage);

            if (chainage != currentChainage) {
                polylineOptions = new PolylineOptions();
                polylineOptions.color(currentColour);
                currentChainage = chainage;

                if (currentPolylineList != null) {
                    currentPolylineList.add(polylineOptions);
                }
            }

            if (polylineOptions != null) {
                polylineOptions.add(new LatLng(cursor.getDouble(columnLatitude),
                        cursor.getDouble(columnLongitude)));
            }
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * This is a private utility method to get a colour for a service from a {@link Map} of service
     * names to colours.
     *
     * @param colours The {@link Map} of service name to the colour associated with the service.
     * @param serviceName The name of the service to obtain a colour for.
     * @return An {@code int} which represents the colour for the service. This will be
     * {@link Color#BLACK} if the service name is {@code null} or empty, if the service to colour
     * mapping is {@code null} or empty, if there is no colour for the service or if there was a
     * problem in getting a colour for the service.
     */
    @ColorInt
    private static int getColourForService(@Nullable final Map<String, String> colours,
            @Nullable final String serviceName) {
        if (TextUtils.isEmpty(serviceName) || colours == null || colours.isEmpty()) {
            return Color.BLACK;
        }

        final String hex = colours.get(serviceName);

        if (TextUtils.isEmpty(hex)) {
            return Color.BLACK;
        }

        try {
            return Color.parseColor(hex);
        } catch (IllegalArgumentException e) {
            return Color.BLACK;
        }
    }
}