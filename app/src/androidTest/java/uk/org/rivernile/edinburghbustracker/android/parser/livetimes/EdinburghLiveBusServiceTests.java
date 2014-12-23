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

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests for {@link EdinburghLiveBusService}.
 * 
 * @author Niall Scott
 */
public class EdinburghLiveBusServiceTests extends TestCase {
    
    /**
     * Test that the getters return the correct values.
     */
    public void testGeneric() {
        final List<EdinburghLiveBus> buses = new ArrayList<EdinburghLiveBus>();
        final EdinburghLiveBusService service =
                new EdinburghLiveBusService("22", buses, "LB",
                        "Gyle -- Ocean Terminal", false, false);
        assertEquals("22", service.getServiceName());
        assertEquals(buses, service.getLiveBuses());
        assertEquals("LB", service.getOperator());
        assertEquals("Gyle -- Ocean Terminal", service.getRoute());
        assertFalse(service.isDisrupted());
        assertFalse(service.isDiverted());
    }
    
    /**
     * Test that {@link EdinburghLiveBusService#isDisrupted()} returns true when
     * the disrupted flag has been set.
     */
    public void testIsDisrupted() {
        final EdinburghLiveBusService service =
                new EdinburghLiveBusService("22",
                        new ArrayList<EdinburghLiveBus>(), "LB",
                        "Gyle -- Ocean Terminal", true, false);
        assertTrue(service.isDisrupted());
        assertFalse(service.isDiverted());
    }
    
    /**
     * Test that {@link EdinburghLiveBusService#isDiverted()} returns true when
     * the diverted flag has been set.
     */
    public void testIsDiverted() {
        final EdinburghLiveBusService service =
                new EdinburghLiveBusService("22",
                        new ArrayList<EdinburghLiveBus>(), "LB",
                        "Gyle -- Ocean Terminal", false, true);
        assertFalse(service.isDisrupted());
        assertTrue(service.isDiverted());
    }
}