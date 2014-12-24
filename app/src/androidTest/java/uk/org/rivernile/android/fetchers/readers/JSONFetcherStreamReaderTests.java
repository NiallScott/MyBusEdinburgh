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

import junit.framework.TestCase;
import org.json.JSONException;

/**
 * Tests for JSONFetcherStreamReader.
 * 
 * @author Niall Scott
 */
public class JSONFetcherStreamReaderTests extends TestCase {
    
    private JSONFetcherStreamReader reader;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        reader = new JSONFetcherStreamReader();
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
     * Test that a JSONException is thrown when the data is null.
     */
    public void testGetJSONObjectWithNullData() {
        try {
            reader.getJSONObject();
        } catch (JSONException e) {
            return;
        }
        
        fail("The data is null, so attempting to get a JSONObject should "
                + "yeild a JSONException.");
    }
    
    /**
     * Test that a JSONException is thrown when the data is null.
     */
    public void testGetJSONArrayWithNullData() {
        try {
            reader.getJSONArray();
        } catch (JSONException e) {
            return;
        }
        
        fail("The data is null, so attempting to get a JSONArray should "
                + "yeild a JSONException.");
    }
}