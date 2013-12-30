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

import java.io.File;
import junit.framework.TestCase;

/**
 * Tests for FileWriterFetcherStreamReader.
 * 
 * @author Niall Scott
 */
public class FileWriterFetcherStreamReaderTests extends TestCase {
    
    /**
     * Test that the constructor throws an IllegalArgumentException when given
     * a null file object.
     */
    public void testConstructorWithNullFile() {
        try {
            new FileWriterFetcherStreamReader((File) null, false);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The file was set to null, so IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that the constructor throws an IllegalArgumentException when given
     * a null filePath.
     */
    public void testConstructorWithNullFilePath() {
        try {
            new FileWriterFetcherStreamReader((String) null, false);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The filePath was set to null, so IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that the constructor throws an IllegalArgumentException when given
     * an empty filePath.
     */
    public void testConstructorWithEmptyFilePath() {
        try {
            new FileWriterFetcherStreamReader("", false);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The filePath was set to empty, so IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the values passed in to the constructor match the getter
     * methods.
     */
    public void testConstructorForFileWithAppend() {
        final File file = new File("test");
        final FileWriterFetcherStreamReader reader =
                new FileWriterFetcherStreamReader(file, true);
        
        assertEquals(file, reader.getFile());
        assertTrue(reader.doesAppend());
    }
    
    /**
     * Test that the values passed in to the constructor match the getter
     * methods.
     */
    public void testConstructorForFileWithNoAppend() {
        final File file = new File("test");
        final FileWriterFetcherStreamReader reader =
                new FileWriterFetcherStreamReader(file, false);
        
        assertEquals(file, reader.getFile());
        assertFalse(reader.doesAppend());
    }
    
    /**
     * Test that the values passed in to the constructor match the getter
     * methods.
     */
    public void testConstructorForFilePathWithAppend() {
        final File file = new File("test");
        final FileWriterFetcherStreamReader reader =
                new FileWriterFetcherStreamReader("test", true);
        
        assertEquals(file, reader.getFile());
        assertTrue(reader.doesAppend());
    }
    
    /**
     * Test that the values passed in to the constructor match the getter
     * methods.
     */
    public void testConstructorForFilePathWithNoAppend() {
        final File file = new File("test");
        final FileWriterFetcherStreamReader reader =
                new FileWriterFetcherStreamReader("test", false);
        
        assertEquals(file, reader.getFile());
        assertFalse(reader.doesAppend());
    }
}