/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

/**
 * This enumerates the different type of errors we may display in the UI.
 *
 * @author Niall Scott
 */
enum class ErrorType {

    /**
     * No stop code has been provided.
     */
    NO_STOP_CODE,
    /**
     * There is no internet connectivity available.
     */
    NO_CONNECTIVITY,
    /**
     * There was an error in the network link while attempting communication with the endpoint.
     */
    COMMUNICATION,
    /**
     * Unable to resolve the DNS hostname of the endpoint.
     */
    UNKNOWN_HOST,
    /**
     * There was an error reported by the server that we don't explicitly handle.
     */
    SERVER_ERROR,
    /**
     * The server returned no data to us, or yielded a response that is not useful to us.
     */
    NO_DATA,
    /**
     * There was an issue authenticating with the remote endpoint. In most systems, this means the
     * API key is not correct.
     */
    AUTHENTICATION,
    /**
     * The endpoint reports to us that it is currently overloaded. A retry attempt should be
     * attempted later.
     */
    SYSTEM_OVERLOADED,
    /**
     * The endpoint reports to us that it is currently down for maintenance. A retry attempt should
     * be attempted later.
     */
    DOWN_FOR_MAINTENANCE
}