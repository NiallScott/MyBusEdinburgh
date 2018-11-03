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

package uk.org.rivernile.android.bustracker.database.busstop.loaders;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;

/**
 * This {@link CursorLoader} takes a user-supplied search term and performs a search over the bus
 * stops table.
 *
 * @author Niall Scott
 */
public class BusStopSearchLoader extends CursorLoader {

    private final String searchTerm;

    /**
     * Create a new {@code BusStopSearchLoader}.
     *
     * @param context A {@link Context} instance.
     * @param searchTerm The term to use while searching the database.
     */
    public BusStopSearchLoader(@NonNull final Context context, @NonNull final String searchTerm) {
        super(context);

        this.searchTerm = searchTerm;

        final String term = '%' + searchTerm + '%';
        setUri(BusStopContract.BusStops.CONTENT_URI);
        setProjection(new String[] {
                BusStopContract.BusStops._ID,
                BusStopContract.BusStops.STOP_CODE,
                BusStopContract.BusStops.STOP_NAME,
                BusStopContract.BusStops.ORIENTATION,
                BusStopContract.BusStops.LOCALITY,
                BusStopContract.BusStops.SERVICE_LISTING
        });
        setSelection(BusStopContract.BusStops.STOP_CODE + " LIKE ? OR " +
                BusStopContract.BusStops.STOP_NAME + " LIKE ? OR " +
                BusStopContract.BusStops.LOCALITY + " LIKE ?");
        setSelectionArgs(new String[] { term, term, term });
        setSortOrder(BusStopContract.BusStops.STOP_NAME + " ASC");
    }

    /**
     * Get the search term used in this instance of this {@link CursorLoader}.
     *
     * @return The current search term.
     */
    @NonNull
    public String getSearchTerm() {
        return searchTerm;
    }
}
