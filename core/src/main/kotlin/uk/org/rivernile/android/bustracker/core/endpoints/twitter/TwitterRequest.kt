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

package uk.org.rivernile.android.bustracker.core.endpoints.twitter

/**
 * An instance of this request describes a single, immutable request against the Twitter endpoint.
 *
 * @param T The type of data returned from the endpoint.
 * @author Niall Scott
 */
interface TwitterRequest<out T> {

    /**
     * Perform the request and return the result. This method is blocking.
     *
     * @return The result of the request.
     * @throws TwitterException When there was a problem with the request.
     */
    @Throws(TwitterException::class)
    fun performRequest(): T

    /**
     * Cancel the request.
     */
    fun cancel()
}