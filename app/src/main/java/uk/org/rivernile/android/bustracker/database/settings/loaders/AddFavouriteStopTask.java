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

package uk.org.rivernile.android.bustracker.database.settings.loaders;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import uk.org.rivernile.android.bustracker.database.settings.SettingsDatabase;

/**
 * This task adds a favourite bus stop to the database.
 *
 * @author Niall Scott
 */
public class AddFavouriteStopTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final String stopCode;
    private final String stopName;

    /**
     * Create a new instance of this {@code AddFavouriteStopTask}.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the stop to add.
     * @param stopName The name to set for the favourite stop.
     */
    private AddFavouriteStopTask(@NonNull final Context context,
            @NonNull final String stopCode, @NonNull final String stopName) {
        this.context = context;
        this.stopCode = stopCode;
        this.stopName = stopName;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        SettingsDatabase.addFavouriteStop(context, stopCode, stopName);
        return null;
    }

    /**
     * Start the task of adding a favourite bus stop to the database.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the stop to add.
     * @param stopName The name to set for the favourite stop.
     */
    public static void start(@NonNull final Context context, @NonNull final String stopCode,
            @NonNull final String stopName) {
        new AddFavouriteStopTask(context, stopCode, stopName).execute();
    }
}
