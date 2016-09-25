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

package uk.org.rivernile.android.bustracker.database.busstop;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link ContentProvider} provides information on bus stops and services within the network
 * covered by this application. For interacting with this {@link ContentProvider}, see the contract
 * defined at {@link BusStopContract}.
 *
 * @author Niall Scott
 */
public class BusStopProvider extends ContentProvider {

    private static final int DATABASE_INFORMATION = 1;
    private static final int SERVICES = 2;
    private static final int BUS_STOPS = 3;
    private static final int SERVICE_STOPS = 4;
    private static final int SERVICE_POINTS = 5;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private BusStopOpenHelper openHelper;

    static {
        URI_MATCHER.addURI(BusStopContract.AUTHORITY,
                BusStopContract.DatabaseInformation.TABLE_NAME, DATABASE_INFORMATION);
        URI_MATCHER.addURI(BusStopContract.AUTHORITY,
                BusStopContract.Services.TABLE_NAME, SERVICES);
        URI_MATCHER.addURI(BusStopContract.AUTHORITY,
                BusStopContract.BusStops.TABLE_NAME, BUS_STOPS);
        URI_MATCHER.addURI(BusStopContract.AUTHORITY,
                BusStopContract.ServiceStops.TABLE_NAME, SERVICE_STOPS);
        URI_MATCHER.addURI(BusStopContract.AUTHORITY,
                BusStopContract.ServicePoints.TABLE_NAME, SERVICE_POINTS);
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        final long assetVersion = Long.parseLong(context.getString(R.string.asset_db_version));
        openHelper = new BusStopOpenHelper(context, assetVersion);
        return true;
    }

