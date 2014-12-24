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

package uk.org.rivernile.android.utils;

import junit.framework.TestCase;

/**
 * Tests for {@link LoaderResult}.
 * 
 * @author Niall Scott
 */
public class LoaderResultTestCase extends TestCase {
    
    /**
     * Tests that the success constructor accepts a null result and all other
     * fields return correct values.
     */
    public void testSuccessConstructorWithNullResult() {
        final long time = System.currentTimeMillis();
        final LoaderResult<String, Exception> result =
                new LoaderResult<String, Exception>((String) null, time);
        assertNull(result.getResult());
        assertNull(result.getException());
        assertEquals(time, result.getLoadTime());
        assertFalse(result.hasException());
    }
    
    /**
     * Tests that the success constructor correctly returns the data it was
     * given.
     */
    public void testSuccessConstructorWithNonNullResult() {
        final long time = System.currentTimeMillis();
        final LoaderResult<String, Exception> result =
                new LoaderResult<String, Exception>("test", time);
        assertNotNull(result.getResult());
        assertEquals("test", result.getResult());
        assertNull(result.getException());
        assertEquals(time, result.getLoadTime());
        assertFalse(result.hasException());
    }
    
    /**
     * Test that the Exception constructor correctly throws an 
     * IllegalArgumentException if the Exception is set as null.
     */
    public void testExceptionConstructorWithNullException() {
        try {
            final LoaderResult<String, Exception> result =
                new LoaderResult<String, Exception>((Exception) null,
                        System.currentTimeMillis());
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("A null Exception must cause an IllegalArgumentException to be "
                + "thrown.");
    }
    
    /**
     * Test that the Exception constructor correctly returns the Exception it
     * was given and other fields return correct data.
     */
    public void testExceptionConstructorWithNonNullException() {
        final long time = System.currentTimeMillis();
        final Exception e = new IllegalStateException("Test exception");
        final LoaderResult<String, Exception> result =
                new LoaderResult<String, Exception>(e, time);
        assertNull(result.getResult());
        assertNotNull(result.getException());
        assertSame(e, result.getException());
        assertEquals(time, result.getLoadTime());
        assertTrue(result.hasException());
    }
}