/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * Tests for {@link MapsUtils}.
 *
 * @author Niall Scott
 */
public class MapsUtilsTests {

    /**
     * Test that {@link MapsUtils#getDirectionDrawableResourceId(int)} returns the
     * correct direction {@link android.graphics.drawable.Drawable} resource IDs for the given
     * orientation values.
     */
    @Test
    public void testGetDirectionDrawableResourceId() {
        assertEquals(R.drawable.mapmarker, MapsUtils.getDirectionDrawableResourceId(-1));
        assertEquals(R.drawable.mapmarker_n,
                MapsUtils.getDirectionDrawableResourceId(0));
        assertEquals(R.drawable.mapmarker_ne,
                MapsUtils.getDirectionDrawableResourceId(1));
        assertEquals(R.drawable.mapmarker_e,
                MapsUtils.getDirectionDrawableResourceId(2));
        assertEquals(R.drawable.mapmarker_se,
                MapsUtils.getDirectionDrawableResourceId(3));
        assertEquals(R.drawable.mapmarker_s,
                MapsUtils.getDirectionDrawableResourceId(4));
        assertEquals(R.drawable.mapmarker_sw,
                MapsUtils.getDirectionDrawableResourceId(5));
        assertEquals(R.drawable.mapmarker_w,
                MapsUtils.getDirectionDrawableResourceId(6));
        assertEquals(R.drawable.mapmarker_nw,
                MapsUtils.getDirectionDrawableResourceId(7));
        assertEquals(R.drawable.mapmarker, MapsUtils.getDirectionDrawableResourceId(8));
    }
}
