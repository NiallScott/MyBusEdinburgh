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

package uk.org.rivernile.android.bustracker.ui.main.sections;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import uk.org.rivernile.edinburghbustracker.android.BusStopMapActivity;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This defines the bus stop map section.
 * 
 * @author Niall Scott
 */
public class BusStopMapSection implements Section {
    
    private static BusStopMapSection instance;
    
    /**
     * Get an instance of this class. This class is safe to be a singleton as it contains no mutable
     * state.
     * 
     * @return An instance of this class.
     */
    @NonNull
    public static BusStopMapSection getInstance() {
        if (instance == null) {
            instance = new BusStopMapSection();
        }
        
        return instance;
    }
    
    /**
     * This constructor is private to prevent outside instantiation.
     */
    private BusStopMapSection() {
        // No implementation.
    }

    @Override
    @NonNull
    public CharSequence getTitle(@NonNull final Context context) {
        return context.getString(R.string.map_title);
    }

    @Override
    @DrawableRes
    public int getIconResource() {
        return R.drawable.ic_drawer_map;
    }

    @Override
    @Nullable
    public Fragment getFragment() {
        return null;
    }

    @Override
    @Nullable
    public String getFragmentTag() {
        return null;
    }
    
    @Override
    public void doAlternativeAction(@NonNull final FragmentActivity activity) {
        final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        
        if (errorCode == ConnectionResult.SUCCESS) {
            activity.startActivity(new Intent(activity, BusStopMapActivity.class));
        } else {
            GooglePlayServicesUtil.showErrorDialogFragment(errorCode, activity, 1);
        }
    }
}