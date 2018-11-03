/*
 * Copyright (C) 2015 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.database.settings;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

/**
 * This {@link ContentProvider} provides the user's favourite saved stops and any alerts set by the
 * user. For interacting with this {@link ContentProvider}, see the contract defined at
 * {@link SettingsContract}.
 *
 * @author Niall Scott
 */
public class SettingsProvider extends ContentProvider {

    private static final int FAVOURITES = 1;
    private static final int FAVOURITES_ID = 2;
    private static final int ALERTS = 3;
    private static final int ALERTS_ID = 4;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SettingsOpenHelper openHelper;

    static {
        uriMatcher.addURI(SettingsContract.AUTHORITY, SettingsContract.Favourites.TABLE_NAME,
                FAVOURITES);
        uriMatcher.addURI(SettingsContract.AUTHORITY, SettingsContract.Favourites.TABLE_NAME +
                "/#", FAVOURITES_ID);
        uriMatcher.addURI(SettingsContract.AUTHORITY, SettingsContract.Alerts.TABLE_NAME, ALERTS);
        uriMatcher.addURI(SettingsContract.AUTHORITY, SettingsContract.Alerts.TABLE_NAME + "/#",
                ALERTS_ID);
    }

    @Override
    public boolean onCreate() {
        openHelper = new SettingsOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public synchronized Cursor query(@NonNull final Uri uri, final String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        String table;

        switch (uriMatcher.match(uri)) {
            case FAVOURITES:
                table = SettingsContract.Favourites.TABLE_NAME;

                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = SettingsContract.Favourites.STOP_NAME + " ASC";
                }

                break;
            case FAVOURITES_ID:
                table = SettingsContract.Favourites.TABLE_NAME;
                selection = SettingsContract.Favourites._ID + " = ?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            case ALERTS:
                table = SettingsContract.Alerts.TABLE_NAME;
                break;
            case ALERTS_ID:
                table = SettingsContract.Alerts.TABLE_NAME;
                selection = SettingsContract.Alerts._ID + " = ?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        final Cursor c = openHelper.getReadableDatabase()
                .query(table, projection, selection,selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull final Uri uri) {
        switch (uriMatcher.match(uri)) {
            case FAVOURITES:
                return SettingsContract.Favourites.CONTENT_TYPE;
            case FAVOURITES_ID:
                return SettingsContract.Favourites.CONTENT_ITEM_TYPE;
            case ALERTS:
                return SettingsContract.Alerts.CONTENT_TYPE;
            case ALERTS_ID:
                return SettingsContract.Alerts.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public synchronized Uri insert(@NonNull final Uri uri, final ContentValues values) {
        String table;

        switch (uriMatcher.match(uri)) {
            case FAVOURITES:
                table = SettingsContract.Favourites.TABLE_NAME;
                break;
            case ALERTS:
                table = SettingsContract.Alerts.TABLE_NAME;
                break;
            case FAVOURITES_ID:
            case ALERTS_ID:
                throw new IllegalArgumentException("Insert not supported on URI " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        final long id = openHelper.getWritableDatabase().insertOrThrow(table, null, values);
        final Uri result = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(uri, null, false);

        return result;
    }

    @Override
    public synchronized int delete(@NonNull final Uri uri, String selection,
            String[] selectionArgs) {
        String table;

        switch (uriMatcher.match(uri)) {
            case FAVOURITES:
                table = SettingsContract.Favourites.TABLE_NAME;
                break;
            case FAVOURITES_ID:
                table = SettingsContract.Favourites.TABLE_NAME;
                selection = SettingsContract.Favourites._ID + " = ?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            case ALERTS:
                table = SettingsContract.Alerts.TABLE_NAME;
                break;
            case ALERTS_ID:
                table = SettingsContract.Alerts.TABLE_NAME;
                selection = SettingsContract.Alerts._ID + " = ?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        final int count = openHelper.getWritableDatabase().delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null, false);

        return count;
    }

    @Override
    public synchronized int update(@NonNull final Uri uri, final ContentValues values,
            String selection, String[] selectionArgs) {
        String table;

        switch (uriMatcher.match(uri)) {
            case FAVOURITES:
                table = SettingsContract.Favourites.TABLE_NAME;
                break;
            case FAVOURITES_ID:
                table = SettingsContract.Favourites.TABLE_NAME;
                selection = SettingsContract.Favourites._ID + " = ?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                break;
            case ALERTS:
            case ALERTS_ID:
                throw new IllegalArgumentException("Update not supported on URI " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        final int count = openHelper.getWritableDatabase().update(table, values, selection,
                selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null, false);

        return count;
    }
}
