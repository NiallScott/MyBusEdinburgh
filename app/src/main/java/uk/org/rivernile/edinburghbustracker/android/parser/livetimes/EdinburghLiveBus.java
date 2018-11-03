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

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import androidx.annotation.NonNull;

import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;

/**
 * This class is an Edinburgh-specific implementation of {@link LiveBus}.
 * 
 * @author Niall Scott
 */
public class EdinburghLiveBus extends LiveBus {
    
    private final int departureMinutes;

    /**
     * Create a new {@code EdinburghLiveBus}. This constructor is not publicly accessible. To
     * construct an instance of this class, use the {@link Builder}.
     *
     * @param builder The {@link Builder} instance to construct from.
     */
    private EdinburghLiveBus(@NonNull final Builder builder) {
        super(builder);

        departureMinutes = builder.departureMinutes;
    }

    /**
     * Get the number of minutes from the time that the request was made with the API server
     * until the bus is due to arrive the bus stop. This differs from
     * {@link LiveBus#getDepartureMinutes()} as in this case the time is calculated by the server
     * rather than on device.
     *
     * @return The number of minutes until the bus is due to arrive at the stop. This time is
     * relative to when the request was made with the server.
     */
    @Override
    public int getDepartureMinutes() {
        return departureMinutes;
    }

    /**
     * This {@link Builder} must be used to construct a new {@link EdinburghLiveBus}. Create a new
     * instance of this class, call the setters and when you are ready, call {@link #build()}.
     */
    public static class Builder extends LiveBus.Builder {

        private int departureMinutes;

        /**
         * Set the number of minutes until departure.
         *
         * @param departureMinutes The number of minutes until departure.
         * @return A reference to this {@code Builder} so that method calls can be chained.
         * @see #build()
         */
        @NonNull
        public Builder setDepartureMinutes(final int departureMinutes) {
            this.departureMinutes = departureMinutes;
            return this;
        }

        /**
         * Build a new {@link EdinburghLiveBus} object.
         *
         * @return A new {@link EdinburghLiveBus} object.
         * @throws IllegalArgumentException See {@link LiveBus.Builder#build()}.
         */
        @NonNull
        @Override
        public EdinburghLiveBus build() {
            return new EdinburghLiveBus(this);
        }
    }
}