/*
 * Copyright (C) 2016 - 2021 Niall 'Rivernile' Scott
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
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.annotation.WorkerThread;

/**
 * This class contains static methods to aid in dealing with the settings database and provides
 * consistency.
 *
 * @author Niall Scott
 */
public final class SettingsDatabase {

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
}
