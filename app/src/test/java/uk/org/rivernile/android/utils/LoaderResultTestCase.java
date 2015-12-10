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

package uk.org.rivernile.android.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link LoaderResult}.
 * 
 * @author Niall Scott
 */
public class LoaderResultTestCase {
    
    /**
     * Tests that the success constructor accepts a {@code null} result and all other fields
     * return correct values.
     */
    @Test
    public void testSuccessConstructorWithNullResult() {
        final long time = System.currentTimeMillis();
        final LoaderResult<String, Exception> result = new LoaderResult<>((String) null, time);
        assertNull(result.getResult());
        assertNull(result.getException());
        assertEquals(time, result.getLoadTime());
        assertFalse(result.hasException());
    }
    
    /**
     * Tests that the success constructor correctly returns the data it was given.
     */
    @Test
    public void testSuccessConstructorWithNonNullResult() {
        final long time = System.currentTimeMillis();
        final LoaderResult<String, Exception> result = new LoaderResult<>("test", time);
        assertNotNull(result.getResult());
        assertEquals("test", result.getResult());
        assertNull(result.getException());
        assertEquals(time, result.getLoadTime());
        assertFalse(result.hasException());
    }
    
    /**
     * Test that the {@link Exception} constructor correctly throws an
     * {@link IllegalArgumentException} if the {@link Exception} is set as {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionConstructorWithNullException() {
        new LoaderResult<>((Exception) null, System.currentTimeMillis());
    }
    
    /**
     * Test that the {@link Exception} constructor correctly returns the {@link Exception} it was
     * given and other fields return correct data.
     */
    @Test
    public void testExceptionConstructorWithNonNullException() {
        final long time = System.currentTimeMillis();
        final Exception e = new IllegalStateException("Test exception");
        final LoaderResult<String, Exception> result = new LoaderResult<>(e, time);
        assertNull(result.getResult());
        assertNotNull(result.getException());
        assertSame(e, result.getException());
        assertEquals(time, result.getLoadTime());
        assertTrue(result.hasException());
    }
}