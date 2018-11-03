/*
 * Copyright (C) 2015 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.parser.livetimes;

import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import uk.org.rivernile.android.fetchutils.loaders.Result;

/**
 * This extends {@link Result} to add a field to keep a record of the time the data was loaded at.
 *
 * @param <S> The type of the success object.
 */
public class LiveTimesResult<S> extends Result<S, LiveTimesException> {

    private final long loadTime;

    /**
     * {@inheritDoc}
     *
     * @param success {@inheritDoc}
     * @param loadTime The time the data was loaded at. This should be based on
     * {@link SystemClock#elapsedRealtime()}.
     */
    public LiveTimesResult(@Nullable final S success, final long loadTime) {
        super(success);

        this.loadTime = loadTime;
    }

    /**
     * {@inheritDoc}
     *
     * @param error {@inheritDoc}
     * @param loadTime The time the data was loaded at. This should be based on
     * {@link SystemClock#elapsedRealtime()}.
     */
    public LiveTimesResult(@NonNull final LiveTimesException error, final long loadTime) {
        super(error);

        this.loadTime = loadTime;
    }

    /**
     * Get the time the data was loaded at. This is based on {@link SystemClock#elapsedRealtime()}.
     *
     * @return The time the data was loaded at. This is based on
     * {@link SystemClock#elapsedRealtime()}.
     */
    public long getLoadTime() {
        return loadTime;
    }
}
