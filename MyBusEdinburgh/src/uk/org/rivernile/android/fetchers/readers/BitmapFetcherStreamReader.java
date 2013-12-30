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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import uk.org.rivernile.android.fetchers.FetcherStreamReader;

/**
 * The BitmapFetcherStreamReader takes an InputStream and produces a Bitmap.
 * {@link BitmapFetcherStreamReader#getBitmap()} will return the Bitmap if it
 * is available. This implementation uses the Bitmap.decodeStream(InputStream)
 * method found inside the Android framework.
 * 
 * @author Niall Scott
 */
public class BitmapFetcherStreamReader implements FetcherStreamReader {
    
    private Bitmap bitmap;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void readInputStream(final InputStream stream) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("The stream must not be null.");
        }
        
        bitmap = BitmapFactory.decodeStream(stream);
    }
    
    /**
     * Get the Bitmap that was returned from the stream. May be null if no data
     * has been fed in to this class yet, or null if there was an error fetching
     * or parsing the data.
     * 
     * @return An instance of Bitmap which contains the image data, or null if
     * the data has yet to be fetched, or null if there was an error.
     */
    public Bitmap getBitmap() {
        return bitmap;
    }
}