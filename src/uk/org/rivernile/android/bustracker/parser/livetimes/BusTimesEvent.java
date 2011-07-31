/*
 * Copyright (C) 2011 Niall 'Rivernile' Scott
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
 * Implement this interface if you wish to register for callbacks based on bus
 * time requests.
 * 
 * @author Niall Scott
 */
public interface BusTimesEvent {
    
    /**
     * This callback is called before the actual request happens.
     */
    public void onPreExecute();
    
    /**
     * This callback is called when an error occurs.
     * 
     * @param errorCode The internal code of the error.
     */
    public void onBusTimesError(int errorCode);
    
    /**
     * This callback is called when the bus times have been received and parsed
     * from the server and are now ready.
     * 
     * @param result The bus times, as a HashMap.
     */
    public void onBusTimesReady(HashMap<String, BusStop> result);
    
    /**
     * This callback is called when the request for bus times has been
     * cancelled.
     */
    public void onCancel();
}