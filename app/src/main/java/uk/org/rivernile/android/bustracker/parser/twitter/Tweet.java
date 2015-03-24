/*
 * Copyright (C) 2012 - 2015 Niall 'Rivernile' Scott
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

import android.text.TextUtils;
import java.util.Date;

/**
 * This class is a bean class to hold data for a Tweet.
 * 
 * @author Niall Scott
 */
public final class Tweet implements Comparable<Tweet> {
    
    private final String body;
    private final String displayName;
    private final Date time;
    private final String profileImageUrl;
    private final String profileUrl;
    
    /**
     * Create a new Twitter news item.
     * 
     * @param body The tweet text. Must not be {@code null} or empty.
     * @param displayName The display name of the tweet author. Must not be {@code null} or empty.
     * @param time A {@link Date} object representing the moment the tweet was posted. Must not be
     *             {@code null}.
     * @param profileImageUrl The URL of the profile image of the tweet's author. Can be
     *                        {@code null}.
     * @param profileUrl The URL of the profile of the tweet's author. Can be {@code null}.
     */
    public Tweet(final String body, final String displayName, final Date time,
                 final String profileImageUrl, final String profileUrl) {
        if (TextUtils.isEmpty(body)) {
            throw new IllegalArgumentException("The body must not be null or "
                    + "empty.");
        }
        
        if (TextUtils.isEmpty(displayName)) {
            throw new IllegalArgumentException("The username must not be null "
                    + "or empty.");
        }
        
        if (time == null) {
            throw new IllegalArgumentException("The time must not be null.");
        }
        
        this.body = body;
        this.displayName = displayName;
        this.time = time;
        this.profileImageUrl = profileImageUrl;
        this.profileUrl = profileUrl;
    }
    
    /**
     * Get the tweet text.
     * 
     * @return The tweet text.
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Get the display name of the account which posted this tweet.
     * 
     * @return The display name of the account which posted this tweet.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the date and time that this tweet was posted at.
     * 
     * @return The date and time that this tweet was posted at.
     */
    public Date getTime() {
        return time;
    }
    
    /**
     * Get the URL of the profile image of the tweet's author.
     * 
     * @return The URL of the profile image of the tweet's author. May be {@code null}.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    /**
     * Get the URL of the profile of the tweet's author.
     * 
     * @return The URL of the profile of the tweet's author. May be {@code null}.
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public int compareTo(final Tweet another) {
        return another != null ? another.time.compareTo(time) : -1;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Tweet tweet = (Tweet) o;

        if (!body.equals(tweet.body)) {
            return false;
        }

        if (!displayName.equals(tweet.displayName)) {
            return false;
        }

        if (!time.equals(tweet.time)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = body.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + time.hashCode();
        return result;
    }
}