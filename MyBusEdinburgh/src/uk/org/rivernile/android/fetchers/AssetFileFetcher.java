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

import android.content.Context;
import android.text.TextUtils;
import java.io.IOException;
import java.io.InputStream;

/**
 * An AssetFileFetcher fetches data from a given filename in the application
 * assets. The data is then given in to an instance of a FetcherStreamReader.
 * This class takes care of opening and closing the file.
 * 
 * @author Niall Scott
 */
public class AssetFileFetcher implements Fetcher {
    
    private final Context context;
    private final String filename;

    /**
     * Create a new AssetFileFetcher.
     * 
     * @param context A Context instance. Cannot be null.
     * @param filename The name of the file to load. Cannot be null or empty.
     */
    public AssetFileFetcher(final Context context, final String filename) {
        if (context == null) {
            throw new IllegalArgumentException("context should not be null.");
        }
        
        if (TextUtils.isEmpty(filename)) {
            throw new IllegalArgumentException("The filename must not be " +
                    "null or empty.");
        }
        
        this.context = context;
        this.filename = filename;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeFetcher(final FetcherStreamReader reader)
            throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("The reader cannot be null.");
        }
        
        InputStream in = null;
        
        try {
            in = context.getAssets().open(filename);
            reader.readInputStream(in);
        } catch (IOException e) {
            // Re-throw the Exception, so that the 'finally' clause is
            // executed.
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Nothing to do here.
                }
            }
        }
    }
    
    /**
     * Get the name of the file that this instance references in the application
     * assets.
     * 
     * @return The name of the file that this instance uses.
     */
    public String getFilename() {
        return filename;
    }
}