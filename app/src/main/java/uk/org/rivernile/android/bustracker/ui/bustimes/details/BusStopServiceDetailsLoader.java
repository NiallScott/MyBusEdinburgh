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

package uk.org.rivernile.android.bustracker.ui.bustimes.details;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link android.support.v4.content.Loader} is used to load services that serve a given
 * {@code stopCode}, along with a description of the service and a colour attributed to the service.
 *
 * @author Niall Scott
 */
class BusStopServiceDetailsLoader extends ProcessedCursorLoader<List<Service>> {

    /**
     * Create a new {@code BusStopServiceDetailsLoader}.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The stop code to get services for.
     */
    BusStopServiceDetailsLoader(@NonNull final Context context, @NonNull final String stopCode) {
        super(context, BusStopContract.ServiceStops.CONTENT_URI,
                new String[] {
                        BusStopContract.ServiceStops.SERVICE_NAME
                },
                BusStopContract.ServiceStops.STOP_CODE + " = ?",
                new String[] {
                        stopCode
                }, null);
    }

    @Nullable
    @Override
    public List<Service> processCursor(@Nullable final Cursor cursor) {
        final String[] services = getServicesFromCursor(cursor);

        if (services != null) {
            final int defaultColour = ContextCompat.getColor(getContext(), R.color.colorAccent);
            final Cursor servicesCursor = getContext().getContentResolver().query(
                    BusStopContract.Services.CONTENT_URI,
                    new String[] {
                            BusStopContract.Services.NAME,
                            BusStopContract.Services.DESCRIPTION,
                            BusStopContract.Services.COLOUR
                    },
                    BusStopContract.Services.NAME + " IN (" +
                            BusStopDatabase.generateInPlaceholders(services.length) + ')',
                    services,
                    BusStopDatabase.getServicesSortByCondition(BusStopContract.Services.NAME));

            return getServiceDetailsFromCursor(servicesCursor, defaultColour);
        }

        return null;
    }

    /**
     * Return a {@link String} array representation of the services in the original "services for
     * stops" {@link Cursor}.
     *
     * @param cursor The {@link Cursor} containing the services for the loaded stop.
     * @return A {@link String} array representation of the services in the {@link Cursor}, or
     * {@code null} if the {@link Cursor} is {@code null} or empty.
     */
    @Nullable
    private static String[] getServicesFromCursor(@Nullable final Cursor cursor) {
        if (cursor != null) {
            final int count = cursor.getCount();

            if (count > 0) {
                final int serviceNameColumn =
                        cursor.getColumnIndex(BusStopContract.ServiceStops.SERVICE_NAME);
                final String[] result = new String[count];

                for (int i = 0; i < count; i++) {
                    if (cursor.moveToPosition(i)) {
                        result[i] = cursor.getString(serviceNameColumn);
                    }
                }

                return result;
            }
        }

        return null;
    }

    /**
     * Given a {@link Cursor} containing services and information related to them, process the
     * {@link Cursor} to convert the data in to a model representation.
     *
     * @param cursor The {@link Cursor} to process.
     * @param defaultColour If a service does not have an attributed colour, use this colour
     * instead.
     * @return A {@link List} of {@link Service}s representing the data in the {@link Cursor}, or
     * {@code null} if the {@link Cursor} is {@code null}.
     */
    @Nullable
    private static List<Service> getServiceDetailsFromCursor(@Nullable final Cursor cursor,
            @ColorInt final int defaultColour) {
        if (cursor != null) {
            final int count = cursor.getCount();

            if (count > 0) {
                final int serviceNameColumn = cursor.getColumnIndex(
                        BusStopContract.Services.NAME);
                final int descriptionColumn = cursor.getColumnIndex(
                        BusStopContract.Services.DESCRIPTION);
                final int colourColumn = cursor.getColumnIndex(BusStopContract.Services.COLOUR);
                final ArrayList<Service> services = new ArrayList<>(count);
                cursor.moveToPosition(-1);

                while (cursor.moveToNext()) {
                    final String colourHex = cursor.getString(colourColumn);
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

                    services.add(new Service(cursor.getString(serviceNameColumn),
                            cursor.getString(descriptionColumn), colour));
                }

                return Collections.unmodifiableList(services);
            }

            // As this Cursor isn't managed by the Loader, we can close it straight away.
            cursor.close();
        }

        return null;
    }
}
