/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;

/**
 * This class is used to call ActionBar related methods in Hoeycomb and upwards.
 * It is designed to live in a package which may be used in 1.6 upwards. If you
 * are not using Honeycomb or upwards, then DO NOT REFERENCE THIS CLASS AT ALL,
 * especially in Android 1.6.
 * 
 * @author Niall Scott
 */
@TargetApi(11)
public class ActionBarCompat {
    
    /**
     * Taken from Android Developer documents:
     * 
     * <p>Set whether home should be displayed as an "up" affordance. Set this
     * to true if selecting "home" returns up by a single level in your UI
     * rather than back to the top level or front page.</p>
     * 
     * <p>To set several display options at once, see the setDisplayOptions
     * methods.</p>
     * 
     * @param activity The Activity where the ActionBar lives.
     * @param showHomeAsUp true to show the user that selecting home will return
     * one level up rather than to the top level of the app.
     */
    public static void setDisplayHomeAsUpEnabled(final Activity activity,
            final boolean showHomeAsUp) {
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
        }
    }
}