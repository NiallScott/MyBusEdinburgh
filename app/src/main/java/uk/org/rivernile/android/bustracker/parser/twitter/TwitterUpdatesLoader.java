/*
 * Copyright (C) 2012 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.parser.twitter;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.fetchutils.loaders.Result;
import uk.org.rivernile.android.fetchutils.loaders.support.SimpleAsyncTaskLoader;

/**
 * This {@link SimpleAsyncTaskLoader} attempts to fetch a {@link List} of {@link Tweet}s from the
 * Twitter endpoint. A {@link Result} will be returned containing this {@link List} of
 * {@link Tweet}s, or a {@link TwitterException} if loading failed.
 * 
 * @author Niall Scott
 * @see SimpleAsyncTaskLoader
 */
public class TwitterUpdatesLoader
        extends SimpleAsyncTaskLoader<Result<List<Tweet>, TwitterException>> {
    
    private final BusApplication app;
    
    /**
     * Create a new {@code TwitterUpdatesLoader}.
     * 
     * @param context A {@link Context} object.
     */
    public TwitterUpdatesLoader(@NonNull final Context context) {
        super(context);
        
        app = (BusApplication) context.getApplicationContext();
    }

    @Override
    public Result<List<Tweet>, TwitterException> loadInBackground() {
        try {
            return new Result<>(app.getTwitterEndpoint().getTweets());
        } catch (TwitterException e) {
            return new Result<>(e);
        }
    }
}