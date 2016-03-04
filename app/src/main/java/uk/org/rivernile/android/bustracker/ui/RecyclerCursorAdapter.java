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

package uk.org.rivernile.android.bustracker.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

/**
 * This class intends to mimic the platform-supplied {@link android.widget.CursorAdapter}, but
 * instead for {@link RecyclerView} (as {@link android.widget.AbsListView} adapters and
 * {@link RecyclerView} adapters are not compatible).
 *
 * <p>
 *     Some of the features in {@link android.widget.CursorAdapter} are missed out, and for good
 *     reason. They allowed {@link Cursor}s to be re-queried on the main UI thread. This
 *     implementation does the bare minimal to support {@link Cursor}s within {@link RecyclerView}.
 *     Loading a {@link Cursor}, including listening for data changes and re-querying upon changes,
 *     should be done somewhere else, on another thread.
 * </p>
 *
 * <p>
 *     As per the same contract within {@link android.widget.CursorAdapter}, the {@link Cursor}
 *     must have a column as defined by {@link BaseColumns#_ID}, otherwise an exception will be
 *     thrown.
 * </p>
 *
 * @author Niall Scott
 * @param <VH> The class to be used for the {@link RecyclerView.ViewHolder}.
 */
public abstract class RecyclerCursorAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final Context context;
    private final LayoutInflater inflater;
    private Cursor cursor;
    private int rowIdColumn;

    /**
     * Create a new {@code RecyclerCursorAdapter}. Set the {@link Cursor} to use in
     * {@link #changeCursor(Cursor)} or {@link #swapCursor(Cursor)}.
     *
     * @param context The {@link Context} of the {@link android.app.Activity} hosting this adapter.
     */
    public RecyclerCursorAdapter(@NonNull final Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    @Override
    public long getItemId(final int position) {
        final Cursor cursor = getItem(position);
        return cursor != null ? cursor.getLong(rowIdColumn) : 0;
    }

    /**
     * Get the {@link Context} for this adapter.
     *
     * @return The {@link Context} for this adapter.
     */
    @NonNull
    public final Context getContext() {
        return context;
    }

    /**
     * Get the {@link LayoutInflater} to use to inflate layouts for this adapter.
     *
     * @return The {@link LayoutInflater} to use to inflate layouts for this adapter.
     */
    @NonNull
    public final LayoutInflater getLayoutInflater() {
        return inflater;
    }

    /**
     * Get the {@link Cursor} currently set on this adapter.
     *
     * @return The {@link Cursor} currently set on this adapter, or {@code null} if there is no
     * {@link Cursor}.
     */
    @Nullable
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * If there is a {@link Cursor} set on this adapter, returns the instance of this {@link Cursor}
     * with the position pre-set to the postion specified. Otherwise, {@code null} is returned.
     *
     * @param position The position within the {@link Cursor} to get.
     * @return The {@link Cursor} with its position pre-set, or {@code null} if there is no
     * {@link Cursor} or its position could not be set.
     */
    @Nullable
    public Cursor getItem(final int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            return cursor;
        } else {
            return null;
        }
    }

    /**
     * Change the underlying {@link Cursor} to a new {@link Cursor}. If there is an existing cursor
     * it will be closed.
     *
     * @param cursor The new {@link Cursor} to be used.
     * @throws IllegalArgumentException When the new {@link Cursor} does not have a column defined
     * by {@link BaseColumns#_ID}.
     */
    public void changeCursor(@Nullable final Cursor cursor) {
        final Cursor old = swapCursor(cursor);

        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new {@link Cursor}, returning the old {@link Cursor}.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old {@link Cursor} is <em>not</em> closed.
     *
     * @param cursor The new {@link Cursor} to be used.
     * @return The previously set {@link Cursor}, or {@code null} if there was not one. If the
     * given new {@link Cursor} is the same instance is the previously set {@link Cursor},
     * {@code null} is also returned.
     * @throws IllegalArgumentException When the new {@link Cursor} does not have a column defined
     * by {@link BaseColumns#_ID}.
     */
    @Nullable
    public Cursor swapCursor(@Nullable final Cursor cursor) {
        if (this.cursor == cursor) {
            return null;
        }

        final Cursor oldCursor = this.cursor;
        this.cursor = cursor;
        rowIdColumn = cursor != null ? cursor.getColumnIndexOrThrow(BaseColumns._ID) : -1;
        notifyDataSetChanged();

        return oldCursor;
    }
}
