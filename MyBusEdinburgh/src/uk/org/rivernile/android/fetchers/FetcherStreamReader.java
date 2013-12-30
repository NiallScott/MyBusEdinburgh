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

import java.io.IOException;
import java.io.InputStream;

/**
 * The FetcherSteamReader is an interface that should be implemented by classes
 * capturing a stream from I/O, such as networking or files.
 * 
 * @author Niall Scott
 */
public interface FetcherStreamReader {
    
    /**
     * This method is called when an InputStream is available to read from.
     * Do not close the stream inside this method, this will be done inside the
     * Fetcher classes. Simply use this class to get bytes from the stream.
     * 
     * @param stream The InputStream.
     * @throws IOException When an IOException occurs.
     */
    public void readInputStream(InputStream stream) throws IOException;
}