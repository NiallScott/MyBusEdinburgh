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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.FileWriterFetcherStreamReader;

/**
 * Tests for {@link FileUtils}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class FileUtilsTests {
    
    private static final String EXPECTED_HASH = "bae7aa5c017ead84fe44197bea819d63";
    
    /**
     * Test that {@link FileUtils#md5Checksum(java.io.File)} correctly throws a
     * {@link FileNotFoundException} if the {@link File} object points towards a file that does
     * not exist.
     *
     * @throws IOException This test is not expected to throw an {@link IOException}, so if it is
     * thrown, let the test fail.
     */
    @Test(expected = FileNotFoundException.class)
    public void testMd5ChecksumWithInvalidFilename() throws IOException {
        FileUtils.md5Checksum(new File("invalid"));
    }
    
    /**
     * Test that {@link FileUtils#md5Checksum(java.io.File)} correctly returns a hash if a valid
     * file is given to it.
     * 
     * @throws IOException This test is not expected to throw an {@link IOException}, so if it is
     * thrown, let the test fail.
     */
    @Test
    public void testMd5ChecksumWithValidFile() throws IOException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "fetchers/redsquare.png");
        final File outFile = new File(InstrumentationRegistry.getTargetContext().getFilesDir(),
                "redsquare.png");
        final FileWriterFetcherStreamReader reader = new FileWriterFetcherStreamReader(outFile,
                false);
        
        fetcher.executeFetcher(reader);
        assertEquals(EXPECTED_HASH, FileUtils.md5Checksum(outFile));
        
        outFile.delete();
    }
}