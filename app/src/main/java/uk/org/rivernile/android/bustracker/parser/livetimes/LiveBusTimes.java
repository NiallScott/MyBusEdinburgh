/*
 * Copyright (C) 2014 - 2018 Niall 'Rivernile' Scott
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

import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * {@code LiveBusTimes} maps unique bus stop codes to {@link LiveBusStop} instances. It also
 * returns data that is global to the bus times request.
 * 
 * @author Niall Scott
 */
public class LiveBusTimes {
    
    private final Map<String, LiveBusStop> busStops;
    private final long receiveTime;
    private final boolean hasGlobalDisruption;

    /**
     * Create a new {@code LiveBusTimes} instance. This constructor is not publicly accessible. To
     * construct an instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    protected LiveBusTimes(@NonNull final Builder builder) {
        if (builder.busStops == null) {
            throw new IllegalArgumentException("The busStops must not be null.");
        }

        busStops = builder.busStops;
        receiveTime = builder.receiveTime;
        hasGlobalDisruption = builder.hasGlobalDisruption;
    }
    
    /**
     * Does this instance contain a mapping from the specified {@code stopCode}?
     * 
     * @param stopCode The bus stop code to test a mapping for.
     * @return {@code true} if this instance is aware of the given {@code stopCode}, {@code false}
     * if not.
     */
    public boolean containsBusStop(@NonNull final String stopCode) {
        return busStops.containsKey(stopCode);
    }
    
    /**
     * Get the {@link LiveBusStop} that is mapped to by {@code stopCode}.
     * 
     * @param stopCode The {@code stopCode} of the {@link LiveBusStop} to fetch.
     * @return An instance of {@link LiveBusStop} which matches the mapping given by
     * {@code stopCode}, or {@code null} if the mapping does not exist.
     */
    @Nullable
    public LiveBusStop getBusStop(@NonNull final String stopCode) {
        return busStops.get(stopCode);
    }
    
    /**
     * Get a {@link Set} of all the bus stop codes that this instance is aware of.
     * 
     * @return A {@link Set} of bus stop codes that this instance is aware of.
     */
    @NonNull
    public Set<String> getBusStops() {
        return busStops.keySet();
    }
    
    /**
     * Get the time, as per {@link android.os.SystemClock#elapsedRealtime()}, that the data was
     * received at. This may be used later to determine how old the data is. The reason that it
     * is based on {@link android.os.SystemClock#elapsedRealtime()} is because that does not
     * change if the user changes the system clock.
     * 
     * @return The number of milliseconds since system boot that the data was received and parsed
     * at.
     */
    public long getReceiveTime() {
        return receiveTime;
    }
    
    /**
     * Test whether this instance is aware of any bus stops at all.
     * 
     * @return {@code true} if this instance contains no bus stops, {@code false} if there is at
     * least 1 bus stop.
     */
    public boolean isEmpty() {
        return busStops.isEmpty();
    }
    
    /**
     * Get the number of bus stops that this instance is aware of.
     * 
     * @return The number of bus stops that this instance is aware of.
     */
    public int size() {
        return busStops.size();
    }
    
    /**
     * Is there a disruption that affects the whole network?
     * 
     * <p>
     *     Not all real-time systems will support this flag. In this case, {@code false} will be
     *     returned.
     * </p>
     * 
     * @return {@code true} if there is a disruption that affects the whole network,
     * {@code false} if not.
     */
    public boolean hasGlobalDisruption() {
        return hasGlobalDisruption;
    }

    /**
     * This {@link Builder} must be used to construct a new {@link LiveBusTimes}. Create a new
     * instance of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private Map<String, LiveBusStop> busStops;
        private long receiveTime;
        private boolean hasGlobalDisruption;

        /**
         * Set the {@link Set} of all the bus stop codes that this instance is aware of.
         *
         * @param busStops The {@link Set} of all the bus stop codes that this instance is aware of.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setBusStops(@Nullable final Map<String, LiveBusStop> busStops) {
            this.busStops = busStops;
            return this;
        }

        /**
         * Set the time the data was received at. This should be based on
         * {@link SystemClock#elapsedRealtime()}.
         *
         * @param receiveTime The time the data was received at.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setReceiveTime(final long receiveTime) {
            this.receiveTime = receiveTime;
            return this;
        }

        /**
         * Set whether there is a disruption that affects the whole network or not.
         *
         * @param hasGlobalDisruption {@code true} if there is a disruption that affects the whole
         * network, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setHasGlobalDisruption(final boolean hasGlobalDisruption) {
            this.hasGlobalDisruption = hasGlobalDisruption;
            return this;
        }

        /**
         * Build a new {@link LiveBusTimes} object.
         *
         * @return A new {@link LiveBusTimes} object.
         * @throws IllegalArgumentException When the {@link Map} of {@link LiveBusStop}s is
         * {@code null}
         */
        @NonNull
        public LiveBusTimes build() {
            return new LiveBusTimes(this);
        }
    }
}