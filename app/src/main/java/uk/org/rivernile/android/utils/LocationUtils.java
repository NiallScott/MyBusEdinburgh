/*
 * Copyright (C) 2013 - 2022 Niall 'Rivernile' Scott
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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * This class contains various methods which help with location services. Some of the code is
 * taken from the Android Developer website. Any bits of code which have been taken from
 * elsewhere will be marked in their method Javadoc.
 *
 * @author Niall Scott
 */
public class LocationUtils {

    /**
     * Prevent instantiation of this class.
     */
    private LocationUtils() {
        // Nothing to do here.
    }

    /**
     * Does the package for the given {@link Context} have permission to use location services?
     *
     * @param context The {@link Context} of the package to check permission on.
     * @return {@code true} if the package has permission to use location services, {@code false} if
     * not.
     */
    public static boolean checkLocationPermission(@NonNull final Context context) {
        final boolean hasFineLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        final boolean hasCoarseLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return hasFineLocation && hasCoarseLocation;
    }
}