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

package uk.org.rivernile.edinburghbustracker.android;

import static org.junit.Assert.assertTrue;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.org.rivernile.edinburghbustracker.android.ui.bustimes.EdinburghDisplayStopDataFragment;

/**
 * Tests for {@link EdinburghFragmentFactory}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class EdinburghFragmentFactoryTests {

    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    
    private EdinburghFragmentFactory factory;

    @Before
    public void setUp() {
        factory = new EdinburghFragmentFactory();
    }

    @After
    public void tearDown() {
        factory = null;
    }
    
    /**
     * Test that {@link EdinburghFragmentFactory#getDisplayStopDataFragment(java.lang.String)}
     * returns a Fragment that is an instance of {@link EdinburghDisplayStopDataFragment}.
     */
    @Test
    @UiThreadTest
    public void testGetDisplayStopDataFragment() {
        assertTrue("getDisplayStopDataFragment() must return a Fragment that is an instance of " +
                        "EdinburghDisplayStopDataFragment.",
                factory.getDisplayStopDataFragment("123456") instanceof
                        EdinburghDisplayStopDataFragment);
    }
}