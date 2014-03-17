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

package uk.org.rivernile.android.bustracker.endpoints;

import junit.framework.TestCase;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.edinburghbustracker.android.parser.livetimes
        .EdinburghParser;
import uk.org.rivernile.edinburghbustracker.android.utils.EdinburghUrlBuilder;

/**
 * Tests for HttpBusTrackerEndpoint.
 * 
 * @author Niall Scott
 */
public class HttpBusTrackerEndpointTests extends TestCase {
    
    private BusParser parser;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        parser = new EdinburghParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        parser = null;
    }
    
    /**
     * Test that the constructor throws an IllegalArgumentException if the
     * urlBuilder is null.
     */
    public void testConstructorWithNullUrlBuilder() {
        try {
            new HttpBusTrackerEndpoint(parser, null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The urlBuilder is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that no exceptions are thrown if the constructor is passed a
     * non-null urlBuilder.
     * 
     * @throws Exception If an Exception occurs, the test should fail.
     */
    public void testConstructorWithNonNullUrlBuilder() throws Exception {
        new HttpBusTrackerEndpoint(parser, new EdinburghUrlBuilder());
    }
}