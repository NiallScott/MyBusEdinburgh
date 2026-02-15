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
 * This defines a service descriptor, to uniquely identify a service.
 */
public interface ServiceDescriptor {

    /**
     * The name of the service.
     */
    public val serviceName: String

    /**
     * A code which denotes which operator is providing the service.
     */
    public val operatorCode: String
}

/**
 * Create a [ServiceDescriptor].
 *
 * @param serviceName See [ServiceDescriptor.serviceName].
 * @param operatorCode See [ServiceDescriptor.operatorCode].
 */
public fun ServiceDescriptor(
    serviceName: String,
    operatorCode: String
): ServiceDescriptor = RequestableServiceDescriptor(
    serviceName = serviceName,
    operatorCode = operatorCode
)

/**
 * The [Any.equals] implementation for any classes which implement [ServiceDescriptor].
 *
 * @param other See [Any.equals].
 * @return See [Any.equals].
 */
public fun ServiceDescriptor.isEquals(other: Any?): Boolean {
    if (this === other) {
        return true
    }

    if (other !is ServiceDescriptor) {
        return false
    }

    if (serviceName != other.serviceName) {
        return false
    }

    if (operatorCode != other.operatorCode) {
        return false
    }

    return true
}

/**
 * The [Any.hashCode] implementation for any classes which implement [ServiceDescriptor].
 *
 * @return See [Any.hashCode].
 */
public fun ServiceDescriptor.calculateHashCode(): Int {
    var result = serviceName.hashCode()
    result = 31 * result + operatorCode.hashCode()
    return result
}

/**
 * Sort this [Collection] of [ServiceDescriptor]s so that the sorting is by service name ascending.
 * The algorithm for sorting the service name is determined by [serviceNameComparator].
 *
 * @param serviceNameComparator The [Comparator], or algorithm, for sorting services.
 * @return This [Collection] sorted so by service name ascending.
 */
public fun Collection<ServiceDescriptor>.sortByServiceName(
    serviceNameComparator: Comparator<String>
): List<ServiceDescriptor> {
    val comparator = compareBy<ServiceDescriptor, String>(serviceNameComparator) {
        it.serviceName
    }

    return sortedWith(comparator)
}

private class RequestableServiceDescriptor(
    override val serviceName: String,
    override val operatorCode: String
) : ServiceDescriptor {

    override fun equals(other: Any?) = isEquals(other)

    override fun hashCode() = calculateHashCode()

    override fun toString(): String {
        return "RequestableServiceDescriptor(serviceName='$serviceName', " +
            "operatorCode='$operatorCode')"
    }
}
