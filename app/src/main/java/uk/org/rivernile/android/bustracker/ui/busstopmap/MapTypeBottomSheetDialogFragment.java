/*
 * Copyright (C) 2017 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import uk.org.rivernile.android.bustracker.ui.widgets.CheckableLinearLayout;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This shows a bottom sheet which allows the user to select a map type.
 *
 * @author Niall Scott
 */
public class MapTypeBottomSheetDialogFragment extends BottomSheetDialogFragment
        implements View.OnClickListener {

    /**
     * An enumeration of map types.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE, MAP_TYPE_HYBRID })
    @interface MapType {}

    /** The map type is a normal map. */
    public static final int MAP_TYPE_NORMAL = 1;
    /** The map type is satellite, i.e. space imagery. */
    public static final int MAP_TYPE_SATELLITE = 2;
    /** The map type is hybrid. That is, street names are imposed on top of satellite imagery. */
    public static final int MAP_TYPE_HYBRID = 3;

    private static final String ARG_MAP_TYPE = "mapType";

    private CheckableLinearLayout layoutNormal;
    private CheckableLinearLayout layoutSatellite;
    private CheckableLinearLayout layoutHybrid;

    /**
     * Create a new instance of a {@code MapTypeBottomSheetDialogFragment}.
     *
     * @param mapType The type of the map.
     * @return A new instance of {@code MapTypeBottomSheetDialogFragment}.
     */
    @NonNull
    static MapTypeBottomSheetDialogFragment newInstance(@MapType final int mapType) {
        final MapTypeBottomSheetDialogFragment fragment = new MapTypeBottomSheetDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_MAP_TYPE, mapType);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.maptype_bottomsheet, container, false);

        layoutNormal = (CheckableLinearLayout) v.findViewById(R.id.layoutNormal);
        layoutSatellite = (CheckableLinearLayout) v.findViewById(R.id.layoutSatellite);
        layoutHybrid = (CheckableLinearLayout) v.findViewById(R.id.layoutHybrid);

        layoutNormal.setOnClickListener(this);
        layoutSatellite.setOnClickListener(this);
        layoutHybrid.setOnClickListener(this);

        setupStates();

        return v;
    }

    @Override
    public void onClick(final View v) {
        if (v == layoutNormal) {
            handleItemClicked(MAP_TYPE_NORMAL);
        } else if (v == layoutSatellite) {
            handleItemClicked(MAP_TYPE_SATELLITE);
        } else if (v == layoutHybrid) {
            handleItemClicked(MAP_TYPE_HYBRID);
        }
    }

    /**
     * Setup the row states depending on what the selected item is.
     */
    private void setupStates() {
        final int mapType = getArguments().getInt(ARG_MAP_TYPE);

        layoutNormal.setChecked(mapType == MAP_TYPE_NORMAL);
        layoutSatellite.setChecked(mapType == MAP_TYPE_SATELLITE);
        layoutHybrid.setChecked(mapType == MAP_TYPE_HYBRID);
    }

    /**
     * Handle an item being clicked.
     *
     * @param itemType The type of the item that was clicked.
     */
    private void handleItemClicked(@MapType final int itemType) {
        final Fragment targetFragment = getTargetFragment();

        if (targetFragment != null && targetFragment instanceof OnMapTypeSelectedListener) {
            ((OnMapTypeSelectedListener) targetFragment).onMapTypeSelected(itemType);
        }

        dismiss();
    }

    /**
     * Classes which wish to receive callbacks when the user makes a map type choice should
     * implement this interface.
     */
    interface OnMapTypeSelectedListener {

        /**
         * This is called when the user chooses a map type.
         *
         * @param mapType The map type the user has chosen.
         */
        void onMapTypeSelected(@MapType int mapType);
    }
}
