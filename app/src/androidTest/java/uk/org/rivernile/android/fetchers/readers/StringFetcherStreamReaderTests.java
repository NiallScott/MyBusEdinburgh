/*
 * Copyright (C) 2013 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.fetchers.readers;

import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests for StringFetcherStreamReader.
 * 
 * @author Niall Scott
 */
public class StringFetcherStreamReaderTests extends TestCase {
    
    private StringFetcherStreamReader reader;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        reader = new StringFetcherStreamReader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        reader = null;
    }
    
    /**
     * Test that an IllegalArgumentException is thrown when the InputStream is
     * set as null.
     * 
     * @throws IOException When an IOException occurs. This should not happen
     * in this test.
     */
    public void testReadInputStreamWithNullInputStream() throws IOException {
        try {
            reader.readInputStream(null);
        } catch (IllegalArgumentException e) {
            // Expected.
            return;
        }
        
        fail("When the stream is set as null, an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that getData() returns null by default.
     */
    public void testGetDataReturnsNullByDefault() {
        assertNull(reader.getData());
    }
    
    /**
     * Test that toString() returns null by default.
     */
    public void testToStringReturnsNullByDefault() {
        assertNull(reader.toString());
    }
}