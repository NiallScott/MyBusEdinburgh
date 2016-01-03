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
import com.davekoelle.alphanum.AlphanumComparator;
import java.util.List;

/**
 * A {@code LiveBusService} represents a single bus service that stops at a single bus stop. It
 * holds a collection of {@link LiveBus}es (or rather, live bus departures).
 * 
 * @author Niall Scott
 */
public class LiveBusService implements Comparable<LiveBusService> {
    
    private static final AlphanumComparator COMPARATOR = new AlphanumComparator();

    private final String serviceName;
    private final List<LiveBus> buses;
    private final String operator;
    private final String route;
    private final boolean isDisrupted;
    private final boolean isDiverted;

    /**
     * Create a new {@code LiveBusService}. This constructor is not publicly accessible. To
     * construct an instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    protected LiveBusService(@NonNull final Builder builder) {
        if (TextUtils.isEmpty(builder.serviceName)) {
            throw new IllegalArgumentException("The serviceName must not be null or empty.");
        }

        if (builder.buses == null) {
            throw new IllegalArgumentException("The List of buses must not be null.");
        }

        serviceName = builder.serviceName;
        buses = builder.buses;
        operator = builder.operator;
        route = builder.route;
        isDisrupted = builder.isDisrupted;
        isDiverted = builder.isDiverted;
    }
    
    /**
     * Get the name of this service.
     * 
     * @return The name of this service.
     */
    @NonNull
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Get the {@link List} of {@link LiveBus} departures attributed to this service.
     * 
     * @return The {@link List} of {@link LiveBus} departures attributed to this service.
     */
    @NonNull
    public List<LiveBus> getLiveBuses() {
        return buses;
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
     * Is the service disrupted?
     *
     * <p>
     *     Not all real-time systems will support this flag. In this case, {@code false} will be
     *     returned.
     * </p>
     * 
     * @return {@code true} if there is a disruption in place for this service, {@code false} if
     * not.
     */
    public boolean isDisrupted() {
        return isDisrupted;
    }
    
    /**
     * Is the service diverted?
     * 
     * <p>
     *     Not all real-time systems will support this flag. In this case, {@code false} will be
     *     returned.
     * </p>
     * 
     * @return {@code true} if this service is diverted, {@code false} if not.
     */
    public boolean isDiverted() {
        return isDiverted;
    }

    @Override
    public int compareTo(@NonNull final LiveBusService another) {
        return COMPARATOR.compare(this.serviceName, another.serviceName);
    }

    /**
     * This {@link Builder} must be used to construct a new {@link LiveBusService}. Create a new
     * instance of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder {

        private String serviceName;
        private List<LiveBus> buses;
        private String operator;
        private String route;
        private boolean isDisrupted;
        private boolean isDiverted;

        /**
         * Set the name of this service.
         *
         * @param serviceName The name of this service.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setServiceName(@Nullable final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * Get the {@link List} of {@link LiveBus} departures attributed to this service.
         *
         * @param buses The {@link List} of {@link LiveBus} departures attributed to this service.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setBuses(@Nullable final List<LiveBus> buses) {
            this.buses = buses;
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
         * Set whether this service is disrupted or not.
         *
         * @param isDisrupted {@code true} if this service is disrupted, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsDisrupted(final boolean isDisrupted) {
            this.isDisrupted = isDisrupted;
            return this;
        }

        /**
         * Set whether this service is diverted or not.
         *
         * @param isDiverted {@code true} if this service is diverted, {@code false} if not.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setIsDiverted(final boolean isDiverted) {
            this.isDiverted = isDiverted;
            return this;
        }

        /**
         * Build a new {@link LiveBusService} object.
         *
         * @return A new {@link LiveBusService} object.
         * @throws IllegalArgumentException When the {@link List} of {@link LiveBus}es is
         * {@code null}, or the service name is {@code null} or empty.
         */
        @NonNull
        public LiveBusService build() {
            return new LiveBusService(this);
        }
    }
}