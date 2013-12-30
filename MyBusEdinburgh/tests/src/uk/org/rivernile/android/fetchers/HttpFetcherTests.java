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

package uk.org.rivernile.android.fetchers;

import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests for HttpFetcher.
 * 
 * @author Niall Scott
 */
public class HttpFetcherTests extends TestCase {
    
    /**
     * Test that the constructor throws an IllegalArgumentException when the url
     * is set to null.
     */
    public void testConstructorWithNullUrl() {
        try {
            new HttpFetcher(null, false);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The url was set as null, so an IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that the constructor throws an IllegalArgumentException when the url
     * is set to empty.
     */
    public void testConstructorWithEmptyUrl() {
        try {
            new HttpFetcher("", false);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The url was set as empty, so an IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test the getters return correct data after passing valid arguments to the
     * constructor.
     */
    public void testValidConstructor() {
        final HttpFetcher fetcher = new HttpFetcher("http://example.com/test",
                true);
        assertEquals("http://example.com/test", fetcher.getUrl());
        assertTrue(fetcher.getAllowRedirects());
    }
    
    /**
     * Test that passing a null reader when executing the fetcher throws an
     * IllegalArgumentException.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testNullReader() throws IOException {
        final HttpFetcher fetcher = new HttpFetcher("http://example.com/test",
                true);
        
        try {
            fetcher.executeFetcher(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The reader was set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
}