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
import java.util.Date;

/**
 * A {@code JourneyDeparture} represents a departure point on a {@link Journey}. A {@link Journey}
 * is a list of points that a service calls at.
 * 
 * @author Niall Scott
 */
public class JourneyDeparture implements Comparable<JourneyDeparture> {

    private final String stopCode;
    private final String stopName;
    private final Date departureTime;
    private final boolean isBusStopDisrupted;
    private final boolean isEstimatedTime;
    private final boolean isDelayed;
    private final boolean isDiverted;
    private final boolean isTerminus;
    private final boolean isPartRoute;
    private final int order;

    /**
     * Create a new {@code JourneyDeparture}. This constructor is not publicly accessible. To
     * construct an instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    protected JourneyDeparture(@NonNull final Builder builder) {
        if (TextUtils.isEmpty(builder.stopCode)) {
            throw new IllegalArgumentException("The stopCode must not be null or empty.");
        }

        if (builder.departureTime == null) {
            throw new IllegalArgumentException("The departureTime must not be null.");
        }

        stopCode = builder.stopCode;
        stopName = builder.stopName;
        departureTime = builder.departureTime;
        isBusStopDisrupted = builder.isBusStopDisrupted;
        isEstimatedTime = builder.isEstimatedTime;
        isDelayed = builder.isDelayed;
        isDiverted = builder.isDiverted;
        isTerminus = builder.isTerminus;
        isPartRoute = builder.isPartRoute;
        order = builder.order;
    }

    /**
     * Get the {@code stopCode} of the departure bus stop.
     * 
     * @return The {@code stopCode} of the departure bus stop.
     */
    @NonNull
    public String getStopCode() {
        return stopCode;
    }
    
    /**
     * Get the name of the departure bus stop.
     * 
     * @return The name of the departure bus stop.
     */
    @Nullable
    public String getStopName() {
        return stopName;
    }

    /**
     * Get a {@link Date} object representing the departure time.
     * 
     * @return A {@link Date} object representing the departure time.
     */
    @NonNull
    public Date getDepartureTime() {
        return departureTime;
    }
    
    /**
     * Get the number of minutes to departure relative to the time that this method is called. A
     * negative number will be returned if the departure time has been passed.
     * 
     * @return The number of minutes to departure. A negative number will be returned if the
     * departure time has been passed.
     */
    public int getDepartureMinutes() {
        return (int) ((departureTime.getTime() - new Date().getTime()) / 60000);
    }
    
    /**
     * Get whether the bus stop has a current disruption set or not.
     * 
     * @return {@code true} if the bus stop is disrupted, {@code false} if not.
     */
    public boolean isBusStopDisrupted() {
        return isBusStopDisrupted;
    }
    
    /**
     * Get whether the time is estimated or not.
     * 
     * @return {@code true} if the time is estimated, {@code false} if it is real-time.
     */
    public boolean isEstimatedTime() {
        return isEstimatedTime;
    }
    
    /**
     * Get whether the service is delayed or not.
     * 
     * @return {@code true} if the service is delayed, {@code false} if not.
     */
    public boolean isDelayed() {
        return isDelayed;
    }
    
    /**
     * Get whether the service is diverted or not.
     * 
     * @return {@code true} if the service is diverted, {@code false} if not.
     */
    public boolean isDiverted() {
        return isDiverted;
    }
    
    /**
     * Get whether this point is a terminus stop on the journey's route.
     * 
     * @return {@code true} if this point is a terminus for this journey, {@code false} if not.
     */
    public boolean isTerminus() {
        return isTerminus;
    }
    
    /**
     * Get whether this journey is only going to complete part of its route or not.
     * 
     * @return {@code true} if this journey will only complete part of its route, {@code false}
     * if not.
     */
    public boolean isPartRoute() {
        return isPartRoute;
    }

    @Override
    public int compareTo(@NonNull final JourneyDeparture another) {
        return order - another.order;
    }

    /**
     * This {@link Builder} must be used to construct a new {@link JourneyDeparture}. Create a new
     * instance of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private String stopCode;
        private String stopName;
        private Date departureTime;
        private boolean isBusStopDisrupted;
        private boolean isEstimatedTime;
        private boolean isDelayed;
        private boolean isDiverted;
        private boolean isTerminus;
        private boolean isPartRoute;
        private int order;

        /**
         * Set the {@code stopCode} of the departure bus stop.
         *
         * @param stopCode The {@code stopCode} of the departure bus stop.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setStopCode(@Nullable final String stopCode) {
            this.stopCode = stopCode;
            return this;
        }

        /**
         * Set the name of the departure bus stop.
         *
         * @param stopName The name of the departure bus stop.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setStopName(@Nullable final String stopName) {
            this.stopName = stopName;
            return this;
        }

        /**
         * Set the {@link Date} object representing the departure time.
         *
         * @param departureTime The {@link Date} object representing the departure time.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setDepartureTime(@Nullable final Date departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        /**
         * Set whether this bus stop has a current disruption or not.
         *
         * @param isBusStopDisrupted {@code true} if this bus stop has a current disruption,
         * {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsBusStopDisrupted(final boolean isBusStopDisrupted) {
            this.isBusStopDisrupted = isBusStopDisrupted;
            return this;
        }

        /**
         * Set whether the time is estimated or not.
         *
         * @param isEstimatedTime {@code true} if the time is estimated, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsEstimatedTime(final boolean isEstimatedTime) {
            this.isEstimatedTime = isEstimatedTime;
            return this;
        }

        /**
         * Set whether the service is delayed or not.
         *
         * @param isDelayed {@code true} if the service is delayed, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsDelayed(final boolean isDelayed) {
            this.isDelayed = isDelayed;
            return this;
        }

        /**
         * Set whether the service is diverted or not.
         *
         * @param isDiverted {@code true} if the service is diverted, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsDiverted(final boolean isDiverted) {
            this.isDiverted = isDiverted;
            return this;
        }

        /**
         * Set whether this departure point is the terminating point or not.
         *
         * @param isTerminus {@code true} if this departure point is the terminating point,
         * {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsTerminus(final boolean isTerminus) {
            this.isTerminus = isTerminus;
            return this;
        }

        /**
         * Set whether this journey is only going to complete part of its route or not.
         *
         * @param isPartRoute {@code true} if this journey is only going to complete part of its
         * route, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsPartRoute(final boolean isPartRoute) {
            this.isPartRoute = isPartRoute;
            return this;
        }

        /**
         * Set the ordering of this journey departure.
         *
         * @param order The order of this journey departure.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setOrder(final int order) {
            this.order = order;
            return this;
        }

        /**
         * Build a new {@link JourneyDeparture} object.
         *
         * @return A new {@link JourneyDeparture} object.
         * @throws IllegalArgumentException When the departure time is {@code null}, or the
         * stop code is {@code null} or empty.
         */
        @NonNull
        public JourneyDeparture build() {
            return new JourneyDeparture(this);
        }
    }
}