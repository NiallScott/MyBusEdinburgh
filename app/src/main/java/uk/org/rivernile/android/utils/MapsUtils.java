/*
 * Copyright (C) 2013 - 2021 Niall 'Rivernile' Scott
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
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import uk.org.rivernile.android.bustracker.core.features.FeatureRepository;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This class contains map utility methods.
 * 
 * @author Niall Scott
 * @deprecated See method deprecation notices for descriptions.
 */
@Deprecated
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
     * @deprecated Use {@link FeatureRepository#getHasStopMapUiFeature()} instead.
     */
    @Deprecated
    public static boolean isGoogleMapsAvailable(@NonNull final Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) ==
                ConnectionResult.SUCCESS && GraphicsUtils.getOpenGLESVersion(context) >= 2;
    }

    /**
     * Get a drawable resource ID for a given {@code orientation}.
     *
     * @param orientation The orientation, expressed as a number between 0 and 7, with 0 being north
     * and 7 being north-west, going clockwise.
     * @return A drawable resource ID for a given {@code orientation}.
     * @deprecated Use {@link uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator#getStopDirectionDrawableResourceId(int)}
     * instead.
     */
    @Deprecated
    @DrawableRes
    public static int getDirectionDrawableResourceId(final int orientation) {
        switch (orientation) {
            case 0:
                return R.drawable.mapmarker_n;
            case 1:
                return R.drawable.mapmarker_ne;
            case 2:
                return R.drawable.mapmarker_e;
            case 3:
                return R.drawable.mapmarker_se;
            case 4:
                return R.drawable.mapmarker_s;
            case 5:
                return R.drawable.mapmarker_sw;
            case 6:
                return R.drawable.mapmarker_w;
            case 7:
                return R.drawable.mapmarker_nw;
            default:
                return R.drawable.mapmarker;
        }
    }

    /**
     * Apply the correct direction icon to the {@link MarkerOptions} object based on the supplied
     * {@code orientation}.
     *
     * @param markerOptions The {@link MarkerOptions} to apply the direction icon to.
     * @param orientation The orientation of the stop, in the range of {@code 0} (north) to
     * {@code 7} (north-west), going clockwise. Any other number will be treated as unknown and the
     * stop will be given a generic icon instead.
     * @deprecated Use {@link uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator#applyStopDirectionToMarker(MarkerOptions, int)}
     * instead.
     */
    @Deprecated
    public static void applyStopDirectionToMarker(@NonNull final MarkerOptions markerOptions,
            final int orientation) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(
                getDirectionDrawableResourceId(orientation)));
    }
}