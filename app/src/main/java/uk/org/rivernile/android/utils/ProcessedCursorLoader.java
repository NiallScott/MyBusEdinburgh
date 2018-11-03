/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.core.content.ContentResolverCompat;
import androidx.core.os.CancellationSignal;
import androidx.core.os.OperationCanceledException;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * This {@link Loader} is a different type of
 * {@link CursorLoader} that allows data from a {@link Cursor} to be
 * processed in Java code before being returned as the loading result.
 *
 * <p>
 *     For example, you may want to get a collection of data from the {@link Cursor}. This may be a
 *     key to value mapping which would be horribly inefficient to loop through the {@link Cursor}
 *     every time you wish to find the value for a key. Using a {@code ProcessedCursorLoader}, you
 *     can iterate through the {@link Cursor} only once and create a {@link java.util.Map} of this
 *     data which would then be very quick to look up.
 * </p>
 *
 * <p>
 *     The processing happens on the same thread that the {@link Cursor} is loaded on, i.e. not the
 *     main thread. After the {@link Cursor} is loaded, {@link #processCursor(Cursor)} will be
 *     called with the {@link Cursor} reference to give subclasses the opportunity to process the
 *     {@link Cursor} data. It's acceptable to return {@code null} from
 *     {@link #processCursor(Cursor)} but that would render this class useless. If that's the case,
 *     revert back to {@link CursorLoader} and if required override
 *     {@link CursorLoader#loadInBackground()} instead.
 * </p>
 *
 * <p>
 *     As for the management of the {@link Cursor} itself, the implementation in this class copies
 *     that of {@link CursorLoader} and therefore closing of the
 *     {@link Cursor} is handled in here for you.
 * </p>
 *
 * <p>
 *     All data returned from this class is wrapped inside {@link ResultWrapper}, which is a tuple
 *     of the {@link Cursor} and an object representing the processed result.
 * </p>
 *
 * @param <T> The type of object that holds the processed result.
 * @author Google Inc. and Niall Scott
 */
public abstract class ProcessedCursorLoader<T>
        extends AsyncTaskLoader<ProcessedCursorLoader.ResultWrapper<T>> {

    private final ForceLoadContentObserver forceLoadObserver = new ForceLoadContentObserver();

    private Uri uri;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String sortOrder;

    private ResultWrapper<T> result;
    private CancellationSignal cancellationSignal;

    /**
     * Create a new {@code ProcessedCursorLoader}. The query must later be defined by calling the
     * query field setters.
     *
     * @param context A {@link Context} instance.
     */
    public ProcessedCursorLoader(@NonNull final Context context) {
        super(context);
    }

    /**
     * Create a new {@code ProcessedCursorLoader}. This constructor is the same as a
     * {@link CursorLoader}.
     *
     * @param context A {@link Context} instance.
     * @param uri The {@link Uri} of the data source.
     * @param projection The columns to fetch for the {@link Cursor}.
     * @param selection The {@code WHERE} clause.
     * @param selectionArgs The arguments to place in the {@code WHERE} clause.
     * @param sortOrder The sort of the returned data.
     */
    public ProcessedCursorLoader(@NonNull final Context context, @NonNull final Uri uri,
            @Nullable final String[] projection, @Nullable final String selection,
            @Nullable final String[] selectionArgs, @Nullable final String sortOrder) {
        super(context);

        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }

    @Override
    public ResultWrapper<T> loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }

            cancellationSignal = new CancellationSignal();
        }

        try {
            final Cursor cursor = ContentResolverCompat.query(getContext().getContentResolver(),
                    uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);

            if (cursor != null) {
                try {
                    cursor.getCount();
                    cursor.registerContentObserver(forceLoadObserver);
                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }

            return new ResultWrapper<>(cursor, processCursor(cursor));
        } finally {
            synchronized (this) {
                cancellationSignal = null;
            }
        }
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
        }
    }

    @Override
    public void deliverResult(final ResultWrapper<T> resultWrapper) {
        final Cursor cursor = resultWrapper != null ? resultWrapper.getCursor() : null;

        if (isReset()) {
            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        final Cursor oldCursor = result != null ? result.getCursor() : null;
        result = resultWrapper;

        if (isStarted()) {
            super.deliverResult(resultWrapper);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if (result != null) {
            deliverResult(result);
        }

        if (takeContentChanged() || result == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(final ResultWrapper<T> resultWrapper) {
        final Cursor cursor = resultWrapper != null ? resultWrapper.getCursor() : null;

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        final Cursor cursor = result != null ? result.getCursor() : null;

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        result = null;
    }

    @Override
    public void dump(final String prefix, final FileDescriptor fd, final PrintWriter writer,
            final String[] args) {
        super.dump(prefix, fd, writer, args);

        writer.print(prefix); writer.print("uri="); writer.println(uri);
        writer.print(prefix); writer.print("projection=");
        writer.println(Arrays.toString(projection));
        writer.print(prefix); writer.print("selection="); writer.println(selection);
        writer.print(prefix); writer.print("selectionArgs=");
        writer.println(Arrays.toString(selectionArgs));
        writer.print(prefix); writer.print("sortOrder="); writer.println(sortOrder);
        writer.print(prefix); writer.print("result="); writer.println(result);
    }

    /**
     * Process a {@link Cursor} to return a resulting object. It is acceptable to return
     * {@code null} here, but if you did that, you would be better off extending
     * {@link CursorLoader} and overriding
     * {@link CursorLoader#loadInBackground()}.
     *
     * @param cursor The {@link Cursor} to process. This could be {@code null} so please sanity
     * check the {@link Cursor} before using it.
     * @return An object which represents the processed result of the {@link Cursor}.
     */
    @Nullable
    public abstract T processCursor(@Nullable Cursor cursor);

    /**
     * Set the {@link Uri} of the data source.
     *
     * @param uri The {@link Uri} of the data source.
     */
    public void setUri(@NonNull final Uri uri) {
        this.uri = uri;
    }

    /**
     * Set the columns to fetch for the {@link Cursor}.
     *
     * @param projection The columns to fetch for the {@link Cursor}.
     */
    public void setProjection(@Nullable final String[] projection) {
        this.projection = projection;
    }

    /**
     * Set the {@code WHERE} clause.
     *
     * @param selection The {@code WHERE} clause.
     */
    public void setSelection(@Nullable final String selection) {
        this.selection = selection;
    }

    /**
     * Set the arguments to place in the {@code WHERE} clause.
     *
     * @param selectionArgs The arguments to place in the {@code WHERE} clause.
     */
    public void setSelectionArgs(@Nullable final String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    /**
     * Set the sort of the returned data.
     *
     * @param sortOrder The sort of the returned data.
     */
    public void setSortOrder(@Nullable final String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * This object represents the result of a loading operation by a {@link ProcessedCursorLoader}.
     *
     * @param <T> The same type the {@link ProcessedCursorLoader} was instantiated with.
     */
    public static class ResultWrapper<T> {

        private final Cursor cursor;
        private final T result;

        /**
         * Create a new {@code ResultWrapper}. This is only used internally by
         * {@link ProcessedCursorLoader}.
         *
         * @param cursor The loaded {@link Cursor}.
         * @param result An object representing the result of processing the {@link Cursor}.
         */
        private ResultWrapper(@Nullable final Cursor cursor, @Nullable final T result) {
            this.cursor = cursor;
            this.result = result;
        }

        /**
         * Get the loaded {@link Cursor}.
         *
         * @return The loaded {@link Cursor}.
         */
        @Nullable
        public Cursor getCursor() {
            return cursor;
        }

        /**
         * Get the object representing the result of processing the {@link Cursor}.
         *
         * @return The object representing the result of processing the {@link Cursor}.
         */
        @Nullable
        public T getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "{ cursor = " + cursor + "; result = " + result + '}';
        }
    }
}
