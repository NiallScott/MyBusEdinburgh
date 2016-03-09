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

package uk.org.rivernile.android.bustracker.database.settings.loaders;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import uk.org.rivernile.android.bustracker.database.settings.SettingsDatabase;

/**
 * This task adds a proximity alert to the database.
 *
 * @author Niall Scott
 */
public class AddProximityAlertTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final String stopCode;
    private final int distance;

    /**
     * Create a new instance of this {@code AddProximityAlertTask}.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the bus stop that this alert is set for.
     * @param distance The distance from the bus stop to trigger the proximity alert.
     */
    private AddProximityAlertTask(@NonNull final Context context,
            @NonNull final String stopCode, final int distance) {
        this.context = context;
        this.stopCode = stopCode;
        this.distance = distance;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        SettingsDatabase.addProximityAlert(context, stopCode, distance);
        return null;
    }

    /**
     * Start the task of adding a proximity alert to the database.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The code of the bus stop that this alert is set for.
     * @param distance The distance from the bus stop to trigger the proximity alert.
     */
    public static void start(@NonNull final Context context, @NonNull final String stopCode,
            final int distance) {
        new AddProximityAlertTask(context, stopCode, distance).execute();
    }
}