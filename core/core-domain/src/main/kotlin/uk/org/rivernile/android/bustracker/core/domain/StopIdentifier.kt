/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.domain

/**
 * This defines a generic stop identifier. This is fully defined in either [AtcoStopIdentifier] or
 * [NaptanStopIdentifier].
 *
 * @author Niall Scott
 */
public sealed interface StopIdentifier {

    /**
     * Convert the stop identifier to a human-readable string.
     *
     * @return This identifier as a human-readable string.
     */
    public fun toHumanReadableString(): String
}

/**
 * This defines an ATCO stop identifier.
 *
 * @property atcoCode The ATCO code as a [String].
 */
@JvmInline
public value class AtcoStopIdentifier(
    public val atcoCode: String
) : StopIdentifier {

    override fun toHumanReadableString(): String = atcoCode
}

/**
 * This defines a Naptan (SMS) stop identifier.
 *
 * @property naptanStopCode The Naptan (SMS) code as a [String].
 */
@JvmInline
public value class NaptanStopIdentifier(
    public val naptanStopCode: String
) : StopIdentifier {

    override fun toHumanReadableString(): String = naptanStopCode
}

/**
 * Convert a [String] in to a [AtcoStopIdentifier].
 */
public fun String.toAtcoStopIdentifier(): AtcoStopIdentifier = AtcoStopIdentifier(this)

/**
 * Convert a [String] in to a [NaptanStopIdentifier].
 */
public fun String.toNaptanStopIdentifier(): NaptanStopIdentifier = NaptanStopIdentifier(this)
