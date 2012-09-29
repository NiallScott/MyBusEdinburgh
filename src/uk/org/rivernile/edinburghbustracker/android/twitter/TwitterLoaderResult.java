/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.twitter;

import java.util.ArrayList;

/**
 * This object is used to store the result of loading updates from Twitter. It
 * may hold a collection of news items, or it may contain an error code.
 * 
 * @author Niall Scott
 */
public class TwitterLoaderResult {
    
    private final ArrayList<TwitterNewsItem> resultList;
    private final byte errorCode;
    
    /**
     * Create a new TwitterLoaderResult in the case of success.
     * 
     * @param results The ArrayList of TwitterNewsItems.
     * @see #TwitterLoaderResult(byte) 
     */
    public TwitterLoaderResult(final ArrayList<TwitterNewsItem> results) {
        resultList = results;
        errorCode = -1;
    }
    
    /**
     * Create a new TwitterLoaderResult in the case of failure.
     * 
     * @param errorCode The error code which describes the problem.
     * @see #TwitterLoaderResult(java.util.ArrayList) 
     */
    public TwitterLoaderResult(final byte errorCode) {
        this.errorCode = errorCode;
        resultList = null;
    }
    
    /**
     * Get the error code. A value of -1 denotes that there is no error.
     * 
     * @return The error code or -1 if no error.
     * @see #hasError() 
     */
    public byte getError() {
        return errorCode;
    }
    
    /**
     * Get the ArrayList of TwitterNewsItems. May be null if there was an error.
     * 
     * @return The ArrayList of TwitterNewsItems, or null if there was an error.
     */
    public ArrayList<TwitterNewsItem> getResult() {
        return resultList;
    }
    
    /**
     * Check to see if this result was erroneous.
     * 
     * @return True if there was an error, false if not.
     * @see #getError() 
     */
    public boolean hasError() {
        return errorCode >= 0;
    }
}