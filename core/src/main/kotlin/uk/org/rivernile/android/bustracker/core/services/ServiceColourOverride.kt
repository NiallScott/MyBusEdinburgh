/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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
     * This gives implementations the opportunity to override the contents of [serviceColours] or
     * add new items to it. If no changes are being made, [serviceColours] should just be returned
     * as-is.
     *
     * @param services The service names that colours should be determined for.
     * @param serviceColours The mapping of service names to colours which has been loaded upstream.
     * This may be `null` if upstream was unable to load colours.
     * @return The mapping of service names to colours which should be used instead.
     */
    fun overrideServiceColours(
            services: Set<String>?,
            serviceColours: Map<String, Int>?): Map<String, Int>?
}