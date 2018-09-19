/*
 * Copyright (C) 2015 - 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import uk.org.rivernile.android.bustracker.database.settings.SettingsDatabase;
import uk.org.rivernile.android.bustracker.database.settings.loaders.BackupFavouritesLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.RestoreFavouritesLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link android.app.Activity} is used to host a {@link SettingsFragment} to allow the user to
 * change application preferences.
 *
 * @author Niall Scott
 */
public class SettingsActivity extends AppCompatActivity implements SettingsFragment.Callbacks,
        LoaderManager.LoaderCallbacks<Integer> {

    private static final int PERMISSION_REQUEST_BACKUP = 1;
    private static final int PERMISSION_REQUEST_RESTORE = 2;

    private static final int LOADER_BACKUP_FAVOURITES = 1;
    private static final int LOADER_RESTORE_FAVOURITES = 2;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);
        final LoaderManager loaderManager = getSupportLoaderManager();

        if (loaderManager.getLoader(LOADER_BACKUP_FAVOURITES) != null) {
            // Reconnect to the Loader to get its result.
            loaderManager.initLoader(LOADER_BACKUP_FAVOURITES, null, this);
        }

        if (loaderManager.getLoader(LOADER_RESTORE_FAVOURITES) != null) {
            // Reconnect to the Loader to get its result.
            loaderManager.initLoader(LOADER_RESTORE_FAVOURITES, null, this);
        }
    }

    @Override
    public Loader<Integer> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BACKUP_FAVOURITES:
                return new BackupFavouritesLoader(this);
            case LOADER_RESTORE_FAVOURITES:
                return new RestoreFavouritesLoader(this);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Integer> loader, final Integer data) {
        switch (loader.getId()) {
            case LOADER_BACKUP_FAVOURITES:
                handleBackupResult(data);
                break;
            case LOADER_RESTORE_FAVOURITES:
                handleRestoreResult(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Integer> loader) {
        // Nothing to do here.
    }

    @Override
    public void onBackupFavourites() {
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                PERMISSION_REQUEST_BACKUP)) {
            performBackup();
        }
    }

    @Override
    public void onRestoreFavourites() {
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                PERMISSION_REQUEST_RESTORE)) {
            performRestore();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_BACKUP:
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    performBackup();
                } else {
                    Toast.makeText(this, R.string.preference_backup_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case PERMISSION_REQUEST_RESTORE:
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    performRestore();
                } else {
                    Toast.makeText(this, R.string.preference_restore_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /**
     * Check if a permission has been granted. If it has not, it will attempt to request the
     * permission.
     *
     * @param permission The permission to request.
     * @param request The request code of the permission request.
     * @return {@code true} if the permission has already been granted, {@code false} if the
     * permission hasn't been granted yet. Note: {@code false} means that this method will go on to
     * asynchronously request the permission - it may come back soon.
     */
    private boolean checkPermission(@NonNull final String permission, final int request) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { permission }, request);

            return false;
        } else {
            return true;
        }
    }

    /**
     * Perform the backup of the user's favourite stops.
     */
    private void performBackup() {
        getSupportLoaderManager().restartLoader(LOADER_BACKUP_FAVOURITES, null, this);
    }

    /**
     * Perform the restore of the user's favourite stops.
     */
    private void performRestore() {
        getSupportLoaderManager().restartLoader(LOADER_RESTORE_FAVOURITES, null, this);
    }

    /**
     * Handle the result of an operation to backup the user's saved favourite stops.
     *
     * @param result A number representing the result of the backup operation.
     */
    private void handleBackupResult(@SettingsDatabase.BackupRestoreResult final int result) {
        getSupportLoaderManager().destroyLoader(LOADER_BACKUP_FAVOURITES);

        final int text;

        switch (result) {
            case SettingsDatabase.BACKUP_RESTORE_SUCCESS:
                text = R.string.preference_backup_success;
                break;
            case SettingsDatabase.ERROR_BACKUP_RESTORE_EXTERNAL_STORAGE:
                text = R.string.preferences_backup_restore_error_media;
                break;
            case SettingsDatabase.ERROR_BACKUP_UNABLE_TO_WRITE:
                text = R.string.preferences_backup_error_write;
                break;
            default:
                text = 0;
        }

        if (text != 0) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handle the result of an operation to restore the user's backup of saved favourite stops.
     *
     * @param result A number representing the result of the restore operation.
     */
    private void handleRestoreResult(@SettingsDatabase.BackupRestoreResult final int result) {
        getSupportLoaderManager().destroyLoader(LOADER_RESTORE_FAVOURITES);

        final int text;

        switch (result) {
            case SettingsDatabase.BACKUP_RESTORE_SUCCESS:
                text = R.string.preference_restore_success;
                break;
            case SettingsDatabase.ERROR_BACKUP_RESTORE_EXTERNAL_STORAGE:
                text = R.string.preferences_backup_restore_error_media;
                break;
            case SettingsDatabase.ERROR_RESTORE_FILE_DOES_NOT_EXIST:
                text = R.string.preferences_restore_error_no_file;
                break;
            case SettingsDatabase.ERROR_RESTORE_UNABLE_TO_READ:
                text = R.string.preferences_restore_error_unable_read;
                break;
            case SettingsDatabase.ERROR_RESTORE_DATA_MALFORMED:
                text = R.string.preferences_restore_error_data_malformed;
                break;
            default:
                text = 0;
        }

        if (text != 0) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
    }
}
