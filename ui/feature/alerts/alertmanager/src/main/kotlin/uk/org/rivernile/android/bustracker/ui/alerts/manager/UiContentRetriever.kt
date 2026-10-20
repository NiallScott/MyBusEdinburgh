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

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.alerts.Alert
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * Used to retrieve the [UiContent] for the alert manager feature.
 *
 * @author Niall Scott
 */
internal interface UiContentRetriever {

    /**
     * A [Flow] which emits the current [UiContent] to show on the UI.
     */
    val uiContentFlow: Flow<UiContent>
}

internal class RealUiContentRetriever @Inject constructor(
    private val featureRepository: FeatureRepository,
    private val alertsRepository: AlertsRepository,
    private val servicesRepository: ServicesRepository,
    private val stopDetailsRetriever: UiStopDetailsRetriever,
    private val serviceNameComparator: Comparator<String>
) : UiContentRetriever {

    private val hasAlertManagerFeature by lazy {
        featureRepository.hasArrivalAlertFeature || featureRepository.hasProximityAlertFeature
    }

    private val hasMapFeature by lazy {
        featureRepository.hasStopMapUiFeature
    }

    override val uiContentFlow get() = if (hasAlertManagerFeature) {
        alertsWithStopDetailsFlow
            .combine(
                servicesRepository.getColoursForServicesFlow()
            ) { alertsWithStopDetails, coloursForServices ->
                createContent(
                    alerts = alertsWithStopDetails?.first,
                    coloursForServices = coloursForServices,
                    stopDetails = alertsWithStopDetails?.second
                )
            }
    } else {
        flowOf(UiContent.NoAlertsFeature)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val alertsWithStopDetailsFlow get() = alertsRepository
        .allAlertsFlow
        .flatMapLatest(::getAlertsWithStopDetailsFlow)

    private fun getAlertsWithStopDetailsFlow(
        alerts: List<Alert>?
    ): Flow<Pair<List<Alert>, Map<StopIdentifier, UiStopDetails>?>?> {
        return if (!alerts.isNullOrEmpty()) {
            val stopIdentifiers = alerts.map { it.stopIdentifier }.toSet()

            stopDetailsRetriever
                .getUiStopDetailsFlow(
                    stopIdentifiers = stopIdentifiers
                )
                .map { Pair(alerts, it) }
        } else {
            flowOf(null)
        }
    }

    private fun createContent(
        alerts: List<Alert>?,
        coloursForServices: Map<ServiceDescriptor, ServiceColours>?,
        stopDetails: Map<StopIdentifier, UiStopDetails>?
    ): UiContent {
        return if (!alerts.isNullOrEmpty()) {
            UiContent.Content(
                alerts = alerts
                    .toUiAlerts(
                        stopDetails = stopDetails,
                        serviceColours = coloursForServices,
                        hasMapFeature = hasMapFeature,
                        serviceNameComparator = serviceNameComparator
                    )
                    .toImmutableList()
            )
        } else {
            UiContent.EmptyAlerts
        }
    }
}
