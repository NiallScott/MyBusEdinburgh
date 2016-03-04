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


package uk.org.rivernile.android.bustracker.ui.favourites;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import uk.org.rivernile.android.fetchutils.loaders.support.SimpleAsyncTaskLoader;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;

/**
 * For each given bus stop code, this will load a {@link String} listing of all services that stop
 * at that stop.
 *
 * @author Niall Scott
 */
class BusStopServicesLoader extends SimpleAsyncTaskLoader<Map<String, String>> {

    private final String[] stopCodes;

    /**
     * Create a new {@code BusStopServicesLoader}.
     *
     * @param context A {@link Context} instance.
     * @param stopCodes The stop codes to request services for.
     */
    BusStopServicesLoader(@NonNull final Context context, @NonNull final String[] stopCodes) {
        super(context);

        this.stopCodes = stopCodes;
    }

    @Override
    public Map<String, String> loadInBackground() {
        final BusStopDatabase bsd =
                BusStopDatabase.getInstance(getContext().getApplicationContext());
        final HashMap<String, String> busStopServices = new HashMap<>(stopCodes.length);

        for (String stopCode : stopCodes) {
            busStopServices.put(stopCode, bsd.getBusServicesForStopAsString(stopCode));
        }

        return busStopServices;
    }
}
