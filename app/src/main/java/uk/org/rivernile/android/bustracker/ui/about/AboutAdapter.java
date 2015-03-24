/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.about;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This adapter populates a list of 'about' items in a {@link RecyclerView}.
 *
 * @author Niall Scott
 */
class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {

    private static final int VIEW_TYPE_SINGLE = 0;
    private static final int VIEW_TYPE_DOUBLE = 1;

    private final LayoutInflater inflater;
    private List<AboutItem> items;
    private WeakReference<OnItemClickedListener> itemClickedListener;

    /**
     * Create a new {@code AboutAdapter}.
     *
     * @param context A {@link Context} instance from the hosting {@link android.app.Activity}.
     */
    AboutAdapter(@NonNull final Context context) {
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final int layout = (viewType == VIEW_TYPE_SINGLE ?
                R.layout.simple_list_item_1 : R.layout.simple_list_item_2);
        return new ViewHolder(inflater.inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final AboutItem item = getItem(position);

        if (item == null) {
            return;
        }

        holder.text1.setText(item.getTitle());
        holder.setIsClickable(item.isClickable());

        if (item.hasSubTitle() && holder.text2 != null) {
            holder.text2.setText(item.getSubTitle());
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public long getItemId(final int position) {
        final AboutItem item = getItem(position);
        return item != null ? item.hashCode() : 0;
    }

    @Override
    public int getItemViewType(final int position) {
        final AboutItem item = getItem(position);
        return item == null || !item.hasSubTitle() ? VIEW_TYPE_SINGLE : VIEW_TYPE_DOUBLE;
    }

    /**
     * Set the list of 'about' items to show.
     *
     * @param items The items to show.
     */
    void setAboutItems(@Nullable final List<AboutItem> items) {
        if (this.items != items) {
            this.items = items;
            notifyDataSetChanged();
        }
    }

    /**
     * Get the {@link AboutItem} at the given {@code position}.
     *
     * @param position The position to get the {@link AboutItem} for.
     * @return The {@link AboutItem} at the given {@code position}, or {@code null} if the items
     *         were not set.
     */
    @Nullable
    AboutItem getItem(final int position) {
        return items != null ? items.get(position) : null;
    }

    /**
     * Set the listener to be called when the user has clicked on an item.
     *
     * @param listener The listener that is called when the user has clicked on an item.
     */
    void setOnItemClickedListener(@Nullable final OnItemClickedListener listener) {
        if (listener != null) {
            itemClickedListener = new WeakReference<>(listener);
        } else {
            itemClickedListener = null;
        }
    }

    /**
     * The {@link RecyclerView.ViewHolder} to populate rows with. Also deals with clicking events.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        TextView text1, text2;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The root {@link View} of the item.
         */
        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            this.itemView = itemView;
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }

        @Override
        public void onClick(final View v) {
            final int position = getPosition();

            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final AboutItem item = getItem(position);

            if (item == null) {
                return;
            }

            final OnItemClickedListener listener =
                    itemClickedListener != null ? itemClickedListener.get() : null;

            if (listener != null) {
                listener.onItemClicked(item);
            }
        }

        /**
         * Set the clickable state of the item.
         *
         * @param clickable {@code true} if the item is clickable, {@code false} if not.
         */
        void setIsClickable(final boolean clickable) {
            itemView.setOnClickListener(clickable ? this : null);
            itemView.setClickable(clickable);
        }
    }

    /**
     * This interface should be implemented by any classes wishing to be notified when a user has
     * clicked an item.
     */
    static interface OnItemClickedListener {

        /**
         * This is called when an item has been clicked.
         *
         * @param item The clicked item.
         */
        void onItemClicked(@NonNull AboutItem item);
    }
}
