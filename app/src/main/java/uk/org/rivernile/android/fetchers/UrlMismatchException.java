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

/**
 * This Exception is thrown when the file that is fetched from the server does
 * not have the same URL that was requested from the server (i.e. there was a
 * redirect).
 * 
 * @author Niall Scott
 */
public class UrlMismatchException extends IOException {
    
    /**
     * Constructs a new UrlMismatchException with the default message filled in.
     */
    public UrlMismatchException() {
        super("The URL that was requested does not match the URL that was "
                + "returned.");
    }
    
    /**
     * Constructs a new UrlMismatchException, specifying the message.
     * 
     * @param detailMessage The Exception message.
     */
    public UrlMismatchException(final String detailMessage) {
        super(detailMessage);
    }
}