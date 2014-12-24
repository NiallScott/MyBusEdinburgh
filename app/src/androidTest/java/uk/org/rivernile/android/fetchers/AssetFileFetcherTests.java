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

import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;
import java.io.File;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.fetchers.readers.BitmapFetcherStreamReader;
import uk.org.rivernile.android.fetchers.readers.FileWriterFetcherStreamReader;
import uk.org.rivernile.android.fetchers.readers.JSONFetcherStreamReader;
import uk.org.rivernile.android.fetchers.readers.StringFetcherStreamReader;

/**
 * Tests for AssetFileFetcher. This TestCase is also used to test the execution
 * of the FetcherStreamReader classes as storing test data in the assets is
 * easier.
 * 
 * @author Niall Scott
 */
public class AssetFileFetcherTests extends InstrumentationTestCase {
    
    /**
     * Test that an IllegalArgumentException is thrown when the context is set
     * to null.
     */
    public void testConstructorWithNullContext() {
        try {
            new AssetFileFetcher(null, "test");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The context is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that an IllegalArgumentException is thrown when the filename is set
     * to null.
     */
    public void testConstructorWithNullFilename() {
        try {
            new AssetFileFetcher(getInstrumentation().getContext(), null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The context is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that an IllegalArgumentException is thrown when the filename is set
     * to blank.
     */
    public void testConstructorWithEmptyFilename() {
        try {
            new AssetFileFetcher(getInstrumentation().getContext(), "");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The context is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the filename that is passed in the constructor matches what is
     * returned in the getter.
     */
    public void testGetFilename() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(), "test");
        assertEquals("test", fetcher.getFilename());
    }
    
    /**
     * Test that an IllegalArgumentException is thrown when the reader is set
     * to null.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testNullReader() throws IOException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(), "test");
        
        try {
            fetcher.executeFetcher(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The reader was set as null, so IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that an IOException is thrown when an invalid filename is passed
     * to the AssetFileFetcher and is executed.
     */
    public void testInvalidFilename() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "invalid");
        
        try {
            fetcher.executeFetcher(new StringFetcherStreamReader());
        } catch (IOException e) {
            return;
        }
        
        fail("An invalid filename was set so an IOException should be thrown.");
    }
    
    /**
     * Test that a Bitmap can be successfully read.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testBitmap() throws IOException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/redsquare.png");
        final BitmapFetcherStreamReader reader =
                new BitmapFetcherStreamReader();
        
        fetcher.executeFetcher(reader);
        
        final Bitmap bitmap = reader.getBitmap();
        assertNotNull(bitmap);
        assertEquals(6, bitmap.getWidth());
        assertEquals(4, bitmap.getHeight());
    }
    
    /**
     * Test that no Bitmap is set when an invalid/corrupt Bitmap file is read
     * from.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testInvalidBitmap() throws IOException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/not_an_image.png");
        final BitmapFetcherStreamReader reader =
                new BitmapFetcherStreamReader();
        
        fetcher.executeFetcher(reader);
        
        assertNull(reader.getBitmap());
    }
    
    /**
     * Test that data can be passed from an AssetFileFetcher to a
     * FileWriterFetcherStreamReader and read the data back in to make sure it
     * is correct. This makes sure that data is overwritten.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testFileWriterWithoutAppend() throws IOException {
        final AssetFileFetcher assetFetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/example.txt");
        final File outFile =
                new File(getInstrumentation().getTargetContext().getFilesDir(),
                        "out.txt");
        final FileWriterFetcherStreamReader assetReader =
                new FileWriterFetcherStreamReader(outFile, false);
        
        // Executed twice to make sure the data is not appended.
        assetFetcher.executeFetcher(assetReader);
        assetFetcher.executeFetcher(assetReader);
        
        final FileFetcher fileFetcher = new FileFetcher(outFile);
        final StringFetcherStreamReader fileReader =
                new StringFetcherStreamReader();
        fileFetcher.executeFetcher(fileReader);
        
        assertEquals("This is example text.", fileReader.toString().trim());
        outFile.delete();
    }
    
    /**
     * Test that data can be passed from an AssetFileFetcher to a
     * FileWriterFetcherStreamReader and read the data back in to make sure it
     * is correct. This makes sure that data is appended.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testFileWriterWithAppend() throws IOException {
        final AssetFileFetcher assetFetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/example.txt");
        final File outFile =
                new File(getInstrumentation().getTargetContext().getFilesDir(),
                        "out.txt");
        final FileWriterFetcherStreamReader assetReader =
                new FileWriterFetcherStreamReader(outFile, true);
        
        // Executed twice to make sure the data is appended.
        assetFetcher.executeFetcher(assetReader);
        assetFetcher.executeFetcher(assetReader);
        
        final FileFetcher fileFetcher = new FileFetcher(outFile);
        final StringFetcherStreamReader fileReader =
                new StringFetcherStreamReader();
        fileFetcher.executeFetcher(fileReader);
        
        assertEquals("This is example text.\nThis is example text.",
                fileReader.toString().trim());
        outFile.delete();
    }
    
    /**
     * Test that a file containing String data can be read from.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     */
    public void testStringReader() throws IOException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/example.txt");
        final StringFetcherStreamReader reader =
                new StringFetcherStreamReader();
        fetcher.executeFetcher(reader);
        
        assertEquals("This is example text.", reader.toString().trim());
    }
    
    /**
     * Test that a JSONObject can be read from.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     * @throws JSONException This test is not expected to throw a JSONException,
     * so if it is thrown, let the TestCase cause a failure.
     */
    public void testJSONObject() throws IOException, JSONException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/example_object.json");
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        fetcher.executeFetcher(reader);
        
        final JSONObject jo = reader.getJSONObject();
        assertEquals("A JSON String.", jo.getString("example"));
    }
    
    /**
     * Test that a JSONObject can be read from.
     * 
     * @throws IOException This test is not expected to throw an IOException, so
     * if it is thrown, let the TestCase cause a failure.
     * @throws JSONException This test is not expected to throw a JSONException,
     * so if it is thrown, let the TestCase cause a failure.
     */
    public void testJSONArray() throws IOException, JSONException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "fetchers/example_array.json");
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        fetcher.executeFetcher(reader);
        
        final JSONArray ja = reader.getJSONArray();
        assertEquals("One", ja.getString(0));
        assertEquals("Two", ja.getString(1));
        assertEquals("Three", ja.getString(2));
        assertEquals("Four", ja.getString(3));
    }
}