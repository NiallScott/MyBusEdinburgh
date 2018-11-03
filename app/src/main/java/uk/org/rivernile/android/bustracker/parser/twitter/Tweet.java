/*
 * Copyright (C) 2012 - 2018 Niall 'Rivernile' Scott
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Date;

/**
 * This is a model class to hold data for a tweet.
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
     * Create a new {@code Tweet}. This constructor is not publicly accessible. To construct an
     * instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    private Tweet(@NonNull final Builder builder) {
        if (TextUtils.isEmpty(builder.body)) {
            throw new IllegalArgumentException("The body must not be null or "
                    + "empty.");
        }

        if (TextUtils.isEmpty(builder.displayName)) {
            throw new IllegalArgumentException("The username must not be null "
                    + "or empty.");
        }

        if (builder.time == null) {
            throw new IllegalArgumentException("The time must not be null.");
        }

        body = builder.body;
        displayName = builder.displayName;
        time = builder.time;
        profileImageUrl = builder.profileImageUrl;
        profileUrl = builder.profileUrl;
    }
    
    /**
     * Get the body of the tweet.
     * 
     * @return The body of the tweet.
     */
    @NonNull
    public String getBody() {
        return body;
    }
    
    /**
     * Get the display name of the account which posted this tweet.
     * 
     * @return The display name of the account which posted this tweet.
     */
    @NonNull
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the date and time that this tweet was posted at.
     * 
     * @return The date and time that this tweet was posted at.
     */
    @NonNull
    public Date getTime() {
        return time;
    }
    
    /**
     * Get the URL which points to the profile image of the account which posted the tweet.
     * 
     * @return The URL which points to the profile image of the account which posted the tweet.
     */
    @Nullable
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    /**
     * Get the URL which points to the profile of the account which posted the tweet.
     * 
     * @return The the URL which points to the profile of the account which posted the tweet.
     */
    @Nullable
    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public int compareTo(@NonNull final Tweet another) {
        return another.time.compareTo(time);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Tweet that = (Tweet) o;

        return body.equals(that.body) && displayName.equals(that.displayName) &&
                time.equals(that.time);
    }

    @Override
    public int hashCode() {
        int result = body.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + time.hashCode();

        return result;
    }

    /**
     * This {@link Builder} must be used to construct a new {@link Tweet}. Create a new instance
     * of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private String body;
        private String displayName;
        private Date time;
        private String profileImageUrl;
        private String profileUrl;

        /**
         * Set the body of the tweet.
         *
         * @param body The body of the tweet. Must not be {@code null} or empty when
         * {@link #build()} is called.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setBody(@Nullable final String body) {
            this.body = body;
            return this;
        }

        /**
         * Set the display name of the account which posted this tweet.
         *
         * @param displayName The display name of the account which posted this tweet. Must not be
         * {@code null} or empty when {@link #build()} is called.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setDisplayName(@Nullable final String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Set the date and time the tweet was posted at.
         *
         * @param time The date and time the tweet was posted at. Must not be {@code null} when
         * {@link #build()} is called.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setTime(@Nullable final Date time) {
            this.time = time;
            return this;
        }

        /**
         * Set the URL which points to the profile image of the account which posted the tweet.
         *
         * @param profileImageUrl The URL which points to the profile image of the account which
         * posted the tweet.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setProfileImageUrl(@Nullable final String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        /**
         * Set the URL which points to the profile of the account which posted the tweet.
         *
         * @param profileUrl The URL which points to the profile of the account which posted the
         * tweet.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setProfileUrl(@Nullable final String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        /**
         * Build a new {@link Tweet} object.
         *
         * @return A new {@link Tweet} object.
         * @throws IllegalArgumentException When {@code time} is null, or {@code body} or
         * {@code displayName} are {@code null} or empty.
         */
        @NonNull
        public Tweet build() {
            return new Tweet(this);
        }
    }
}