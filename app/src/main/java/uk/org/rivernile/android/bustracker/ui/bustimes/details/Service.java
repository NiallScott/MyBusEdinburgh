/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.details;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class holds data relating to a service.
 *
 * @author Niall Scott
 */
class Service {

    private final String serviceName;
    private final String description;
    private final int colour;

    /**
     * Create a new {@code Service}.
     *
     * @param serviceName The name of the service.
     * @param description The description of the service.
     * @param colour The colour hex to use for the service.
     */
    Service(@NonNull final String serviceName, @Nullable final String description,
            @ColorInt final int colour) {
        this.serviceName = serviceName;
        this.description = description;
        this.colour = colour;
    }

    /**
     * Get the service name.
     *
     * @return The name of the service.
     */
    @NonNull
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the description of the service.
     *
     * @return The description of the service.
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Get the colour of the service.
     *
     * @return The colour of the service.
     */
    @ColorInt
    public int getColour() {
        return colour;
    }
}