    @Nullable
    @Override
    public synchronized Cursor query(@NonNull final Uri uri, @Nullable final String[] projection,
            @Nullable final String selection, @Nullable final String[] selectionArgs,
            @Nullable final String sortOrder) {
        final Cursor c;

        switch (URI_MATCHER.match(uri)) {
            case DATABASE_INFORMATION:
                c = doDatabaseInformationQuery(projection, selection, selectionArgs, sortOrder);
                break;
            case SERVICES:
                c = doServicesQuery(projection, selection, selectionArgs, sortOrder);
                break;
            case BUS_STOPS:
                c = doBusStopsQuery(projection, selection, selectionArgs, sortOrder);
                break;
            case SERVICE_STOPS:
                c = doServiceStopsQuery(projection, selection, selectionArgs, sortOrder);
                break;
            case SERVICE_POINTS:
                c = doServicePointsQuery(projection, selection, selectionArgs, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull final Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case DATABASE_INFORMATION:
                return BusStopContract.DatabaseInformation.CONTENT_TYPE;
            case SERVICES:
                return BusStopContract.Services.CONTENT_TYPE;
            case BUS_STOPS:
                return BusStopContract.BusStops.CONTENT_TYPE;
            case SERVICE_STOPS:
                return BusStopContract.ServiceStops.CONTENT_TYPE;
            case SERVICE_POINTS:
                return BusStopContract.ServicePoints.CONTENT_TYPE;
            default:
                return null;
        }
    }

    /**
     * This {@link ContentProvider} does not support modification. All calls to this method will
     * throw {@link UnsupportedOperationException}.
     *
     * @param uri Irrelevant. Don't call this method.
     * @param values Irrelevant. Don't call this method.
     * @return Irrelevant. Don't call this method.
     * @throws UnsupportedOperationException When this method is called.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        throw new UnsupportedOperationException("Insert is not supported.");
    }

    /**
     * This {@link ContentProvider} does not support modification. All calls to this method will
     * throw {@link UnsupportedOperationException}.
     *
     * @param uri Irrelevant. Don't call this method.
     * @param selection Irrelevant. Don't call this method.
     * @param selectionArgs Irrelevant. Don't call this method.
     * @return Irrelevant. Don't call this method.
     * @throws UnsupportedOperationException When this method is called.
     */
    @Override
    public int delete(@NonNull final Uri uri, final String selection,
            final String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete is not supported.");
    }

    /**
     * This {@link ContentProvider} does not support modification. All calls to this method will
     * throw {@link UnsupportedOperationException}.
     *
     * @param uri Irrelevant. Don't call this method.
     * @param values Irrelevant. Don't call this method.
     * @param selection Irrelevant. Don't call this method.
     * @param selectionArgs Irrelevant. Don't call this method.
     * @return Irrelevant. Don't call this method.
     * @throws UnsupportedOperationException When this method is called.
     */
    @Override
    public int update(@NonNull final Uri uri, final ContentValues values, final String selection,
            final String[] selectionArgs) {
        throw new UnsupportedOperationException("Update is not supported.");
    }

    @Nullable
    @Override
    public Bundle call(@NonNull final String method, @Nullable final String arg,
            @Nullable final Bundle extras) {
        if (BusStopContract.METHOD_REPLACE_DATABASE.equals(method)) {
            replaceDatabase(arg);
            return null;
        } else {
            return super.call(method, arg, extras);
        }
    }

    /**
     * Do a query against the database for database information.
     *
     * @param projection The columns to return.
     * @param selection Should always be {@code null}.
     * @param selectionArgs Should always be {@code null}.
     * @param sortOrder Should always be {@code null}.
     * @return A {@link Cursor} which is the result of the query.
     * @throws IllegalArgumentException When any of {@code selection}, {@code selectionArgs} or
     * {@code sortOrder} are non-{@code null}.
     */
    @NonNull
    private Cursor doDatabaseInformationQuery(@Nullable final String[] projection,
            @Nullable final String selection, @Nullable final String[] selectionArgs,
            @Nullable final String sortOrder) {
        if (selection != null || selectionArgs != null) {
            throw new IllegalArgumentException("Only 1 row is ever returned from this table, so " +
                    "selection arguments are pointless.");
        }

        if (sortOrder != null) {
            throw new IllegalArgumentException("Only 1 row is ever returned from this table, so a" +
                    " sortOrder is pointless.");
        }

        return openHelper.getReadableDatabase().query(
                BusStopContract.DatabaseInformation.TABLE_NAME, projection, null, null, null,
                null, null, "1");
    }

    /**
     * Do a query against the database to get services information.
     *
     * @param projection The columns to return.
     * @param selection The {@code WHERE} clause.
     * @param selectionArgs The arguments to replace in to the {@code WHERE} clause.
     * @param sortOrder The {@code ORDER BY} clause.
     * @return A {@link Cursor} which is the result of the query.
     */
    @NonNull
    private Cursor doServicesQuery(@Nullable final String[] projection,
            @Nullable final String selection, @Nullable final String[] selectionArgs,
            @Nullable final String sortOrder) {
        return openHelper.getReadableDatabase().query(BusStopContract.Services.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    /**
     * Do a query against the database to get bus stop information.
     *
     * @param projection The columns to return.
     * @param selection The {@code WHERE} clause.
     * @param selectionArgs The arguments to replace in to the {@code WHERE} clause.
     * @param sortOrder The {@code ORDER BY} clause.
     * @return A {@link Cursor} which is the result of the query.
     */
    @NonNull
    private Cursor doBusStopsQuery(@Nullable final String[] projection,
            @Nullable final String selection, @Nullable final String[] selectionArgs,
            @Nullable final String sortOrder) {
        return openHelper.getReadableDatabase().query(BusStopContract.BusStops.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    /**
     * Do a query against the database to get service stops information.
     *
     * @param projection The columns to return.
     * @param selection The {@code WHERE} clause.
     * @param selectionArgs The arguments to replace in to the {@code WHERE} clause.
     * @param sortOrder The {@code ORDER BY} clause.
     * @return A {@link Cursor} which is the result of the query.
     */
    @NonNull
    private Cursor doServiceStopsQuery(@Nullable final String[] projection,
            @Nullable final String selection, @Nullable final String[] selectionArgs,
            @Nullable String sortOrder) {
        if (sortOrder == null) {
            sortOrder = "CASE WHEN " + BusStopContract.ServiceStops.SERVICE_NAME +
                    " GLOB '[^0-9.]*' THEN " + BusStopContract.ServiceStops.SERVICE_NAME +
                    " ELSE cast(" + BusStopContract.ServiceStops.SERVICE_NAME + " AS int) END";
        }

        return openHelper.getReadableDatabase().query(BusStopContract.ServiceStops.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    /**
     * Do a query against the database to get service points information.
     *
     * @param projection The columns to return.
     * @param selection The {@code WHERE} clause.
     * @param selectionArgs The arguments to replace in to the {@code WHERE} clause.
     * @param sortOrder The {@code ORDER BY} clause.
     * @return A {@link Cursor} which is the result of the query.
     */
    @NonNull
    private Cursor doServicePointsQuery(@Nullable final String[] projection,
            @Nullable final String selection, @Nullable final String[] selectionArgs,
            @Nullable String sortOrder) {
        if (sortOrder == null) {
            sortOrder = BusStopContract.ServicePoints.CHAINAGE + " ASC, " +
                    BusStopContract.ServicePoints.ORDER_VALUE + " ASC";
        }

        return openHelper.getReadableDatabase().query(BusStopContract.ServicePoints.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    /**
     * Replace the current bus stop database with a database living at the path specified by
     * {@code dbPath}.
     *
     * @param dbPath The path to the new database file that should be used. This file will be moved
     * in to the correct location.
     * @throws IllegalArgumentException When {@code dbPath} is {@code null} or empty.
     */
    private synchronized void replaceDatabase(@Nullable final String dbPath) {
        if (TextUtils.isEmpty(dbPath)) {
            throw new IllegalArgumentException("The dbPath must not be null or empty.");
        }

        openHelper.replaceDatabase(new File(dbPath));

        final ContentResolver resolver = getContext().getContentResolver();
        resolver.notifyChange(BusStopContract.DatabaseInformation.CONTENT_URI, null, false);
        resolver.notifyChange(BusStopContract.Services.CONTENT_URI, null, false);
        resolver.notifyChange(BusStopContract.BusStops.CONTENT_URI, null, false);
        resolver.notifyChange(BusStopContract.ServiceStops.CONTENT_URI, null, false);
        resolver.notifyChange(BusStopContract.ServicePoints.CONTENT_URI, null, false);
    }
}
