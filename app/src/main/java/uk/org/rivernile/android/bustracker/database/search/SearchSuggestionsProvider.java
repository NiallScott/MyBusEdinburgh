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

package uk.org.rivernile.android.bustracker.database.search;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import androidx.annotation.Nullable;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.edinburghbustracker.android.BuildConfig;

/**
 * This {@link android.content.ContentProvider} provides suggested search items when the user begins
 * typing a search term in to the search box.
 *
 * @author Niall Scott
 */
public class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider {

    /** The authority to use. */
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID +
            ".SearchSuggestionsProvider";

    /** The database modes. */
    public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    private static final String[] COLUMNS = new String[] {
            SearchManager.SUGGEST_COLUMN_FORMAT,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_QUERY,
            BaseColumns._ID
    };

    /**
     * Create a new {@code SearchSuggestionsProvider}. As per the API documentation, this sets up
     * the suggestions.
     */
    public SearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection,
            final String[] selectionArgs, final String sortOrder) {
        final Cursor recentCursor = super.query(uri, projection, selection, selectionArgs,
                sortOrder);

        // If there's no selection args, then just return the recent searches cursor.
        if (selectionArgs == null || selectionArgs.length == 0 ||
                TextUtils.isEmpty(selectionArgs[0])) {
            return recentCursor;
        }

        // This is so that the _id column is unique.
        final int recentLastIndex = recentCursor != null ? recentCursor.getCount() : 0;
        final Cursor suggestionsCursor = getSuggestionsCursor(selectionArgs[0], recentLastIndex);

        return suggestionsCursor != null
                ? new MergeCursor(new Cursor[] { recentCursor, suggestionsCursor })
                : recentCursor;
    }

    /**
     * Get a {@link Cursor} which contains suggested search items.
     *
     * @param searchTerm What search term the user has entered so far.
     * @param startIndex What index to start items from so it does not collide with other suggestion
     * items.
     * @return A {@link Cursor} which contains suggested search items, or {@code null} if there was
     * a problem.
     */
    @Nullable
    private Cursor getSuggestionsCursor(@Nullable final String searchTerm, final int startIndex) {
        final String selection;
        final String[] selectionArgs;

        if (!TextUtils.isEmpty(searchTerm)) {
            selection = BusStopContract.BusStops.LOCALITY + " LIKE ?";
            selectionArgs = new String[] { '%' + searchTerm + '%' };
        } else {
            selection = null;
            selectionArgs = null;
        }

        final Cursor localityCursor = getContext().getContentResolver().query(
                BusStopContract.BusStops.CONTENT_URI,
                new String[] { "DISTINCT " + BusStopContract.BusStops.LOCALITY }, selection,
                selectionArgs, BusStopContract.BusStops.LOCALITY + " ASC");

        if (localityCursor != null) {
            final int localityColumn = localityCursor.getColumnIndex(
                    BusStopContract.BusStops.LOCALITY);
            final MatrixCursor result = new MatrixCursor(COLUMNS);
            final int count = localityCursor.getCount();

            for (int i = 0; i < count; i++) {
                localityCursor.moveToPosition(i);
                final String locality = localityCursor.getString(localityColumn);

                result.addRow(new Object[] {
                        null,
                        "android.resource://" + getContext().getPackageName() + '/' +
                                android.R.color.transparent,
                        locality,
                        null,
                        locality,
                        (startIndex + i)
                });
            }

            localityCursor.close();

            return result;
        } else {
            return null;
        }
    }
}
