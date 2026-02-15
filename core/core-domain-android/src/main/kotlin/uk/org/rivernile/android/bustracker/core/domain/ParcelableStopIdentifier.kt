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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A [Parcelable] version of [StopIdentifier].
 *
 * @author Niall Scott
 */
public sealed interface ParcelableStopIdentifier : Parcelable

/**
 * A [Parcelable] version of [AtcoStopIdentifier].
 *
 * @property atcoCode The ATCO code.
 */
@JvmInline
@Parcelize
public value class ParcelableAtcoStopIdentifier(
    public val atcoCode: String
) : ParcelableStopIdentifier

/**
 * A [Parcelable] version of the [NaptanStopIdentifier].
 *
 * @property naptanStopCode The Naptan code.
 */
@JvmInline
@Parcelize
public value class ParcelableNaptanStopIdentifier(
    public val naptanStopCode: String
): ParcelableStopIdentifier

/**
 * Map a [ParcelableNaptanStopIdentifier] to a [StopIdentifier].
 *
 * @return This [ParcelableNaptanStopIdentifier] as a [StopIdentifier].
 */
public fun ParcelableStopIdentifier.toStopIdentifier(): StopIdentifier {
    return when (this) {
        is ParcelableAtcoStopIdentifier -> toAtcoStopIdentifier()
        is ParcelableNaptanStopIdentifier -> toNaptanStopIdentifier()
    }
}

/**
 * Map this [ParcelableAtcoStopIdentifier] to an [AtcoStopIdentifier].
 *
 * @return This [ParcelableAtcoStopIdentifier] as an [AtcoStopIdentifier].
 */
public fun ParcelableAtcoStopIdentifier.toAtcoStopIdentifier(): AtcoStopIdentifier =
    atcoCode.toAtcoStopIdentifier()

/**
 * Map this [ParcelableNaptanStopIdentifier] to a [NaptanStopIdentifier].
 *
 * @return This [ParcelableNaptanStopIdentifier] as a [NaptanStopIdentifier].
 */
public fun ParcelableNaptanStopIdentifier.toNaptanStopIdentifier(): NaptanStopIdentifier =
    naptanStopCode.toNaptanStopIdentifier()

/**
 * Map this [String] to a [ParcelableAtcoStopIdentifier].
 *
 * @return This [String] as a [ParcelableAtcoStopIdentifier].
 */
public fun String.toParcelableAtcoStopIdentifier(): ParcelableAtcoStopIdentifier =
    ParcelableAtcoStopIdentifier(this)

/**
 * Map this [String] to a [ParcelableNaptanStopIdentifier].
 *
 * @return This [String] as a [ParcelableNaptanStopIdentifier].
 */
public fun String.toParcelableNaptanStopIdentifier(): ParcelableNaptanStopIdentifier =
    ParcelableNaptanStopIdentifier(this)

/**
 * Map this [StopIdentifier] to a [ParcelableStopIdentifier].
 *
 * @return This [StopIdentifier] as a [ParcelableStopIdentifier].
 */
public fun StopIdentifier.toParcelableStopIdentifier(): ParcelableStopIdentifier {
    return when (this) {
        is AtcoStopIdentifier -> toParcelableAtcoStopIdentifier()
        is NaptanStopIdentifier -> toParcelableNaptanStopIdentifier()
    }
}

/**
 * Map this [AtcoStopIdentifier] to a [ParcelableAtcoStopIdentifier].
 *
 * @return This [AtcoStopIdentifier] as a [ParcelableAtcoStopIdentifier].
 */
public fun AtcoStopIdentifier.toParcelableAtcoStopIdentifier(): ParcelableAtcoStopIdentifier {
    return ParcelableAtcoStopIdentifier(atcoCode)
}

/**
 * Map this [NaptanStopIdentifier] to a [ParcelableNaptanStopIdentifier].
 *
 * @return This [NaptanStopIdentifier] as a [ParcelableNaptanStopIdentifier].
 */
public fun NaptanStopIdentifier.toParcelableNaptanStopIdentifier(): ParcelableNaptanStopIdentifier {
    return ParcelableNaptanStopIdentifier(naptanStopCode)
}
