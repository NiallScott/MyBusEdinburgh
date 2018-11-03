/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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
 * 1. This notice may not be removed or altered from any file it appears in.
 *
 * 2. Any modifications made to this software, except those defined in
 *    clause 3 of this agreement, must be released under this license, and
 *    the source code of any modifications must be made available on a
 *    publically accessible (and locateable) website, or sent to the
 *    original author of this software.
 *
 * 3. Software modifications that do not alter the functionality of the
 *    software but are simply adaptations to a specific environment are
 *    exempt from clause 2.
 */

package uk.org.rivernile.android.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

/**
 * This {@link Context} renames databases for testing so they do not interfere with live databases
 * on the device. It does the same as the old platform {@code RenamingDelegatingContext}, but works
 * around the problem of that class deleting database files unexpectedly.
 *
 * @author Niall Scott
 */
public class DatabaseRenamingContext extends ContextWrapper {

    private final String filePrefix;

    /**
     * Create a new {@code DatabaseRenamingContext}.
     *
     * @param context The {@link Context} to use for real implementation.
     * @param filePrefix The {@link String} to prefix the database name with.
     */
    public DatabaseRenamingContext(@NonNull final Context context,
            @NonNull final String filePrefix) {
        super(context);

        this.filePrefix = filePrefix;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(final String name, final int mode,
            final SQLiteDatabase.CursorFactory factory) {
        return super.openOrCreateDatabase(renameDatabase(name), mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(final String name, final int mode,
            final SQLiteDatabase.CursorFactory factory, final DatabaseErrorHandler errorHandler) {
        return super.openOrCreateDatabase(renameDatabase(name), mode, factory, errorHandler);
    }

    @Override
    public boolean deleteDatabase(final String name) {
        return super.deleteDatabase(renameDatabase(name));
    }

    @Override
    public File getDatabasePath(final String name) {
        return super.getDatabasePath(renameDatabase(name));
    }

    @Override
    public String[] databaseList() {
        final String[] databases = super.databaseList();
        final ArrayList<String> isolatedDatabases = new ArrayList<>(databases.length);

        for (String db : databases) {
            if (db.startsWith(filePrefix)) {
                isolatedDatabases.add(db);
            }
        }

        return (String[]) isolatedDatabases.toArray();
    }

    /**
     * Rename a database name {@link String} to one prefixed with the specified prefix.
     *
     * @param nameIn The original name of the database.
     * @return The new name of the database.
     */
    @NonNull
    private String renameDatabase(@NonNull final String nameIn) {
        return filePrefix + nameIn;
    }
}
