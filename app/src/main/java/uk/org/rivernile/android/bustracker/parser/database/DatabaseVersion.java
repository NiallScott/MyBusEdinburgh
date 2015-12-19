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

package uk.org.rivernile.android.bustracker.parser.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * This model object describes a database version response from the endpoint.
 * 
 * @author Niall Scott
 */
public class DatabaseVersion {
    
    private final String schemaName;
    private final String topologyId;
    private final String url;
    private final String checksum;

    /**
     * Create a new {@code DatabaseVersion}. This constructor is not publicly accessible. To
     * construct an instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    private DatabaseVersion(@NonNull final Builder builder) {
        schemaName = builder.schemaName;
        topologyId = builder.topologyId;
        url = builder.url;
        checksum = builder.checksum;
    }

    /**
     * Get the schema name.
     * 
     * @return The schema name.
     */
    @NonNull
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Get the topology ID.
     * 
     * @return The topology ID.
     */
    @NonNull
    public String getTopologyId() {
        return topologyId;
    }

    /**
     * Get the URL to download the database file.
     * 
     * @return The URL to download the database file.
     */
    @NonNull
    public String getUrl() {
        return url;
    }

    /**
     * Get the checksum of the database file.
     * 
     * @return The checksum of the database file.
     */
    @NonNull
    public String getChecksum() {
        return checksum;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DatabaseVersion that = (DatabaseVersion) o;

        return topologyId.equals(that.topologyId) && schemaName.equals(that.schemaName);
    }

    @Override
    public int hashCode() {
        int result = schemaName.hashCode();
        result = 31 * result + topologyId.hashCode();

        return result;
    }

    /**
     * This {@link Builder} must be used to construct a new {@link DatabaseVersion}. Create a new
     * instance of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private String schemaName;
        private String topologyId;
        private String url;
        private String checksum;

        /**
         * Set the schema name.
         *
         * @param schemaName The schema name. Must not be {@code null} or empty when
         * {@link #build()} is called.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setSchemaName(@Nullable final String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        /**
         * Set the topology ID.
         *
         * @param topologyId The topology ID. Must not be {@code null} or empty when
         * {@link #build()} is called.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setTopologyId(@Nullable final String topologyId) {
            this.topologyId = topologyId;
            return this;
        }

        /**
         * Set the URL to download the database file.
         *
         * @param url The URL to download the database file. Must not be {@code null} or empty
         * when {@link #build()} is called.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setUrl(@Nullable final String url) {
            this.url = url;
            return this;
        }

        /**
         * Set the checksum of the datbase file.
         *
         * @param checksum The checksum of the database file. Must not be {@code null} or empty when
         * {@link #build()} is called.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setChecksum(@Nullable final String checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * Build a new {@link DatabaseVersion} object.
         *
         * @return A new {@link DatabaseVersion} object.
         * @throws IllegalArgumentException When any of the fields are {@code null} or empty.
         */
        @NonNull
        public DatabaseVersion build() {
            if (TextUtils.isEmpty(schemaName)) {
                throw new IllegalArgumentException("The schemaName must not be null or empty.");
            }

            if (TextUtils.isEmpty(topologyId)) {
                throw new IllegalArgumentException("The topologyId must not be null or empty.");
            }

            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException("The url must not be null or empty..");
            }

            if (TextUtils.isEmpty(checksum)) {
                throw new IllegalArgumentException("The checksum must not be null or empty.");
            }

            return new DatabaseVersion(this);
        }
    }
}