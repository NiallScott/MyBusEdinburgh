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

package uk.org.rivernile.android.bustracker.database.settings;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import uk.org.rivernile.android.fetchutils.fetchers.FileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;

/**
 * This class contains static methods to aid in dealing with the settings database and provides
 * consistency.
 *
 * @author Niall Scott
 */
public final class SettingsDatabase {

    /**
     * Defines a backup or restore result, to ensure the correct error codes are checked for.
     */
    @IntDef({BACKUP_RESTORE_SUCCESS, ERROR_BACKUP_RESTORE_EXTERNAL_STORAGE,
            ERROR_BACKUP_UNABLE_TO_WRITE, ERROR_RESTORE_FILE_DOES_NOT_EXIST,
            ERROR_RESTORE_UNABLE_TO_READ, ERROR_RESTORE_DATA_MALFORMED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BackupRestoreResult { }

    /** The backup or restore was successful. */
    public static final int BACKUP_RESTORE_SUCCESS = 0;
    /** The external storage could not be accessed to read or write the backup to or from. */
    public static final int ERROR_BACKUP_RESTORE_EXTERNAL_STORAGE = 1;
    /** The backup was unable to write to the backup file. */
    public static final int ERROR_BACKUP_UNABLE_TO_WRITE = 2;
    /** The file to restore the backup from does not exist. */
    public static final int ERROR_RESTORE_FILE_DOES_NOT_EXIST = 3;
    /** Unable to read from the backup file. */
    public static final int ERROR_RESTORE_UNABLE_TO_READ = 4;
    /** The data in the backup file is malformed. */
    public static final int ERROR_RESTORE_DATA_MALFORMED = 5;

    private static final String BACKUP_DB_VERSION = "dbVersion";
    private static final String BACKUP_SCHEMA_VERSION = "jsonSchemaVersion";
    private static final String BACKUP_CREATE_TIME = "createTime";
    private static final String BACKUP_FAVOURITE_STOPS = "favouriteStops";
    private static final String BACKUP_STOPCODE = "stopCode";
    private static final String BACKUP_STOPNAME = "stopName";

    private static final String BACKUP_DIRECTORY = "/mybusedinburgh/";
    private static final String BACKUP_FILE_NAME = "settings.backup";

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
     * Backup the user's favourite stops in to the supplied {@link File}.
     *
     * @param context A {@link Context} instance.
     * @param file The {@link File} to backup the user's favourite stops to.
     * @return The result of the backup. See {@link BackupRestoreResult}.
     */
    @WorkerThread
    @BackupRestoreResult
    public static int backupFavourites(@NonNull final Context context, @NonNull final File file) {
        final byte[] output = serialiseFavourites(context).toString().getBytes();
        BufferedOutputStream out = null;
        int result;

        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(output);
            out.flush();
            result = BACKUP_RESTORE_SUCCESS;
        } catch (IOException e) {
            result = ERROR_BACKUP_UNABLE_TO_WRITE;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Already in an error state and could not close even told to. Nothing else can
                    // be done.
                }
            }
        }

