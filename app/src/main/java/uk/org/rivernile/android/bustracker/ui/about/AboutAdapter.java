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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This adapter provides the items to show in the list of application 'about' information.
 *
 * @author Niall Scott
 */
class AboutAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_SINGLE = 0;
    private static final int VIEW_TYPE_DOUBLE = 1;

    private final LayoutInflater inflater;
    private List<AboutItem> items;

    /**
     * Create a new {@code AboutAdapter}. This is used to populate the about items.
     *
     * @param context A {@link Context} instance.
     */
    AboutAdapter(@NonNull final Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Nullable
    @Override
    public AboutItem getItem(final int position) {
        return items != null ? items.get(position) : null;
    }

    @Override
    public long getItemId(final int position) {
        final AboutItem item = getItem(position);
        return item != null ? item.hashCode() : 0;
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable final View convertView,
                        @NonNull final ViewGroup parent) {
        final AboutItem item = getItem(position);

        if (item == null) {
            return null;
        }

        final boolean hasSubTitle = item.hasSubTitle();
        final View v;
        final ViewHolder holder;

        if (convertView == null) {
            v = inflater.inflate((hasSubTitle ?
                    R.layout.simple_list_item_2 : R.layout.simple_list_item_1), parent, false);
            holder = new ViewHolder();
            holder.text1 = (TextView) v.findViewById(android.R.id.text1);

            if (hasSubTitle) {
                holder.text2 = (TextView) v.findViewById(android.R.id.text2);
            }

            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }

        holder.text1.setText(item.getTitle());

        if (hasSubTitle) {
            holder.text2.setText(item.getSubTitle());
        }

        return v;
    }

    @Override
    public int getItemViewType(final int position) {
        final AboutItem item = getItem(position);
        return item == null || !item.hasSubTitle() ? VIEW_TYPE_SINGLE : VIEW_TYPE_DOUBLE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
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
     * This is used to hold references to {@link View}s during recycling.
     */
    private static class ViewHolder {
        TextView text1, text2;
    }
}
