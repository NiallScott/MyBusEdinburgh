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

package uk.org.rivernile.android.bustracker.utils

/**
 * This is used as a wrapper around events which are only consumed by their first receiver, and will
 * yield `null` for all further uses.
 *
 * @param T The type of data this event contains.
 * @param content The event content.
 * @author Niall Scott
 */
class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Get the content of this event. If this event has not been handled, a non-`null` value will
     * be returned, otherwise `null` will be returned.
     *
     * @return The content of this event, or `null` if it has already been handled.
     */
    fun getContentIfNotHandled(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }

    /**
     * Peek at the content of this [Event] without handling it.
     *
     * @return The content of this [Event].
     */
    fun peek() = content
}