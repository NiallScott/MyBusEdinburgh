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

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests for FileFetcher.
 * 
 * @author Niall Scott
 */
public class FileFetcherTests extends TestCase {
    
    /**
     * Test that an IllegalArgumentException is thrown if the filePath is set to
     * null.
     */
    public void testConstructorWithNullFilePath() {
        try {
            new FileFetcher((String) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The filePath was set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that an IllegalArgumentException is thrown if the filePath is set to
     * empty.
     */
    public void testConstructorWithEmptyFilePath() {
        try {
            new FileFetcher("");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The filePath was set as empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that an IllegalArgumentException is thrown if the file is set to
     * null.
     */
    public void testConstructorWithNullFile() {
        try {
            new FileFetcher((File) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The file was set as null, so an IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that the file instance that is returned by the getter is correct
     * after passing the filePath in the constructor.
     */
    public void testConstructorWithValidFilePath() {
        final File file = new File("test");
        final FileFetcher fetcher = new FileFetcher("test");
        
        assertEquals(file, fetcher.getFile());
    }
    
    /**
     * Test that the file instance this is returned by the getter is correct
     * after passing the file in the constructor.
     */
    public void testConstructorWithValidFile() {
        final File file = new File("test");
        final FileFetcher fetcher = new FileFetcher(file);
        
        assertEquals(file, fetcher.getFile());
    }
    
    /**
     * Test that an IllegalArgumentException is thrown when the reader is set to
     * null when executing the fetcher.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testExecuteWithNullReader() throws IOException {
        final FileFetcher fetcher = new FileFetcher("test");
        
        try {
            fetcher.executeFetcher(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The reader was set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
}