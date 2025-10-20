/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
 *
 */

package uk.org.rivernile.android.bustracker.core.services

/**
 * Implementations of this interface are given the opportunity to override the service colours
 * loaded from the database. This may be beneficial in the case when there is no colour entry in the
 * database for a given service, but there is some business logic within the specific app flavour
 * which is able to determine a colour for a service.
 *
 * @author Niall Scott
 */
interface ServiceColourOverride {

    /**
     * This gives implementations the opportunity to override the colour for a given [serviceName],
     * or assign it a colour when it does not already have a colour.
     *
     * If no changes are being made, then `null` should be returned.
     *
     * @param serviceName The display name of the service.
     * @param currentBackgroundColour The currently set colour of the service.
     * @return The new colour for the service. If `null` is returned, the colour is not overridden.
     */
    fun overrideServiceColour(
        serviceName: String,
        currentBackgroundColour: Int?
    ): ServiceColours?
}