        return result;
    }

    /**
     * Back up the user's favourite stops to external storage.
     *
     * @param context A {@link Context} instance.
     * @return The result of the backup. See {@link BackupRestoreResult}.
     */
    @WorkerThread
    @BackupRestoreResult
    public static int userBackupFavourites(@NonNull final Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return ERROR_BACKUP_RESTORE_EXTERNAL_STORAGE;
        }

        final File out = new File(Environment.getExternalStorageDirectory(), BACKUP_DIRECTORY);

        if (!out.exists() && !out.mkdirs()) {
            return ERROR_BACKUP_UNABLE_TO_WRITE;
        }

        return backupFavourites(context, new File(out, BACKUP_FILE_NAME));
    }

    /**
     * Restore the user's favourite stops from the supplied {@link File}.
     *
     * @param context A {@link Context} instance.
     * @param file The {@link File} containing the user's backed up favourite stops.
     * @return The result of the restore. See {@link BackupRestoreResult}.
     */
    @WorkerThread
    @BackupRestoreResult
    public static int restoreFavourites(@NonNull final Context context, @NonNull final File file) {
        if (!file.exists()) {
            return ERROR_RESTORE_FILE_DOES_NOT_EXIST;
        }

        final FileFetcher fetcher = new FileFetcher(file);
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        int result;

        try {
            fetcher.executeFetcher(reader);
            deserialiseFavourites(context, reader.getJSONObject());
            result = BACKUP_RESTORE_SUCCESS;
        } catch (IOException e) {
            result = ERROR_RESTORE_UNABLE_TO_READ;
        } catch (JSONException e) {
            result = ERROR_RESTORE_DATA_MALFORMED;
        }

        return result;
    }

    /**
     * Restore the user's favourite stops from external storage.
     *
     * @param context A {@link Context} instance.
     * @return The result of the restore. See {@link BackupRestoreResult}.
     */
    @WorkerThread
    @BackupRestoreResult
    public static int userRestoreFavourites(@NonNull final Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return ERROR_BACKUP_RESTORE_EXTERNAL_STORAGE;
        }

        final File in = new File(Environment.getExternalStorageDirectory(),
                BACKUP_DIRECTORY + BACKUP_FILE_NAME);
        if (!in.exists() || !in.canRead()) {
            return ERROR_RESTORE_UNABLE_TO_READ;
        }

        return restoreFavourites(context, in);
    }

    /**
     * Serialise the favourite stops table in to a JSON document.
     *
     * @param context A {@link Context} instance.
     * @return A root {@link JSONObject} for the document, containing the serialised favourite
     * stops.
     */
    @WorkerThread
    @NonNull
    private static JSONObject serialiseFavourites(@NonNull final Context context) {
        final JSONObject joRoot = new JSONObject();
        final JSONArray jaFavouriteStops = new JSONArray();

        try {
            joRoot.put(BACKUP_DB_VERSION, SettingsContract.DB_VERSION);
            joRoot.put(BACKUP_SCHEMA_VERSION, 1);
            joRoot.put(BACKUP_CREATE_TIME, System.currentTimeMillis());
            joRoot.put(BACKUP_FAVOURITE_STOPS, jaFavouriteStops);

            final Cursor c = context.getContentResolver().query(
                    SettingsContract.Favourites.CONTENT_URI, null, null, null, null);

            if (c != null) {
                final int stopCodeColumn = c.getColumnIndex(SettingsContract.Favourites.STOP_CODE);
                final int stopNameColumn = c.getColumnIndex(SettingsContract.Favourites.STOP_NAME);

                while (c.moveToNext()) {
                    final JSONObject joStop = new JSONObject();
                    joStop.put(BACKUP_STOPCODE, c.getString(stopCodeColumn));
                    joStop.put(BACKUP_STOPNAME, c.getString(stopNameColumn));
                    jaFavouriteStops.put(joStop);
                }

                c.close();
            }
        } catch (JSONException ignored) {
            // This will never happen, but unfortunately JSONObject forces us to catch the
            // Exception.
        }

        return joRoot;
    }

    /**
     * Deserialise the user's backed up favourite stops from the JSON document and populate them in
     * the database. Prior to adding the new items in the database, the favourites table will be
     * cleared out.
     *
     * @param context A {@link Context} instance.
     * @param joRoot The root {@link JSONObject} of the document.
     * @throws JSONException When the {@link JSONObject} has an unexpected structure.
     */
    @WorkerThread
    private static void deserialiseFavourites(@NonNull final Context context,
            @NonNull final JSONObject joRoot) throws JSONException {
        final JSONArray jaFavouriteStops = joRoot.getJSONArray(BACKUP_FAVOURITE_STOPS);
        context.getContentResolver().delete(SettingsContract.Favourites.CONTENT_URI, null, null);
        final int len = jaFavouriteStops.length();

        for (int i = 0; i < len; i++) {
            try {
                final JSONObject joStop = jaFavouriteStops.getJSONObject(i);
                addFavouriteStop(context, joStop.getString(BACKUP_STOPCODE),
                        joStop.getString(BACKUP_STOPNAME));
            } catch (JSONException ignored) {
                // Ignored. Progress on to next loop iteration.
            }
        }
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
