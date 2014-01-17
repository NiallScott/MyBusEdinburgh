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

import android.test.InstrumentationTestCase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import uk.org.rivernile.android.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchers.readers.FileWriterFetcherStreamReader;

/**
 * Tests for FileUtils.
 * 
 * @author Niall Scott
 */
public class FileUtilsTests extends InstrumentationTestCase {
    
    private static final String EXPECTED_HASH =
            "bae7aa5c017ead84fe44197bea819d63";
    
    /**
     * Test that {@link FileUtils#md5Checksum(java.io.File)} correctly throws
     * an IllegalArgumentException if the file is set to null.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testMd5ChecksumWithNullFile() throws IOException {
        try {
            FileUtils.md5Checksum(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The file is set to null, so IllegalArgumentException should be "
                + "thrown.");
    }
    
    /**
     * Test that {@link FileUtils#md5Checksum(java.io.File)} correctly throws a
     * FileNotFoundException if the File object points towards a file that does
     * not exist.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testMd5ChecksumWithInvalidFilename() throws IOException {
        try {
            FileUtils.md5Checksum(new File("invalid"));
        } catch (FileNotFoundException e) {
            return;
        }
        
        fail("The file is set to an invalid file, so FileNotFoundException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link FileUtils#md5Checksum(java.io.File)} correctly returns
     * a hash if a valid file is given to it.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testMd5ChecksumWithValidFile() throws IOException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/redsquare.png");
        final File outFile =
                new File(getInstrumentation().getTargetContext().getFilesDir(),
                        "redsquare.png");
        final FileWriterFetcherStreamReader reader =
                new FileWriterFetcherStreamReader(outFile, false);
        
        fetcher.executeFetcher(reader);
        assertEquals(EXPECTED_HASH, FileUtils.md5Checksum(outFile));
        
        outFile.delete();
    }
}