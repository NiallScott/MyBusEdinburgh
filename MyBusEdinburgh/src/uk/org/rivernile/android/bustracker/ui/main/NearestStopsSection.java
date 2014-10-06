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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .NearestStopsFragment;

/**
 * This defines the nearest bus stops section.
 * 
 * @author Niall Scott
 */
public class NearestStopsSection implements Section {
    
    private static NearestStopsSection instance;
    
    /**
     * Get an instance of this class. This class is safe to be a singleton as it
     * contains no mutable state.
     * 
     * @return An instance of this class.
     */
    public static NearestStopsSection getInstance() {
        if (instance == null) {
            instance = new NearestStopsSection();
        }
        
        return instance;
    }
    
    /**
     * This constructor is private to prevent outside instantiation.
     */
    private NearestStopsSection() {
        // No implementation.
    }

    @Override
    public CharSequence getTitle(final Context context) {
        return context.getString(R.string.neareststops_title);
    }

    @Override
    public int getIconResource() {
        return 0;
    }

    @Override
    public Fragment getFragment() {
        return new NearestStopsFragment();
    }

    @Override
    public String getFragmentTag() {
        return "TAG_NEAREST_STOPS";
    }
    
    @Override
    public void doAlternativeAction(final FragmentActivity activity) {
        // Nothing to do here.
    }
}