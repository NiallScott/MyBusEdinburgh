/*
 * Copyright (C) 2014 - 2016 Niall 'Rivernile' Scott
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.util.List;

/**
 * A {@code LiveBusStop} represents a single bus stop returned from the real-time system. It
 * holds a {@link List} of {@link LiveBusService}s which in turn hold a {@link List} of
 * {@link LiveBus}s which hold the departure times.
 * 
 * @author Niall Scott
 */
public class LiveBusStop {
    
    private final String stopCode;
    private final String stopName;
    private final List<LiveBusService> services;
    private final boolean isDisrupted;

    /**
     * Create a new {@code LiveBusStop}. This constructor is not publicly accessible. To
     * construct an instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    protected LiveBusStop(@NonNull final Builder builder) {
        if (TextUtils.isEmpty(builder.stopCode)) {
            throw new IllegalArgumentException("The stopCode must not be null or empty.");
        }

        if (builder.services == null) {
            throw new IllegalArgumentException("The List of services must not be null.");
        }

        stopCode = builder.stopCode;
        stopName = builder.stopName;
        services = builder.services;
        isDisrupted = builder.isDisrupted;
    }
    
    /**
     * Get the unique code of this bus stop.
     * 
     * @return The unique code of this bus stop.
     */
    @NonNull
    public String getStopCode() {
        return stopCode;
    }
    
    /**
     * Get the name of this bus stop.
     * 
     * @return The name of this bus stop.
     */
    @Nullable
    public String getStopName() {
        return stopName;
    }
    
    /**
     * Get the {@link List} of services that have departures at this bus stop.
     * 
     * @return The {@link List} of services that have departures at this bus stop.
     */
    @NonNull
    public List<LiveBusService> getServices() {
        return services;
    }
    
    /**
     * Is the bus stop disrupted?
     * 
     * <p>
     *     Not all real-time systems will support this flag. In this case, {@code false} will be
     *     returned.
     * </p>
     * 
     * @return {@code true} if there is a disruption in place for this bus stop, {@code false} if
     * not.
     */
    public boolean isDisrupted() {
        return isDisrupted;
    }

    /**
     * This {@link Builder} must be used to construct a new {@link LiveBusStop}. Create a new
     * instance of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private String stopCode;
        private String stopName;
        private List<LiveBusService> services;
        private boolean isDisrupted;

        /**
         * Set the unique code of this bus stop.
         *
         * @param stopCode The unique code of this bus stop.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setStopCode(@Nullable final String stopCode) {
            this.stopCode = stopCode;
            return this;
        }

        /**
         * Set the name of this bus stop.
         *
         * @param stopName The name of this bus stop.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setStopName(@Nullable final String stopName) {
            this.stopName = stopName;
            return this;
        }

        /**
         * Set the {@link List} of services that have departures at this bus stop.
         *
         * @param services The {@link List} of services that have departures at this bus stop.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setServices(@Nullable final List<LiveBusService> services) {
            this.services = services;
            return this;
        }

        /**
         * Set whether this bus stop is disrupted or not.
         *
         * @param isDisrupted {@code true} if this bus stop is disrupted, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsDisrupted(final boolean isDisrupted) {
            this.isDisrupted = isDisrupted;
            return this;
        }

        /**
         * Build a new {@link LiveBusStop} object.
         *
         * @return A new {@link LiveBusStop} object.
         * @throws IllegalArgumentException When the {@link List} of {@link LiveBusService}s is
         * {@code null}, or the stop code is {@code null} or empty.
         */
        @NonNull
        public LiveBusStop build() {
            return new LiveBusStop(this);
        }
    }
}