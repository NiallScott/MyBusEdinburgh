/*
 * Copyright (C) 2014 - 2018 Niall 'Rivernile' Scott
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
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.org.rivernile.android.bustracker.ui.main.sections.AboutSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.AlertManagerSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.BusStopMapSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.FavouritesSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.NearestStopsSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.NewsSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.Section;
import uk.org.rivernile.android.bustracker.ui.main.sections.SettingsSection;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link ListFragment} makes up the view in the application navigation drawer. It shows a
 * {@link RecyclerView} of application {@link Section}s to the user, which they can choose to
 * navigate to.
 * 
 * @author Niall Scott
 */
public class SectionListFragment extends Fragment {

    private static final String KEY_SELECTED = "position";
    
    private Callbacks callbacks;
    private SectionListAdapter adapter;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        
        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() + " does not implement " +
                    Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final Activity activity = getActivity();
        final ArrayList<Section> sectionsList = new ArrayList<>(9);
        sectionsList.add(FavouritesSection.getInstance());
        sectionsList.add(BusStopMapSection.getInstance());

        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
            // The Nearest Stops feature is useless without the device having location services. If
            // there's no location services, don't show it as an option to the user.
            sectionsList.add(NearestStopsSection.getInstance());
        }

        sectionsList.add(NewsSection.getInstance());
        sectionsList.add(AlertManagerSection.getInstance());
        sectionsList.add(null);
        sectionsList.add(SettingsSection.getInstance());
        sectionsList.add(AboutSection.getInstance());
        
        final Section[] sections = new Section[sectionsList.size()];
        sectionsList.toArray(sections);
        
        adapter = new SectionListAdapter(activity);
        adapter.setOnSectionChosenListener(callbacks);
        adapter.setSections(sections);

        if (savedInstanceState != null) {
            adapter.setSelected(savedInstanceState.getInt(KEY_SELECTED));
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.sectionlist_fragment, container, false);
        final RecyclerView recyclerView = (RecyclerView) v.findViewById(android.R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        
        return v;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_SELECTED, adapter.getSelected());
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.section_list_option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.section_list_option_menu_search:
                callbacks.onShowSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@link Activity Activities} which host this {@link Fragment} must implement this interface.
     */
    public interface Callbacks extends SectionListAdapter.OnSectionChosenListener {

        /**
         * This is called when the user wishes to perform a search.
         */
        void onShowSearch();
    }
}