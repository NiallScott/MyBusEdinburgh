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

package uk.org.rivernile.android.bustracker.ui.busstopmap;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;

/**
 * This task is used to obtain a service listing for a given stop code.
 *
 * @author Niall Scott
 */
class GetStopServicesTask extends AsyncTask<String, Void, String> {

    private final WeakReference<Context> contextRef;
    private final WeakReference<OnStopServicesLoadedListener> stopServicesLoadedListenerRef;

    /**
     * Create a new {@code GetStopServicesTask}.
     *
     * @param context A {@link Context} instance.
     * @param listener A listener that is called when the service listing has finished loading.
     */
    GetStopServicesTask(@NonNull final Context context,
            @NonNull final OnStopServicesLoadedListener listener) {
        contextRef = new WeakReference<>(context);
        stopServicesLoadedListenerRef = new WeakReference<>(listener);
    }

    @Override
    protected String doInBackground(final String... stopCode) {
        final Context context = contextRef.get();

        if (context != null) {
            return BusStopDatabase.getServicesForStop(context, stopCode[0]);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final String services) {
        final OnStopServicesLoadedListener listener = stopServicesLoadedListenerRef.get();

        if (listener != null) {
            listener.onStopServicesLoaded(services);
        }
    }

    /**
     * Implement this interface to receive a callback when services have been loaded for the given
     * stop code.
     */
    interface OnStopServicesLoadedListener {

        /**
         * This is called when services have finished loading for the given stop code.
         *
         * @param services The services for the given stop code.
         */
        void onStopServicesLoaded(@Nullable String services);
    }
}
