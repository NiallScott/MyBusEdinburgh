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

package uk.org.rivernile.android.utils;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;

/**
 * This class extends the AsyncTaskLoader to make it behave better. The
 * solutions in here come from a variety of sources, mainly on StackOverflow and
 * SimpleCursorLoader.
 * 
 * @author Niall Scott
 * @param <D> The type of data that this Loader will return.
 */
public abstract class SimpleResultLoader<D> extends AsyncTaskLoader<D> {
    
    private D result;
    
    /**
     * Create a new SimpleResultLoader.
     * 
     * @param context A Context object.
     */
    public SimpleResultLoader(final Context context) {
        super(context);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStartLoading() {
        if(result != null) {
            // If a result already exists, deliver it.
            deliverResult(result);
        } else {
            // If a result does not exist, force a load.
            forceLoad();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onReset() {
        super.onReset();
        
        onStopLoading();
        
        // Reset to defaults.
        result = null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverResult(final D resultIn) {
        // If the Loader has been reset, do not deliver a result.
        if(isReset()) {
            return;
        }
        
        result = resultIn;

        // Deliver the result only if the Loader is in the started state.
        if(isStarted()) {
            super.deliverResult(resultIn);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract D loadInBackground();
}