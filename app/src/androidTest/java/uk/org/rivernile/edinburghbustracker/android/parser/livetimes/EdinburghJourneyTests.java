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
import junit.framework.TestCase;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;

/**
 * Tests for {@link EdinburghJourney}.
 * 
 * @author Niall Scott
 */
public class EdinburghJourneyTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the terminus is set to null.
     */
    public void testConstructorWithNullTerminus() {
        try {
            new EdinburghJourney("123", "22",
                    new ArrayList<EdinburghJourneyDeparture>(), null, null,
                    null, null, false, false, false, 123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The terminus is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the terminus is set to empty.
     */
    public void testConstructorWithEmptyTerminus() {
        try {
            new EdinburghJourney("123", "22",
                    new ArrayList<EdinburghJourneyDeparture>(), null, null,
                    null, "", false, false, false, 123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The terminus is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the getters return the correct values.
     */
    public void testGeneralValid() {
        final Journey journey = new EdinburghJourney("123", "25",
                new ArrayList<EdinburghJourneyDeparture>(), "LB",
                "Riccarton -- Restalrig", "Riccarton", "123456", false, false,
                false, 123456789L);
        assertEquals("123", journey.getJourneyId());
        assertEquals("25", journey.getServiceName());
        assertTrue(journey.getDepartures().isEmpty());
        assertEquals("LB", journey.getOperator());
        assertEquals("Riccarton -- Restalrig", journey.getRoute());
        assertEquals("Riccarton", journey.getDestination());
        assertEquals("123456", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
    }
    
    /**
     * Test that {@link EdinburghJourney#hasGlobalDisruption()} returns true
     * when 'globalDisruption' is set to true in the constructor.
     */
    public void testHasGlobalDisruption() {
        final Journey journey = new EdinburghJourney("123", "25",
                new ArrayList<EdinburghJourneyDeparture>(), "LB",
                "Riccarton -- Restalrig", "Riccarton", "123456", true, false,
                false, 123456789L);
        assertTrue(journey.hasGlobalDisruption());
    }
    
    /**
     * Test that {@link EdinburghJourney#hasServiceDisruption()} returns true
     * when 'serviceDisruption' is set to true in the constructor.
     */
    public void testHasServiceDisruption() {
        final Journey journey = new EdinburghJourney("123", "25",
                new ArrayList<EdinburghJourneyDeparture>(), "LB",
                "Riccarton -- Restalrig", "Riccarton", "123456", false, true,
                false, 123456789L);
        assertTrue(journey.hasServiceDisruption());
    }
    
    /**
     * Test that {@link EdinburghJourney#hasServiceDiversion()} returns true
     * when 'serviceDiversion' is set to true in the constructor.
     */
    public void testHasServiceDiversion() {
        final Journey journey = new EdinburghJourney("123", "25",
                new ArrayList<EdinburghJourneyDeparture>(), "LB",
                "Riccarton -- Restalrig", "Riccarton", "123456", false, false,
                true, 123456789L);
        assertTrue(journey.hasServiceDiversion());
    }
}