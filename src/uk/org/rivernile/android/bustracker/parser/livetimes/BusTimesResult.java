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

package uk.org.rivernile.android.bustracker.parser.livetimes;

import java.util.HashMap;

/**
 * This object is used to hold the contents of loading BusTimes by a Loader.
 * It may hold a HashMap of results, or it may hold an error code.
 * 
 * @author Niall Scott
 */
public class BusTimesResult {
    
    private final HashMap<String, BusStop> busStops;
    private final byte errorCode;
    private final long lastRefresh;
    
    /**
     * This constructor is used when there is no error loading the data and data
     * exists.
     * 
     * @param busStops A HashMap of stopCode -> BusStop object mappings.
     * @param lastRefresh The time this was loaded at.
     */
    public BusTimesResult(final HashMap<String, BusStop> busStops,
            final long lastRefresh) {
        this.busStops = busStops;
        this.lastRefresh = lastRefresh;
        errorCode = -1;
    }
    
    /**
     * This constructor is used when when an error occurs.
     * 
     * @param errorCode The code of the error.
     * @param lastRefresh The time this was loaded at.
     */
    public BusTimesResult(final byte errorCode, final long lastRefresh) {
        this.errorCode = errorCode;
        this.lastRefresh = lastRefresh;
        busStops = null;
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
     * Get the time that this data was loaded at, as the number of milliseconds
     * elapsed since the Epoch.
     * 
     * @return The time that this data was loaded at.
     */
    public long getLastRefresh() {
        return lastRefresh;
    }
    
    /**
     * Get the HashMap of stopCode -> BusStop object mappings. May be null if
     * an error occurred.
     * 
     * @return The result or null if an error occurred.
     */
    public HashMap<String, BusStop> getResult() {
        return busStops;
    }
    
    /**
     * Check to see if this result was erroneous.
     * 
     * @return True if there was an error, false if not.
     */
    public boolean hasError() {
        return errorCode >= 0;
    }
}