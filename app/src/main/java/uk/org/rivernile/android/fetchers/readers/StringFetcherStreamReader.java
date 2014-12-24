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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import uk.org.rivernile.android.fetchers.FetcherStreamReader;

/**
 * The StringFetcherStreamReader takes an InputStream and produces a String
 * version of this data.
 * 
 * @author Niall Scott
 */
public class StringFetcherStreamReader implements FetcherStreamReader {
    
    private String data;

    /**
     * {@inheritDoc}
     */
    @Override
    public void readInputStream(final InputStream stream) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("The stream must not be null.");
        }
        
        final StringBuilder sb = new StringBuilder();
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream));
        final char[] buf = new char[1024];
        int len;
        
        // Use an array buffer rather than reading in to String, otherwise we
        // create lots of String objects that need garbage collected.
        while ((len = reader.read(buf, 0, 1024)) != -1) {
            sb.append(buf, 0, len);
        }
        
        data = sb.toString();
    }
    
    /**
     * Get the data that was in the InputStream as a String. This may be null if
     * the stream has yet to be read from, or if there was an error.
     * 
     * @return The data from the stream as a String, or null if it has not yet
     * been read from or there was an error.
     */
    public String getData() {
        return data;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getData();
    }
}