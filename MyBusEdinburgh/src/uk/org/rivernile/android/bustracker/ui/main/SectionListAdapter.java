/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This {@link Adapter} is used to show a list of {@link Section}s to the user.
 * 
 * @author Niall Scott
 */
public class SectionListAdapter extends BaseAdapter {
    
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<Section> sections = new ArrayList<Section>();
    
    /**
     * Create a new {@code SectionListAdapter}.
     * 
     * @param context A {@link Context} instance. Must not be {@code null}.
     */
    public SectionListAdapter(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return sections.size();
    }

    @Override
    public Section getItem(final int position) {
        return position >= 0 && position < sections.size() ?
                sections.get(position) : null;
    }

    @Override
    public long getItemId(final int position) {
        final Section section = getItem(position);
        return section != null ? section.getIconResource() : 0;
    }

    @Override
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {
        final View v;
        final ViewHolder holder;
        
        if (convertView == null) {
            v = inflater.inflate(android.R.layout.simple_list_item_1, parent,
                    false);
            holder = new ViewHolder();
            holder.text1 = (TextView) v.findViewById(android.R.id.text1);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }
        
        final Section section = getItem(position);
        holder.text1.setText(section.getTitle(context));
        holder.text1.setCompoundDrawablesWithIntrinsicBounds(
                section.getIconResource(), 0, 0, 0);
        
        return v;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    /**
     * Get the {@link Context} used by this {@link Adapter}.
     * 
     * @return The {@link Context} used by this {@link Adpater}.
     */
    public final Context getContext() {
        return context;
    }
    
    /**
     * Add a section to be shown.
     * 
     * @param section The new {@link Section}. {@code null} will be ignored.
     */
    public void addSection(final Section section) {
        if (section == null) {
            return;
        }
        
        sections.add(section);
        notifyDataSetChanged();
    }
    
    /**
     * Add a {@link Collection} of {@link Section}s to be shown.
     * 
     * @param sections The {@link Collection} of {@link Section}s to be shown.
     * {@code null} and empty will be ignored.
     */
    public void addSections(final Collection<Section> sections) {
        if (sections == null || sections.isEmpty()) {
            return;
        }
        
        this.sections.ensureCapacity(this.sections.size() + sections.size());
        this.sections.addAll(sections);
        notifyDataSetChanged();
    }
    
    /**
     * Add an array of {@link Section} objects to be shown.
     * 
     * @param sections The {@link Section}s to add. {@code null} and empty will
     * be ignored.
     */
    public void addSections(final Section[] sections) {
        if (sections == null || sections.length == 0) {
            return;
        }
        
        this.sections.ensureCapacity(this.sections.size() + sections.length);
        
        for (Section s : sections) {
            this.sections.add(s);
        }
        
        notifyDataSetChanged();
    }
    
    /**
     * Remove a section.
     * 
     * @param section The {@link Section} to be removed.
     */
    public void removeSection(final Section section) {
        if (sections.remove(section)) {
            notifyDataSetChanged();
        }
    }
    
    /**
     * Get the position for the given {@link Section}.
     * 
     * @param section The {@link Section} to get the position for.
     * @return The position of the {@link Section}, or {@code -1} if it does not
     * exist in the adapter.
     */
    public int getPositionForSection(final Section section) {
        return sections.indexOf(section);
    }
    
    /**
     * This is used to hold references to {@link View}s while they are being
     * recycled because calls to {@link View#findViewById(int)} are expensive.
     */
    private static class ViewHolder {
        TextView text1;
    }
}