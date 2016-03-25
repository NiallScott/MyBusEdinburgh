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
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

/**
 * This class contains utility methods related to handling of graphics.
 * 
 * @author Niall Scott
 */
public final class GraphicsUtils {

    /**
     * This constructor is private to prevent instantiation.
     */
    private GraphicsUtils() { }

    /**
     * This method returns the version code of OpenGL ES on the system. If the version could not
     * be determined, version 1 is assumed.
     *
     * <p>
     *     This code came from;
     *     <a href="http://stackoverflow.com/questions/6450709/detect-if-opengl-es-2-0-is-available-or-not">
     *     http://stackoverflow.com/questions/6450709/detect-if-opengl-es-2-0-is-available-or-not
     *     </a>
     * </p>
     * 
     * @param context A {@link Context} instance.
     * @return The OpenGL ES version.
     */
    public static int getOpenGLESVersion(@NonNull final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final FeatureInfo[] featureInfos = packageManager.getSystemAvailableFeatures();
        
        if (featureInfos != null && featureInfos.length > 0) {
            for (FeatureInfo featureInfo : featureInfos) {
                // Null feature name means this feature is the OpenGL ES version feature.
                if (featureInfo.name == null) {
                    if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
                        return (featureInfo.reqGlEsVersion & 0xffff0000 ) >> 16;
                    } else {
                        return 1; // Lack of property means OpenGL ES version 1
                    }
                }
            }
        }
        
        return 1;
    }
}