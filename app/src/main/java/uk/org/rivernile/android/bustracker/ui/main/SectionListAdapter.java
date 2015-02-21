/*
 * Copyright (C) 2014 - 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.lang.ref.WeakReference;

import uk.org.rivernile.android.bustracker.ui.main.sections.Section;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link RecyclerView.Adapter} is used to populate the items to display in the section list.
 * This shows the top level navigation of the application.
 *
 * @author Niall Scott
 */
public class SectionListAdapter extends RecyclerView.Adapter {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_DIVIDER = 1;

    private final Context context;
    private final LayoutInflater inflater;
    private Section[] sections;
    private WeakReference<OnSectionChosenListener> sectionChosenListener;
    private int selected = 0;

    /**
     * Create a new {@code SectionListAdapter}.
     *
     * @param context The {@link Context} of the hosting {@link android.app.Activity}.
     */
    public SectionListAdapter(@NonNull final Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return viewType == TYPE_NORMAL ?
                new ViewHolder(inflater.inflate(R.layout.sectionlist_item, parent, false)) :
                new DividerViewHolder(inflater.inflate(R.layout.list_divider, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Section section = sections[position];

        if (section == null) {
            return;
        }

        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.text1.setText(section.getTitle(context));
        viewHolder.text1.setCompoundDrawablesWithIntrinsicBounds(
                section.getIconResource(), 0, 0, 0);
        viewHolder.text1.setChecked(position == selected);
    }

    @Override
    public int getItemCount() {
        return sections != null ? sections.length : 0;
    }

    @Override
    public long getItemId(final int position) {
        final Section section = getItem(position);
        return section != null ? section.getIconResource() : 0;
    }

    @Override
    public int getItemViewType(final int position) {
        final Section section = getItem(position);
        return section != null ? TYPE_NORMAL : TYPE_DIVIDER;
    }

    /**
     * Set the {@link Section}s to show.
     *
     * @param sections The {@link Section}s to show.
     */
    public void setSections(@Nullable final Section[] sections) {
        if (this.sections != sections) {
            this.sections = sections;
            notifyDataSetChanged();
        }
    }

    /**
     * Set the listener to be called when the user has selected a {@link Section}.
     *
     * @param listener The listener that is called when the user has selected a {@link Section}.
     */
    public void setOnSectionChosenListener(@Nullable final OnSectionChosenListener listener) {
        if (listener != null) {
            sectionChosenListener = new WeakReference<OnSectionChosenListener>(listener);
        } else {
            sectionChosenListener = null;
        }
    }

    /**
     * Set the currently selected item.
     *
     * @param selected The currently selected item.
     */
    public void setSelected(final int selected) {
        final Section section = getItem(selected);
        final int oldSelected = this.selected;

        if (section != null && section.getFragmentTag() != null) {
            this.selected = selected;
        }

        if (this.selected != oldSelected) {
            notifyItemChanged(oldSelected);
            notifyItemChanged(selected);
        }
    }

    /**
     * Get the currently selected item.
     *
     * @return The currently selected item.
     */
    public int getSelected() {
        return selected;
    }

    /**
     * Get the {@link Section} at the given {@code position}.
     *
     * @param position The position of the {@link Section}.
     * @return The {@link Section} at the given {@code position}, or {@code null} if the
     *         {@link Section}s is currently set as {@code null}.
     */
    @Nullable
    private Section getItem(final int position) {
        return sections != null ? sections[position] : null;
    }

    /**
     * This {@link RecyclerView.ViewHolder} is used to display the {@link Section} item.
     */
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CheckedTextView text1;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The {@link View} for this {@link RecyclerView.ViewHolder}.
         */
        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            text1 = (CheckedTextView) itemView;
            text1.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            final int position = getPosition();

            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final OnSectionChosenListener listener =
                    sectionChosenListener != null ? sectionChosenListener.get() : null;

            if (listener != null) {
                setSelected(position);
                listener.onSectionChosen(sections[position]);
            }
        }
    }

    /**
     * This {@link RecyclerView.ViewHolder} is used to display a dividing line between two sections
     * in the list.
     */
    private static class DividerViewHolder extends RecyclerView.ViewHolder {

        /**
         * Create a new {@code DividerViewHolder}.
         *
         * @param itemView The {@link View} for this {@link RecyclerView.ViewHolder}.
         */
        DividerViewHolder(@NonNull final View itemView) {
            super(itemView);
        }
    }

    /**
     * This interface should be implemented by classes wishing to receive callbacks when a
     * {@link Section} has been chosen.
     *
     * @see #setOnSectionChosenListener(uk.org.rivernile.android.bustracker.ui.main.SectionListAdapter.OnSectionChosenListener)
     */
    public static interface OnSectionChosenListener {

        /**
         * This is called when a {@link Section} has been chosen.
         *
         * @param section The chosen {@link Section}.
         */
        public void onSectionChosen(@NonNull Section section);
    }
}
