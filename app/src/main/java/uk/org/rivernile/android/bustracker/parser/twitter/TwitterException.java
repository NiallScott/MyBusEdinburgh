/*
 * Copyright (C) 2014 - 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.parser.twitter;

/**
 * {@code TwitterException} is used when there are problems during the fetching or parsing of
 * Twitter data.
 *
 * @author Niall Scott
 */
public class TwitterException extends Exception {
    
    /**
     * Create a new {@code TwitterException}.
     */
    public TwitterException() {
        super();
    }
    
    /**
     * Create a new {@code TwitterException}.
     * 
     * @param detailMessage The message to include in the {@link Exception}.
     */
    public TwitterException(final String detailMessage) {
        super(detailMessage);
    }
    
    /**
     * Create a new {@code TwitterException}.
     * 
     * @param detailMessage The message to include in the {@link Exception}.
     * @param throwable The Throwable that caused this {@link Exception}.
     */
    public TwitterException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }
    
    /**
     * Create a new {@code TwitterException}.
     * 
     * @param throwable The Throwable that caused this {@link Exception}.
     */
    public TwitterException(final Throwable throwable) {
        super(throwable);
    }
}