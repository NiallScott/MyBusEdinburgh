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

package uk.org.rivernile.android.bustracker.database.busstop.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;

/**
 * This {@link CursorLoader} loads information about the bus stop database.
 *
 * @author Niall Scott
 */
public class DatabaseInformationLoader extends CursorLoader {

    /**
     * Create a new {@code DatabaseInformationLoader}.
     *
     * @param context A {@link Context} instance.
     */
    public DatabaseInformationLoader(@NonNull final Context context) {
        super(context, BusStopContract.DatabaseInformation.CONTENT_URI,
                new String[] {
                        BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID,
                        BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP
                }, null, null, null);
    }
}
