/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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
 * This [Exception] is thrown when there was an issue communicating with the tracker endpoint.
 *
 * @author Niall Scott
 */
sealed class TrackerException : Exception {

    /**
     * Constructor that specifies a message.
     *
     * @param message Exception message.
     */
    constructor(message: String) : super(message)

    /**
     * Constructor that specifies a cause [Throwable].
     *
     * @param throwable The cause [Throwable].
     */
    constructor(throwable: Throwable) : super(throwable)
}

/**
 * This [TrackerException] is thrown when there is no connectivity to the internet.
 *
 * @author Niall Scott
 */
class NoConnectivityException : TrackerException("No connectivity to the internet")

/**
 * This [TrackerException] is thrown when there is a networking issue.
 *
 * @param throwable The causing [Throwable].
 * @author Niall Scott
 */
class NetworkException(throwable: Throwable) : TrackerException(throwable)

/**
 * This [TrackerException] should be thrown when there is a problem with dealing with the request on
 * the remote server, for example, when the HTTP server returns a 5xx status code.
 *
 * @author Niall Scott
 */
sealed class ServerErrorException(detailMessage: String) : TrackerException(detailMessage)

/**
 * This [Exception] is used when there was a server-side error, but we don't handle it.
 *
 * @author Niall Scott
 */
class UnrecognisedServerErrorException : ServerErrorException {

    /**
     * Create a new `UnrecognisedServerErrorException` with a default message.
     */
    constructor() : super("An unrecognised error occurred on the server.")

    /**
     * Create a new `UnrecognisedServerErrorException` with the given `detailMessage`.
     *
     * @param detailMessage The message to set in the [Exception].
     */
    constructor(detailMessage: String) : super(detailMessage)
}

/**
 * This [ServerErrorException] should be thrown when there is a problem authenticating with the
 * remote server, for example, if an API key is required and it is rejected.
 *
 * @author Niall Scott
 */
class AuthenticationException : ServerErrorException {

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

/**
 * This [Exception] should be thrown when the remote server is down for maintenance.
 *
 * @author Niall Scott
 */
class MaintenanceException : ServerErrorException("The remote server is down for maintenance.")

/**
 * This [Exception] should be thrown when the server reports that it is overloaded (or some other
 * rate limiting is implemented).
 *
 * @author Niall Scott
 */
class SystemOverloadedException : ServerErrorException("The remote server is overloaded.")