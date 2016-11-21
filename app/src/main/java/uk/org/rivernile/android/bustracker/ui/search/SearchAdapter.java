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

package uk.org.rivernile.android.bustracker.ui.search;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.ui.RecyclerCursorAdapter;
import uk.org.rivernile.android.utils.MapsUtils;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link RecyclerView.Adapter} is responsible for populating the items in the search results.
 *
 * @author Niall Scott
 */
class SearchAdapter extends RecyclerCursorAdapter<SearchAdapter.ViewHolder> {

    private final String[] directionStrings;
    private WeakReference<OnItemClickedListener> itemClickedListener;
    private int columnStopCode;
    private int columnStopName;
    private int columnOrientation;
    private int columnLocality;
    private int columnServiceListing;

    /**
     * Create a new {@code SearchAdapter}.
     *
     * @param context The {@link android.app.Activity} instance.
     */
    SearchAdapter(@NonNull final Context context) {
        super(context);

        directionStrings = context.getResources().getStringArray(R.array.orientations);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(getLayoutInflater().inflate(R.layout.search_list_item, parent,
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
            columnStopCode = cursor.getColumnIndex(BusStopContract.BusStops.STOP_CODE);
            columnStopName = cursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME);
            columnOrientation = cursor.getColumnIndex(BusStopContract.BusStops.ORIENTATION);
            columnLocality = cursor.getColumnIndex(BusStopContract.BusStops.LOCALITY);
            columnServiceListing = cursor.getColumnIndex(BusStopContract.BusStops.SERVICE_LISTING);
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
     * This class holds on to the {@link View}s for a single row which can be recycled. It is also
     * responsible for populating its own and handling click events on the row.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView imgDirection;
        private final TextView text1;
        private final TextView text2;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The root {@link View} this {@code ViewHolder} is to hold.
         */
        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            imgDirection = (ImageView) itemView.findViewById(R.id.imgDirection);
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

        /**
         * Populate the {@link View}s in this {@code ViewHolder} with the data of the given
         * {@link Cursor}, or if it's {@code null}, sets appropriate defaults.
         *
         * @param cursor The object to populate the {@link View}s with.
         */
        private void populate(@Nullable final Cursor cursor) {
            if (cursor != null) {
                final String stopCode = cursor.getString(columnStopCode);
                final String stopName = cursor.getString(columnStopName);
                final String locality = cursor.getString(columnLocality);
                final int orientation = cursor.getInt(columnOrientation);

                if (!TextUtils.isEmpty(locality)) {
                    text1.setText(getContext().getString(R.string.busstop_locality, stopName,
                            locality, stopCode));
                } else {
                    text1.setText(getContext().getString(R.string.busstop, stopName, stopCode));
                }

                text2.setText(cursor.getString(columnServiceListing));
                imgDirection.setImageResource(MapsUtils.getDirectionDrawableResourceId(
                        orientation));

                if (orientation >= 0 && orientation < directionStrings.length) {
                    imgDirection.setContentDescription(directionStrings[orientation]);
                } else {
                    imgDirection.setContentDescription(getContext().getString(
                            R.string.orientation_unknown));
                }
            } else {
                text1.setText(null);
                text2.setText(null);
                imgDirection.setImageDrawable(null);
                imgDirection.setContentDescription(null);
            }
        }
    }

    /**
     * This interface should be implemented by classes wanting to receive callbacks when an item has
     * been clicked Register the callbacks by calling
     * {@link #setOnItemClickedListener(OnItemClickedListener)}.
     */
    interface OnItemClickedListener {

        /**
         * This is called when an item has been clicked.
         *
         * @param cursor The {@link Cursor} set to the position of the clicked item.
         */
        void onItemClicked(@NonNull Cursor cursor);
    }
}
