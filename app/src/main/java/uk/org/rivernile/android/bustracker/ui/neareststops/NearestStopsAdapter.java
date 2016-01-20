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

package uk.org.rivernile.android.bustracker.ui.neareststops;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link RecyclerView.Adapter} populates each row for a nearby bus stop.
 *
 * @author Niall Scott
 */
class NearestStopsAdapter extends RecyclerView.Adapter<NearestStopsAdapter.ViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    private final String[] directionStrings;
    private List<SearchResult> results;
    private WeakReference<OnItemClickedListener> itemClickedListener;

    /**
     * Create a new {@code NearestStopsAdapter}.
     *
     * @param context The {@link android.app.Activity} instance.
     */
    NearestStopsAdapter(@NonNull final Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        directionStrings = context.getResources().getStringArray(R.array.neareststops_orientations);
        setHasStableIds(true); // Enables animation of item changes.
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.neareststops_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.populate(getItem(position));
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    @Override
    public long getItemId(final int position) {
        final SearchResult item = getItem(position);
        return item != null ? item.hashCode() : 0;
    }

    /**
     * Get the item at the given position, or {@code null} if the item does not exist.
     *
     * @param position The position of the item to get.
     * @return The item at the given position, or {@code null} if the item does not exist.
     */
    @Nullable
    SearchResult getItem(final int position) {
        return position >= 0 && results != null && position < results.size() ?
                results.get(position) : null;
    }

    /**
     * Set the items to be shown by this {@link RecyclerView.Adapter}.
     *
     * @param results The items to be shown by this {@link RecyclerView.Adapter}.
     */
    void setSearchResults(@Nullable final List<SearchResult> results) {
        if (this.results != results) {
            this.results = results;
            notifyDataSetChanged();
        }
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
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private final ImageView imgDirection;
        private final TextView text1;
        private final TextView text2;
        private final TextView txtDistance;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The root {@link View} this {@code ViewHolder} is to hold.
         */
        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            imgDirection = (ImageView) itemView.findViewById(R.id.imgDirection);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
            txtDistance = (TextView) itemView.findViewById(R.id.txtDistance);
        }

        @Override
        public void onClick(final View v) {
            final int position = getAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final SearchResult item = getItem(position);

            if (item == null) {
                return;
            }

            final OnItemClickedListener listener =
                    itemClickedListener != null ? itemClickedListener.get() : null;

            if (listener != null) {
                listener.onItemClicked(item);
            }
        }

        @Override
        public boolean onLongClick(final View v) {
            final int position = getAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return false;
            }

            final SearchResult item = getItem(position);

            if (item == null) {
                return false;
            }

            final OnItemClickedListener listener =
                    itemClickedListener != null ? itemClickedListener.get() : null;

            return listener != null && listener.onItemLongClicked(item);
        }

        /**
         * Populate the {@link View}s in this {@code ViewHolder} with the data of the given
         * {@link SearchResult}, or if it's {@code null}, sets appropriate defaults.
         *
         * @param searchResult The object to populate the {@link View}s with.
         */
        void populate(@Nullable final SearchResult searchResult) {
            if (searchResult == null) {
                imgDirection.setImageResource(0);
                imgDirection.setContentDescription(null);
                text1.setText(null);
                text2.setText(null);
                txtDistance.setText(null);

                return;
            }

            final String locality = searchResult.getLocality();
            final String name;

            if (!TextUtils.isEmpty(locality)) {
                name = context.getString(R.string.busstop_locality_coloured,
                        searchResult.getStopName(), locality, searchResult.getStopCode());
            } else {
                name = context.getString(R.string.busstop_coloured,
                        searchResult.getStopName(), searchResult.getStopCode());
            }

            text1.setText(Html.fromHtml(name));
            text2.setText(searchResult.getServices());
            txtDistance.setText(context.getString(R.string.neareststops_distance_format,
                    (int) searchResult.getDistance()));
            final int orientation = searchResult.getOrientation();
            imgDirection.setImageResource(getDirectionDrawableResourceId(
                    searchResult.getOrientation()));

            if (orientation >=0 && orientation < 8) {
                imgDirection.setContentDescription(directionStrings[orientation]);
            } else {
                imgDirection.setContentDescription(
                        context.getString(R.string.neareststops_orientation_unknown));
            }
        }
    }

    /**
     * Get a drawable resource ID for a given {@code orientation}.
     *
     * @param orientation The orientation, expressed as a number between 0 and 7, with 0 being north
     * and 7 being north-west, going clockwise.
     * @return A drawable resource ID for a given {@code orientation}.
     */
    @DrawableRes
    static int getDirectionDrawableResourceId(final int orientation) {
        switch (orientation) {
            case 0:
                return R.drawable.mapmarker_n;
            case 1:
                return R.drawable.mapmarker_ne;
            case 2:
                return R.drawable.mapmarker_e;
            case 3:
                return R.drawable.mapmarker_se;
            case 4:
                return R.drawable.mapmarker_s;
            case 5:
                return R.drawable.mapmarker_sw;
            case 6:
                return R.drawable.mapmarker_w;
            case 7:
                return R.drawable.mapmarker_nw;
            default:
                return R.drawable.mapmarker;
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
         * @param item The clicked item.
         */
        void onItemClicked(@NonNull SearchResult item);

        /**
         * This is called when an item has been long clicked.
         *
         * @param item The clicked item.
         * @return {@code true} if the long click was handled, {@code false} if not.
         */
        boolean onItemLongClicked(@NonNull SearchResult item);
    }
}
