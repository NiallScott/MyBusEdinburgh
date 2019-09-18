/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

/**
 * This [Exception] should be thrown when there is a problem authenticating with the remote
 * server, for example, if an API key is required and it is rejected.
 *
 * @author Niall Scott
 */
class AuthenticationException : TrackerException {

    /**
     * Create a new `AuthenticationException` with a default message.
     */
    constructor() : super("There was a problem authenticating with the remote server.")

    /**
     * Create a new `AuthenticationException` with the given `detailMessage`.
     *
     * @param detailMessage The message to set in the [Exception].
     */
    constructor(detailMessage: String) : super(detailMessage)
}