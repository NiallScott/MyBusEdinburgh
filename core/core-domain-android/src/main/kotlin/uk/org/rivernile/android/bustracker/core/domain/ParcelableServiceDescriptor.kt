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
 * This is a [Parcelable] version of [ServiceDescriptor] which is suitable for being serialised by
 * Android components.
 *
 * @property serviceName See [ServiceDescriptor.serviceName].
 * @property operatorCode See [ServiceDescriptor.operatorCode].
 * @author Niall Scott
 */
@Parcelize
public class ParcelableServiceDescriptor(
    override val serviceName: String,
    override val operatorCode: String
) : ServiceDescriptor, Parcelable {

    override fun equals(other: Any?): Boolean = isEquals(other)

    override fun hashCode(): Int = calculateHashCode()

    override fun toString(): String {
        return "ParcelableServiceDescriptor(serviceName='$serviceName', " +
            "operatorCode='$operatorCode')"
    }
}

/**
 * Map a [List] of [ServiceDescriptor]s to a [List] of [ParcelableServiceDescriptor].
 */
public fun List<ServiceDescriptor>
    .toParcelableServiceDescriptorList(): List<ParcelableServiceDescriptor> {
    return map { it.toParcelableServiceDescriptor() }
}

/**
 * Maps a [ServiceDescriptor] to a [ParcelableServiceDescriptor], to create objects which can be
 * serialised by Android components.
 *
 * @return This [ServiceDescriptor] as a [ParcelableServiceDescriptor].
 */
public fun ServiceDescriptor.toParcelableServiceDescriptor(): ParcelableServiceDescriptor {
    return this as? ParcelableServiceDescriptor
        ?: ParcelableServiceDescriptor(
            serviceName = serviceName,
            operatorCode = operatorCode
        )
}
