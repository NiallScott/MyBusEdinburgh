/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.rivernile.android.utils;

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.AsyncTaskLoader;

/**
 * Used to write apps that run on platforms prior to Android 3.0. When running
 * on Android 3.0 or above, this implementation is still used; it does not try
 * to switch to the framework's implementation. See the framework SDK
 * documentation for a class overview.
 *
 * This was based on the CursorLoader class.
 * 
 * This source comes from http://stackoverflow.com/questions/7182485/
 * usage-cursorloader-without-contentprovider/7422343#7422343
 * 
 * Tidied up by Niall Scott
 */
public abstract class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {
    
    private Cursor mCursor;

    /**
     * Create a new SimpleCursorLoader.
     * 
     * @param context A Context object.
     */
    public SimpleCursorLoader(Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Cursor loadInBackground();

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverResult(Cursor cursor) {
        if(isReset()) {
            // An async query came in while the loader is stopped
            if(cursor != null) {
                cursor.close();
            }
            
            return;
        }
        
        final Cursor oldCursor = mCursor;
        mCursor = cursor;

        if(isStarted()) {
            super.deliverResult(cursor);
        }

        if(oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStartLoading() {
        if(mCursor != null) {
            deliverResult(mCursor);
        }
        
        if(takeContentChanged() || mCursor == null) {
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
    public void onCanceled(Cursor cursor) {
        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if(mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        
        mCursor = null;
    }
}