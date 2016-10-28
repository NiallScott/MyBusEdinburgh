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

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.endpoints.DatabaseEndpoint;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseEndpointException;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.android.fetchutils.fetchers.HttpFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.FileWriterFetcherStreamReader;
import uk.org.rivernile.android.utils.FileUtils;

/**
 * This is the sync adapter used to update the bus stop database.
 *
 * @author Niall Scott
 */
class BusStopDatabaseSyncAdapter extends AbstractThreadedSyncAdapter {

    private final SharedPreferences sp;
    private final DatabaseEndpoint databaseEndpoint;

    /**
     * Create a new {@code BusStopDatabaseSyncAdapter}.
     *
     * @param context A {@link Context} instance.
     */
    BusStopDatabaseSyncAdapter(@NonNull final Context context) {
        super(context, true, false);

        sp = context.getSharedPreferences(PreferenceConstants.PREF_FILE, 0);
        databaseEndpoint = ((BusApplication) context).getDatabaseEndpoint();
    }

    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority,
            final ContentProviderClient provider, final SyncResult syncResult) {
        final DatabaseVersion version;

        try {
            version = databaseEndpoint.getDatabaseVersion(BusStopContract.SCHEMA_NAME);
        } catch (DatabaseEndpointException e) {
            syncResult.stats.numIoExceptions++;
            return;
        }

        // Check for interruptions after long running tasks. As per the documentation for
        // AbstractThreadedSyncAdapter, our process might be killed if we don't respect
        // interruption.
        if (Thread.interrupted()) {
            return;
        }

        if (!BusStopContract.SCHEMA_NAME.equals(version.getSchemaName())) {
            syncResult.stats.numParseExceptions++;
            return;
        }

        final String dbTopoId = BusStopDatabase.getTopologyId(getContext());

        if (Thread.interrupted()) {
            return;
        }

        if (!version.getTopologyId().equals(dbTopoId)) {
            updateDatabase(version, syncResult);
        } else {
            writeUpdatedCheckTime();
        }
    }

    /**
     * Update the database if there's an update available.
     *
     * @param version The database version information.
     */
    private void updateDatabase(@NonNull final DatabaseVersion version,
            @NonNull final SyncResult syncResult) {
        // The new database is put in to a temporary file until it's ready to be swapped in.
        final Context context = getContext();
        BusStopOpenHelper.ensureDatabasePath(context);
        final File tempFile = context.getDatabasePath(BusStopContract.DB_NAME + "_temp");

        final HttpFetcher fetcher = new HttpFetcher.Builder(context)
                .setUrl(version.getUrl())
                .setAllowHostRedirects(false)
                .build();
        final FileWriterFetcherStreamReader reader = new FileWriterFetcherStreamReader(tempFile,
                false);

        try {
            // This single call will fetch the database from the server and pipe it out on to a
            // file on disk.
            fetcher.executeFetcher(reader);

            if (Thread.interrupted()) {
                tempFile.delete();
                return;
            }

            // This checks the consistency of the downloaded file. If the file is corrupt, delete
            // it and stop the process.
            if (!FileUtils.md5Checksum(tempFile).equalsIgnoreCase(version.getChecksum())) {
                // Assume that if the MD5 checksum fails, the file is corrupt due to an IO issue.
                syncResult.stats.numIoExceptions++;
                tempFile.delete();
                return;
            }
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            tempFile.delete();
            return;
        }

        getContext().getContentResolver().call(BusStopContract.CONTENT_URI,
                BusStopContract.METHOD_REPLACE_DATABASE, tempFile.getAbsolutePath(), null);
        syncResult.stats.numEntries++;
        syncResult.stats.numUpdates++;

        // Finally, if we reached this point, it means that everything was probably okay, so
        // write the new check time.
        writeUpdatedCheckTime();
    }

    /**
     * Write a new check time to the preferences. This should only be done in success cases.
     */
    private void writeUpdatedCheckTime() {
        final SharedPreferences.Editor edit = sp.edit();
        edit.putLong(PreferenceConstants.PREF_DATABASE_UPDATE_LAST_CHECK,
                System.currentTimeMillis());
        edit.apply();
    }
}
