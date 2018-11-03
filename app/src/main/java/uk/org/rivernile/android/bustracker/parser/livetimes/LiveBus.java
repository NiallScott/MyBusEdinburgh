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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import java.util.Date;

/**
 * A {@code LiveBus} is a bus that is part of a service which has real-time tracking information
 * associated in it, described by this class.
 * 
 * @author Niall Scott
 */
public class LiveBus implements Comparable<LiveBus> {
    
    private final String destination;
    private final Date departureTime;
    private final String terminus;
    private final String journeyId;
    private final boolean isEstimatedTime;
    private final boolean isDelayed;
    private final boolean isDiverted;
    private final boolean isTerminus;
    private final boolean isPartRoute;

    /**
     * Create a new {@code LiveBus}. This constructor is not publicly accessible. To construct an
     * instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    protected LiveBus(@NonNull final Builder builder) {
        if (TextUtils.isEmpty(builder.destination)) {
            throw new IllegalArgumentException("The destination must not be null or empty.");
        }

        if (builder.departureTime == null) {
            throw new IllegalArgumentException("The departureTime must not be null or empty.");
        }

        destination = builder.destination;
        departureTime = builder.departureTime;
        terminus = builder.terminus;
        journeyId = builder.journeyId;
        isEstimatedTime = builder.isEstimatedTime;
        isDelayed = builder.isDelayed;
        isDiverted = builder.isDiverted;
        isTerminus = builder.isTerminus;
        isPartRoute = builder.isPartRoute;
    }

    /**
     * Get the name of the destination of this bus.
     * 
     * @return The name of the destination of this bus.
     */
    @NonNull
    public String getDestination() {
        return destination;
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
     * Get the stop code of the terminating bus stop.
     *
     * <p>
     *     Not all real-time systems will support this field. In this case, {@code null} or an
     *     empty {@link String} should be returned.
     * </p>
     * 
     * @return The stop code of the terminating bus stop. May be {@code null} or an empty
     * {@link String}.
     */
    @Nullable
    public String getTerminus() {
        return terminus;
    }
    
    /**
     * Get the unique ID of the journey of this bus.
     * 
     * <p>
     *     Not all real-time systems will support this field. In this case, {@code null} or an
     *     empty {@link String} should be returned.
     * </p>
     * 
     * @return The unique ID of the journey of this bus. May be {@code null} or an empty
     * {@link String}.
     */
    @Nullable
    public String getJourneyId() {
        return journeyId;
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
    public int compareTo(@NonNull final LiveBus another) {
        return departureTime.compareTo(another.departureTime);
    }

    /**
     * This {@link Builder} must be used to construct a new {@link LiveBus}. Create a new instance
     * of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private String destination;
        private Date departureTime;
        private String terminus;
        private String journeyId;
        private boolean isEstimatedTime;
        private boolean isDelayed;
        private boolean isDiverted;
        private boolean isTerminus;
        private boolean isPartRoute;

        /**
         * Set the name of the destination of this bus.
         *
         * @param destination The name of the destination of this bus.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setDestination(@Nullable final String destination) {
            this.destination = destination;
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
         * Set the stop code of the terminating bus stop.
         *
         * @param terminus Set the stop code of the terminating bus stop.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setTerminus(@Nullable final String terminus) {
            this.terminus = terminus;
            return this;
        }

        /**
         * Set the unique ID of the journey of this bus.
         *
         * @param journeyId Set the unique ID of the journey of this bus.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setJourneyId(@Nullable final String journeyId) {
            this.journeyId = journeyId;
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
         * Set whether this point is a terminus stop on the journey's route.
         *
         * @param isTerminus {@code true} if this point is a terminus stop, {@code false} if not.
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
         * @param isPartRoute {@code true} if this journey will only complete part of its route,
         * {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsPartRoute(final boolean isPartRoute) {
            this.isPartRoute = isPartRoute;
            return this;
        }

        /**
         * Build a new {@link LiveBus} object.
         *
         * @return A new {@link LiveBus} object.
         * @throws IllegalArgumentException When the departure time is {@code null}, or the
         * destination is {@code null} or empty.
         */
        @NonNull
        public LiveBus build() {
            return new LiveBus(this);
        }
    }
}