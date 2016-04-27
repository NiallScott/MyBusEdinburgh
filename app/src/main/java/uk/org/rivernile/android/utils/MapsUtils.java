/*
 * Copyright (C) 2013 - 2016 Niall 'Rivernile' Scott
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
package uk.org.rivernile.android.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This class contains map utility methods.
 * 
 * @author Niall Scott
 */
public final class MapsUtils {

    /**
     * This constructor is private to prevent instantiation.
     */
    private MapsUtils() { }
    
    /**
     * A utility method to determine if the Google Maps component will show on the device.
     * 
     * @param context A {@link Context} instance.
     * @return {@code true} if the Google Maps should show, {@code false} if not.
     */
    public static boolean isGoogleMapsAvailable(@NonNull final Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable (context) ==
                ConnectionResult.SUCCESS && GraphicsUtils.getOpenGLESVersion(context) >= 2;
    }

    /**
     * Apply the correct direction icon to the {@link MarkerOptions} object based on the supplied
     * {@code orientation}.
     *
     * @param markerOptions The {@link MarkerOptions} to apply the direction icon to.
     * @param orientation The orientation of the stop, in the range of {@code 0} (north) to
     * {@code 7} (north-west), going clockwise. Any other number will be treated as unknown and the
     * stop will be given a generic icon instead.
     */
    public static void applyStopDirectionToMarker(@NonNull final MarkerOptions markerOptions,
            final int orientation) {
        switch (orientation) {
            case 0:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_n));
                break;
            case 1:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_ne));
                break;
            case 2:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_e));
                break;
            case 3:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_se));
                break;
            case 4:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_s));
                break;
            case 5:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_sw));
                break;
            case 6:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_w));
                break;
            case 7:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_nw));
                break;
            default:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker));
                break;
        }
    }
}