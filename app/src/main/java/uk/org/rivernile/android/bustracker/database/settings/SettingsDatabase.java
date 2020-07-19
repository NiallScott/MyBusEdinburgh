/*
 * Copyright (C) 2016 - 2020 Niall 'Rivernile' Scott
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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

/**
 * This class contains static methods to aid in dealing with the settings database and provides
 * consistency.
 *
 * @author Niall Scott
 */
public final class SettingsDatabase {

    /**
     * Add a new favourite stop to the database.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the stop to add.
     * @param stopName The name to save for the favourite stop.
     * @return A {@link Uri} to the newly added row, as defined by
     * {@link android.content.ContentResolver#insert(Uri, ContentValues)}.
     */
    @WorkerThread
    @Nullable
    public static Uri addFavouriteStop(@NonNull final Context context,
            @NonNull @Size(min = 1) final String stopCode,
            @NonNull @Size(min = 1) final String stopName) {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, stopCode);
        cv.put(SettingsContract.Favourites.STOP_NAME, stopName);

        return context.getContentResolver().insert(SettingsContract.Favourites.CONTENT_URI, cv);
    }

    /**
     * Update a favourite stop in the database. This modifies the name of the favourite stop.
     *
     * @param context A {@link Context} instance.
     * @param rowId The ID of the row to update.
     * @param stopName The new user supplied name for the stop.
     * @return The number of rows updated, as defined by
     * {@link android.content.ContentResolver#update(Uri, ContentValues, String, String[])}.
     */
    @WorkerThread
    public static int updateFavouriteStop(@NonNull final Context context,
            final long rowId, @NonNull @Size(min = 1) final String stopName) {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, stopName);

        return context.getContentResolver().update(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, rowId), cv,
                null, null);
    }

    /**
     * Delete a favourite stop from the database, specifying what stop to delete by its stop code.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the bus stop to delete.
     * @return The number of rows deleted, as defined by
     * {@link android.content.ContentResolver#delete(Uri, String, String[])}.
     */
    @WorkerThread
    public static int deleteFavouriteStop(@NonNull final Context context,
            @NonNull @Size(min = 1) final String stopCode) {
        return context.getContentResolver().delete(SettingsContract.Favourites.CONTENT_URI,
                SettingsContract.Favourites.STOP_CODE + " = ?", new String[] { stopCode });
    }

    /**
     * Add a new proximity alert to the database.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the bus stop that this alert is set for.
     * @param distance The distance from the bus stop to trigger the proximity alert.
     * @return A {@link Uri} to the newly added row, as defined by
     * {@link android.content.ContentResolver#insert(Uri, ContentValues)}.
     */
    @WorkerThread
    @Nullable
    public static Uri addProximityAlert(@NonNull final Context context,
            @NonNull @Size(min = 1) final String stopCode, @IntRange(from = 1) final int distance) {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv.put(SettingsContract.Alerts.TIME_ADDED, System.currentTimeMillis());
        cv.put(SettingsContract.Alerts.STOP_CODE, stopCode);
        cv.put(SettingsContract.Alerts.DISTANCE_FROM, distance);

        return context.getContentResolver().insert(SettingsContract.Alerts.CONTENT_URI, cv);
    }

    /**
     * Add a new time alert to the database.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the bus stop that this alert is set for.
     * @param services A {@link String} array of services which can trigger this alert.
     * @param timeTrigger The time at which to trigger the time alert. The time is relative to the
     * number of minutes until the service is due to arrive at the stop.
     * @return A {@link Uri} to the newly added row, as defined by
     * {@link android.content.ContentResolver#insert(Uri, ContentValues)}.
     */
    @WorkerThread
    @Nullable
    public static Uri addTimeAlert(@NonNull final Context context,
            @NonNull @Size(min = 1) final String stopCode,
            @NonNull @Size(min = 1) final String[] services, final int timeTrigger) {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_TIME);
        cv.put(SettingsContract.Alerts.TIME_ADDED, System.currentTimeMillis());
        cv.put(SettingsContract.Alerts.STOP_CODE, stopCode);
        cv.put(SettingsContract.Alerts.SERVICE_NAMES, packServices(services));
        cv.put(SettingsContract.Alerts.TIME_TRIGGER, timeTrigger);

        return context.getContentResolver().insert(SettingsContract.Alerts.CONTENT_URI, cv);
    }

    /**
     * Delete all proximity alerts from the database.
     *
     * @param context A {@link Context} instance.
     * @return The number of rows deleted, as defined by
     * {@link android.content.ContentResolver#delete(Uri, String, String[])}.
     */
    @WorkerThread
    public static int deleteAllProximityAlerts(@NonNull final Context context) {
        return deleteAllAlertsOfType(context, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
    }

    /**
     * Delete all time alerts from the database.
     *
     * @param context A {@link Context} instance.
     * @return The number of rows deleted, as defined by
     * {@link android.content.ContentResolver#delete(Uri, String, String[])}.
     */
    @WorkerThread
    public static int deleteAllTimeAlerts(@NonNull final Context context) {
        return deleteAllAlertsOfType(context, SettingsContract.Alerts.ALERTS_TYPE_TIME);
    }

    /**
     * Delete all alerts of a given type from the database.
     *
     * @param context A {@link Context} instance.
     * @param alertType Either {@link SettingsContract.Alerts#ALERTS_TYPE_PROXIMITY} or
     * {@link SettingsContract.Alerts#ALERTS_TYPE_TIME}.
     * @return The number of rows deleted, as defined by
     * {@link android.content.ContentResolver#delete(Uri, String, String[])}.
     */
    @WorkerThread
    private static int deleteAllAlertsOfType(@NonNull final Context context, final int alertType) {
        return context.getContentResolver().delete(SettingsContract.Alerts.CONTENT_URI,
                SettingsContract.Alerts.TYPE + " = ?", new String[] { String.valueOf(alertType) });
    }

    /**
     * Pack a {@link String} array of services in to a single {@link String} that can be written in
     * to a column in the database.
     *
     * @param services The {@link String} array of services to pack.
     * @return The packed {@link String} of services.
     */
    @NonNull
    @VisibleForTesting
    static String packServices(@NonNull final String[] services) {
        final int len = services.length;

        if (len > 0) {
            final StringBuilder sb = new StringBuilder();

            for (int i = 0; i < len; i++) {
                sb.append(services[i]);

                if (i != (len - 1)) {
                    sb.append(',');
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }
}
