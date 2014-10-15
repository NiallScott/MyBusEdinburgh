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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link ListFragment} makes up the view in the application navigation
 * drawer. It shows a {@link ListView} of application {@link Section}s to the
 * user, which they can choose to navigate to.
 * 
 * @author Niall Scott
 */
public class SectionListFragment extends ListFragment {
    
    private Callbacks callbacks;
    private SectionListAdapter adapter;
    
    private ListView listView;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName()
                    + " does not implement " + Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Section[] sections = new Section[] {
            FavouritesSection.getInstance(),
            EnterStopCodeSection.getInstance(),
            BusStopMapSection.getInstance(),
            NearestStopsSection.getInstance(),
            NewsSection.getInstance(),
            AlertManagerSection.getInstance(),
            null, // Divider.
            SettingsSection.getInstance(),
            AboutSection.getInstance()
        };
        
        adapter = new SectionListAdapter(getActivity());
        adapter.addSections(sections);
        
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.sectionlist_fragment,
                container, false);
        listView = (ListView) v.findViewById(android.R.id.list);
        
        return v;
    }

    @Override
    public void onListItemClick(final ListView l, View v, final int position,
            final long id) {
        final Section section = adapter.getItem(position);
        callbacks.onSectionChosen(section);
    }
    
    /**
     * Set a {@link Section} as the selected {@link Section}.
     * 
     * @param section The {@link Section} to select as the chosen one.
     */
    public void setSectionAsSelected(final Section section) {
        final int position = adapter.getPositionForSection(section);
        if (position >= 0) {
            listView.setItemChecked(position, true);
        }
    }
    
    /**
     * {@link Activity Activities} which host this {@link Fragment} must
     * implement this interface.
     */
    public static interface Callbacks {
        
        /**
         * This is called when the user has chosen a section.
         * 
         * @param section The section that the user has chosen.
         */
        public void onSectionChosen(Section section);
    }
}