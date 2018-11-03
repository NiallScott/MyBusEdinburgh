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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

/**
 * This class is used to open the settings database and if necessary, create the schema.
 *
 * @author Niall Scott
 */
class SettingsOpenHelper extends SQLiteOpenHelper {

    private static final int FAVOURITES_UPGRADE_VERSION = 3;

    /**
     * Create the {@link SQLiteOpenHelper} to open the settings database.
     *
     * @param context A {@link Context} object for the application.
     */
    SettingsOpenHelper(@NonNull final Context context) {
        super(context, SettingsContract.DB_NAME, null, SettingsContract.DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        createFavouritesTable(db);
        createAlertsTable(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        createAlertsTable(db);

        if (oldVersion < FAVOURITES_UPGRADE_VERSION) {
            // Version 3 of the DB changes the schema of the favourites table. Do the upgrade.
            upgradeFavouritesTableForV3(db);
        }
    }

    /**
     * Create the table schema for the favourites table.
     *
     * @param db The {@link SQLiteDatabase}.
     */
    private static void createFavouritesTable(@NonNull final SQLiteDatabase db) {
        db.execSQL(getFavouritesCreateTableStatement(SettingsContract.Favourites.TABLE_NAME));
    }

    /**
     * Create the table schema for the alerts table.
     *
     * @param db The {@link SQLiteDatabase}.
     */
    private static void createAlertsTable(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SettingsContract.Alerts.TABLE_NAME + " (" +
                SettingsContract.Alerts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SettingsContract.Alerts.TYPE + " NUMERIC NOT NULL," +
                SettingsContract.Alerts.TIME_ADDED + " INTEGER NOT NULL," +
                SettingsContract.Alerts.STOP_CODE + " TEXT NOT NULL," +
                SettingsContract.Alerts.DISTANCE_FROM + " INTEGER," +
                SettingsContract.Alerts.SERVICE_NAMES + " TEXT," +
                SettingsContract.Alerts.TIME_TRIGGER + " INTEGER);");
        createAlertsTriggers(db);
    }

    /**
     * Create triggers required on the alerts table.
     *
     * @param db The {@link SQLiteDatabase}.
     */
    private static void createAlertsTriggers(@NonNull final SQLiteDatabase db) {
        db.execSQL(getAlertsTriggerStatement("insert_alert", "BEFORE INSERT"));
        db.execSQL(getAlertsTriggerStatement("delete_alert", "AFTER DELETE"));
        db.execSQL(getAlertsTriggerStatement("update_alert", "AFTER UPDATE"));
    }

    /**
     * Upgrade the favourites table to add an integer primary key field (to replace the stopCode as
     * the primary key). This will make management of this table easier going forward.
     *
     * @param db The {@link SQLiteDatabase}.
     */
    private static void upgradeFavouritesTableForV3(@NonNull final SQLiteDatabase db) {
        db.execSQL(getFavouritesCreateTableStatement("temp_favourites"));
        db.execSQL("INSERT INTO temp_favourites (" +
                SettingsContract.Favourites.STOP_CODE + ',' +
                SettingsContract.Favourites.STOP_NAME +
                ") SELECT " +
                SettingsContract.Favourites._ID + ',' +
                SettingsContract.Favourites.STOP_NAME +
                " FROM " + SettingsContract.Favourites.TABLE_NAME +
                " WHERE " + SettingsContract.Favourites._ID + " NOT NULL;");
        db.execSQL("DROP TABLE " + SettingsContract.Favourites.TABLE_NAME + ';');
        db.execSQL("ALTER TABLE temp_favourites RENAME TO " +
                SettingsContract.Favourites.TABLE_NAME + ';');
    }

    /**
     * Convenience method for creating the schema of the favourites table.
     *
     * @param tableName The name of the table to create.
     * @return The schema to create a favourites table.
     */
    @NonNull
    private static String getFavouritesCreateTableStatement(@NonNull final String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                SettingsContract.Favourites._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SettingsContract.Favourites.STOP_CODE + " TEXT NOT NULL UNIQUE," +
                SettingsContract.Favourites.STOP_NAME + " TEXT NOT NULL);";
    }

    /**
     * Convenience method for getting a trigger statement for creating alert triggers.
     *
     * @param triggerName The name of the trigger to create.
     * @param condition When the trigger should be fired.
     * @return The schema to create an alerts trigger.
     */
    @NonNull
    private static String getAlertsTriggerStatement(@NonNull final String triggerName,
            @NonNull final String condition) {
        final String toInsert = triggerName + ' ' + condition;
        return "CREATE TRIGGER IF NOT EXISTS " + toInsert + " ON " +
                SettingsContract.Alerts.TABLE_NAME + " FOR EACH ROW BEGIN " +
                "DELETE FROM " + SettingsContract.Alerts.TABLE_NAME + " WHERE " +
                SettingsContract.Alerts.TIME_ADDED +
                " < ((SELECT strftime('%s','now') * 1000) - 3600000); END;";
    }
}
