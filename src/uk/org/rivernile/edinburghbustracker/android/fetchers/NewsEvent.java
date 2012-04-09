/*
 * Copyright (C) 2011 - 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fetchers;

import java.util.ArrayList;

/**
 * This interface contains callbacks for the news task.
 * 
 * @author Niall Scott
 */
public interface NewsEvent {
    
    /**
     * This is executed before the main task is carried out.
     */
    public void onPreExecute();
    
    /**
     * When the news has been downloaded, this callback is called to announce
     * the data is ready.
     * 
     * @param newsItems The list of news items.
     */
    public void onNewsAvailable(ArrayList<TwitterNewsItem> newsItems);
    
    /**
     * This is called when an error occurs.
     * 
     * @param errorCode The local code for the error.
     */
    public void onFetchError(int errorCode);
}