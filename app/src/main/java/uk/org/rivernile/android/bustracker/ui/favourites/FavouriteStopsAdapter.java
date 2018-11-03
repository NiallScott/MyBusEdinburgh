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

package uk.org.rivernile.android.bustracker.ui.favourites;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;
import uk.org.rivernile.android.bustracker.ui.RecyclerCursorAdapter;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link RecyclerView.Adapter} populates each row for a user's favourite bus stops.
 *
 * @author Niall Scott
 */
class FavouriteStopsAdapter extends RecyclerCursorAdapter<FavouriteStopsAdapter.ViewHolder> {

    private WeakReference<OnItemClickedListener> itemClickedListener;
    private Map<String, String> busStopServices;
    private int columnStopCode;
    private int columnStopName;

    /**
     * Create a new {@code FavouriteStopsAdapter}.
     *
     * @param context The {@link android.app.Activity} instance.
     */
    FavouriteStopsAdapter(@NonNull final Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(getLayoutInflater().inflate(R.layout.simple_list_item_2, parent,
                false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.populate(getItem(position));
    }

    @Nullable
    @Override
    public Cursor swapCursor(@Nullable final Cursor cursor) {
        if (cursor != null) {
            columnStopCode = cursor.getColumnIndex(SettingsContract.Favourites.STOP_CODE);
            columnStopName = cursor.getColumnIndex(SettingsContract.Favourites.STOP_NAME);
        }

        return super.swapCursor(cursor);
    }

    /**
     * Set the listener to be called when the user has clicked on an item.
     *
     * @param listener The listener that is called when the user has clicked on an item.
     */
    void setOnItemClickedListener(@Nullable final OnItemClickedListener listener) {
        itemClickedListener = listener != null ? new WeakReference<>(listener) : null;
    }

    /**
     * Set the {@link Map} of services that stop at the user's favourite bus stops.
     *
     * @param busStopServices The {@link Map} of services that stop at the user's favourite bus
     * stops.
     */
    void setBusStopServices(@Nullable final Map<String, String> busStopServices) {
        if (this.busStopServices != busStopServices) {
            this.busStopServices = busStopServices;
            notifyDataSetChanged();
        }
    }

    /**
     * This class holds on to the {@link View}s for a single row which can be recycled. It is also
     * responsible for populating its own and handling click events on the row.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private final TextView text1;
        private final TextView text2;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The root {@link View} this {@code ViewHolder} is to hold.
         */
        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            itemView.setLongClickable(true);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }

        @Override
        public void onClick(final View v) {
            final int position = getAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final Cursor cursor = getItem(position);

            if (cursor == null) {
                return;
            }

            final OnItemClickedListener listener =
                    itemClickedListener != null ? itemClickedListener.get() : null;

            if (listener != null) {
                listener.onItemClicked(cursor);
            }
        }

        @Override
        public boolean onLongClick(final View v) {
            final int position = getAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return false;
            }

            final Cursor cursor = getItem(position);

            if (cursor == null) {
                return false;
            }

            final OnItemClickedListener listener =
                    itemClickedListener != null ? itemClickedListener.get() : null;

            return listener != null && listener.onItemLongClicked(cursor);
        }

        /**
         * Populate the {@link View}s in this {@code ViewHolder} with the data of the given
         * {@link Cursor}, or if it's {@code null}, sets appropriate defaults.
         *
         * @param cursor The object to populate the {@link View}s with.
         */
        void populate(@Nullable final Cursor cursor) {
            if (cursor == null) {
                text1.setText(null);
                text2.setText(null);

                return;
            }

            text1.setText(cursor.getString(columnStopName));
            text2.setText(busStopServices != null ?
                    busStopServices.get(cursor.getString(columnStopCode)) : null);
        }
    }

    /**
     * This interface should be implemented by classes wanting to receive callbacks when an item has
     * been clicked or long clicked. Register the callbacks by calling
     * {@link #setOnItemClickedListener(OnItemClickedListener)}.
     */
    interface OnItemClickedListener {

        /**
         * This is called when an item has been clicked.
         *
         * @param cursor The {@link Cursor} set to the position of the clicked item.
         */
        void onItemClicked(Cursor cursor);

        /**
         * This is called when an item has been long clicked.
         *
         * @param cursor The {@link Cursor} set to the position of the clicked item.
         * @return {@code true} if the long click was handled, {@code false} if not.
         */
        boolean onItemLongClicked(Cursor cursor);
    }
}
