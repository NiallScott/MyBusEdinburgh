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
 * A {@code Journey} represents a bus travelling from some origin to a destination with
 * the calling points in-between. A {@code Journey} will have some sort of ID. The journey will
 * be carried out by a given service name and have an operator. It will also have an
 * destination/end point.
 * 
 * @author Niall Scott
 */
public class Journey {
    
    private final String journeyId;
    private final String serviceName;
    private final List<JourneyDeparture> departures;
    private final String operator;
    private final String route;
    private final String destination;
    private final String terminus;
    private final boolean hasGlobalDisruption;
    private final boolean hasServiceDisruption;
    private final boolean hasServiceDiversion;
    private final long receiveTime;

    /**
     * Create a new {@code Journey}. This constructor is not publicly accessible. To construct an
     * instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    protected Journey(@NonNull final Builder builder) {
        if (TextUtils.isEmpty(builder.journeyId)) {
            throw new IllegalArgumentException("The journeyId must not be null or empty.");
        }

        if (TextUtils.isEmpty(builder.serviceName)) {
            throw new IllegalArgumentException("The serviceName must not be null or empty.");
        }

        if (builder.departures == null) {
            throw new IllegalArgumentException("The departures must not be null");
        }

        if (TextUtils.isEmpty(builder.terminus)) {
            throw new IllegalArgumentException("The terminus must not be null or empty.");
        }

        journeyId = builder.journeyId;
        serviceName = builder.serviceName;
        departures = builder.departures;
        operator = builder.operator;
        route = builder.route;
        destination = builder.destination;
        terminus = builder.terminus;
        hasGlobalDisruption = builder.hasGlobalDisruption;
        hasServiceDisruption = builder.hasServiceDisruption;
        hasServiceDiversion = builder.hasServiceDiversion;
        receiveTime = builder.receiveTime;
    }
    
    /**
     * Get the ID of this journey.
     * 
     * @return The ID of this journey.
     */
    @NonNull
    public String getJourneyId() {
        return journeyId;
    }

    /**
     * Get the name of the service completing this journey.
     * 
     * @return The name of the service completing this journey.
     */
    @NonNull
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Get the {@link List} of {@link JourneyDeparture}s for this journey.
     * 
     * @return The {@link List} of {@link JourneyDeparture}s for this journey.
     */
    @NonNull
    public List<JourneyDeparture> getDepartures() {
        return departures;
    }

    /**
     * Get the name of the operator performing the journey.
     * 
     * <p>
     *     Not all real-time systems will support this field. In this case, {@code null} or an
     *     empty {@link String} should be returned.
     * </p>
     * 
     * @return The name of the operator performing the journey. May be {@code null} or an empty
     * {@link String}.
     */
    @Nullable
    public String getOperator() {
        return operator;
    }

    /**
     * Get a textual description of the route.
     * 
     * <p>
     *     Not all real-time systems will support this field. In this case, {@code null} or an
     *     empty {@link String} should be returned.
     * </p>
     * 
     * @return A textual description of the route. May be {@code null} or an empty {@link String}.
     */
    @Nullable
    public String getRoute() {
        return route;
    }

    /**
     * Get the name of the destination of the journey.
     * 
     * <p>
     *     Not all real-time systems will support this field. In this case, {@code null} or an
     *     empty {@link String} should be returned.
     * </p>
     * 
     * @return The name of the destination of the journey. May be {@code null} or an empty
     * {@link String}.
     */
    @Nullable
    public String getDestination() {
        return destination;
    }

    /**
     * Get the {@code stopCode} of the terminus stop.
     * 
     * @return The {@code stopCode} of the terminus stop.
     */
    @NonNull
    public String getTerminus() {
        return terminus;
    }
    
