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
import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;

/**
 * This {@link CursorLoader} is used to determine if the given stop code is saved in the user's
 * favourites.
 *
 * @author Niall Scott
 */
public class HasFavouriteStopLoader extends CursorLoader {

    /**
     * Used to create a {@code CursorLoader} which determines if the given stop code is saved in
     * the user's favourites.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The bus stop code to determine if the user has a favourite saved for.
     */
    public HasFavouriteStopLoader(@NonNull final Context context, @NonNull final String stopCode) {
        super(context, SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites._ID },
                SettingsContract.Favourites.STOP_CODE + " = ?", new String[] { stopCode }, null);
    }
}
