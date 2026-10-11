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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import uk.org.rivernile.android.bustracker.core.alerts.Alert
import uk.org.rivernile.android.bustracker.core.alerts.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.sortByServiceName
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * An alert rendered on the UI.
 *
 * @author Niall Scott
 */
@Immutable
internal sealed interface UiAlert {

    /**
     * The ID of the alert.
     */
    val id: Int

    /**
     * An arrival alert.
     *
     * @property id The ID of the alert.
     * @property stopIdentifier The stop of the alert.
     * @property stopDetails Details of the stop this alert is for.
     * @property services The [ImmutableList] of services this alert triggers for.
     * @property timeTriggerMinutes The time trigger this alert triggers for, in minutes.
     * @property isMapShown Is the map shown?
     */
    data class ArrivalAlert(
        override val id: Int,
        val stopIdentifier: StopIdentifier,
        val stopDetails: UiStopDetails?,
        val services: ImmutableList<UiServiceName>,
        val timeTriggerMinutes: Int,
        val isMapShown: Boolean
    ) : UiAlert

    /**
     * A proximity alert.
     *
     * @property id The ID of the alert.
     * @property stopIdentifier The stop of the alert.
     * @property stopDetails Details of the stop this alert is for.
     * @property distanceFromMeters The distance trigger this alert triggers for, in meters.
     * @property isMapShown Is the map shown?
     */
    data class ProximityAlert(
        override val id: Int,
        val stopIdentifier: StopIdentifier,
        val stopDetails: UiStopDetails?,
        val distanceFromMeters: Int,
        val isMapShown: Boolean
    ) : UiAlert
}

/**
 * Map this [Collection] of [Alert]s in to a [List] of [UiAlert], suitable for display on the UI.
 *
 * @param stopDetails A mapping of [UiStopDetails] to match up with alerts.
 * @param serviceColours A mapping of [ServiceColours] to match up with services.
 * @param hasMapFeature Should the map be displayed?
 * @param serviceNameComparator Used to sort the services.
 * @return This [Collection] of [Alert]s as a [List] of [UiAlert].
 */
internal fun Collection<Alert>.toUiAlerts(
    stopDetails: Map<StopIdentifier, UiStopDetails>?,
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    hasMapFeature: Boolean,
    serviceNameComparator: Comparator<String>
) = map { alert ->
    alert.toUiAlert(
        stopDetails = stopDetails?.get(alert.stopIdentifier),
        serviceColours = serviceColours,
        hasMapFeature = hasMapFeature,
        serviceNameComparator = serviceNameComparator
    )
}

private fun Alert.toUiAlert(
    stopDetails: UiStopDetails?,
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    hasMapFeature: Boolean,
    serviceNameComparator: Comparator<String>
): UiAlert {
    return when (this) {
        is ArrivalAlert -> toUiArrivalAlert(
            stopDetails = stopDetails,
            serviceColours = serviceColours,
            hasMapFeature = hasMapFeature,
            serviceNameComparator = serviceNameComparator
        )
        is ProximityAlert -> toUiProximityAlert(
            stopDetails = stopDetails,
            hasMapFeature = hasMapFeature
        )
    }
}

private fun ArrivalAlert.toUiArrivalAlert(
    stopDetails: UiStopDetails?,
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    hasMapFeature: Boolean,
    serviceNameComparator: Comparator<String>
): UiAlert.ArrivalAlert {
    val serviceNames = services
        .sortByServiceName(serviceNameComparator)
        .toUiServiceNames(
            serviceColours = serviceColours
        )
        .toImmutableList()

    return UiAlert.ArrivalAlert(
        id = id,
        stopIdentifier = stopIdentifier,
        stopDetails = stopDetails,
        services = serviceNames,
        timeTriggerMinutes = timeTriggerMinutes,
        isMapShown = hasMapFeature
    )
}

private fun ProximityAlert.toUiProximityAlert(
    stopDetails: UiStopDetails?,
    hasMapFeature: Boolean
): UiAlert.ProximityAlert {
    return UiAlert.ProximityAlert(
        id = id,
        stopIdentifier = stopIdentifier,
        stopDetails = stopDetails,
        distanceFromMeters = distanceFromMeters,
        isMapShown = hasMapFeature
    )
}

private fun Collection<ServiceDescriptor>.toUiServiceNames(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?
) = map { serviceDescriptor ->
    serviceDescriptor.toUiServiceName(
        serviceColours = serviceColours
    )
}

private fun ServiceDescriptor.toUiServiceName(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?
): UiServiceName {
    return UiServiceName(
        serviceName = serviceName,
        colours = serviceColours
            ?.get(this)
            ?.toUiServiceColours()
    )
}

private fun ServiceColours.toUiServiceColours(): UiServiceColours {
    return UiServiceColours(
        backgroundColour = colourPrimary,
        textColour = colourOnPrimary
    )
}