    /**
     * Is there a disruption that affects the whole network or the real-time system?
     * 
     * <p>
     *     Not all real-time systems will support this flag. In this case, {@code false} will be
     *     returned.
     * </p>
     * 
     * @return {@code true} if there is a global disruption in place, {@code false} if not.
     */
    public boolean hasGlobalDisruption() {
        return hasGlobalDisruption;
    }
    
    /**
     * Is the service that this journey is served by disrupted?
     * 
     * <p>
     *     Not all real-time systems will support this flag. In this case, {@code false} will be
     *     returned.
     * </p>
     *
     * @return {@code true} if there is a disruption in place for this service, {@code false} if
     * not.
     */
    public boolean hasServiceDisruption() {
        return hasServiceDisruption;
    }
    
    /**
     * Is the service that this journey is served by diverted?
     * 
     * <p>
     *     Not all real-time systems will support this flag. In this case, {@code false} will be
     *     returned.
     * </p>
     * 
     * @return {@code true} if this service is diverted, {@code false} if not.
     */
    public boolean hasServiceDiversion() {
        return hasServiceDiversion;
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
     * This {@link Builder} must be used to construct a new {@link Journey}. Create a new
     * instance of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private String journeyId;
        private String serviceName;
        private List<JourneyDeparture> departures;
        private String operator;
        private String route;
        private String destination;
        private String terminus;
        private boolean hasGlobalDisruption;
        private boolean hasServiceDisruption;
        private boolean hasServiceDiversion;
        private long receiveTime;

        /**
         * Set the ID of this journey.
         *
         * @param journeyId The ID of this journey.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setJourneyId(@Nullable final String journeyId) {
            this.journeyId = journeyId;
            return this;
        }

        /**
         * Set the name of the service completing this journey.
         *
         * @param serviceName The name of the service completing this journey.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setServiceName(@Nullable final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * Set the {@link List} of {@link JourneyDeparture}s for this journey.
         *
         * @param departures The {@link List} of {@link JourneyDeparture}s for this journey.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setDepartures(@Nullable final List<JourneyDeparture> departures) {
            this.departures = departures;
            return this;
        }

        /**
         * Set the name of the operator performing the journey.
         *
         * @param operator The name of the operator performing the journey.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setOperator(@Nullable final String operator) {
            this.operator = operator;
            return this;
        }

        /**
         * Set a textual description of the route.
         *
         * @param route A textual description of the route.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setRoute(@Nullable final String route) {
            this.route = route;
            return this;
        }

        /**
         * Set the name of the destination of the journey.
         *
         * @param destination The name of the destination of the journey.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setDestination(@Nullable final String destination) {
            this.destination = destination;
            return this;
        }

        /**
         * Set the {@code stopCode} of the terminus stop.
         *
         * @param terminus The {@code stopCode} of the terminus stop.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setTerminus(@Nullable final String terminus) {
            this.terminus = terminus;
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
         * Set whether there is a disruption that affects this service or not.
         *
         * @param hasServiceDisruption {@code true} if there is a disruption that affects this
         * service, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setHasServiceDisruption(final boolean hasServiceDisruption) {
            this.hasServiceDisruption = hasServiceDisruption;
            return this;
        }

        /**
         * Set whether there is a diversion that affects this service or not.
         *
         * @param hasServiceDiversion {@code true} if there is a diversion that affects this
         * service, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setHasServiceDiversion(final boolean hasServiceDiversion) {
            this.hasServiceDiversion = hasServiceDiversion;
            return this;
        }

        /**
         * Set the time the data was received at. This should be based on
         * {@link android.os.SystemClock#elapsedRealtime()}.
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
         * Build a new {@link Journey} object.
         *
         * @return A new {@link Journey} object.
         * @throws IllegalArgumentException When {@code journeyId} is {@code null} or empty,
         * {@code serviceName} is {@code null} or empty, {@code departures} is {@code null}, or
         * {@code terminus} is {@code null} or empty.
         */
        @NonNull
        public Journey build() {
            return new Journey(this);
        }
    }
}