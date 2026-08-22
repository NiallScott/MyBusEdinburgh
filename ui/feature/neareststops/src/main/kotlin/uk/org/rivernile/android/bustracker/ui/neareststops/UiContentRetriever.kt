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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This is used to retrieve the [UiContent] state.
 *
 * @author Niall Scott
 */
internal interface UiContentRetriever {

    /**
     * A [Flow] which emits the current [UiContent] state.
     */
    val uiContentFlow: Flow<UiContent>
}

internal class RealUiContentRetriever @Inject constructor(
    private val nearestStopsRetriever: NearestStopsRetriever,
    private val servicesRepository: ServicesRepository,
    private val dropdownMenuGenerator: UiNearestStopDropdownMenuGenerator,
    private val serviceNameComparator: Comparator<String>
) : UiContentRetriever {

    override val uiContentFlow get() = combine(
        nearestStopsStateWithDropdownMenusFlow,
        servicesRepository.getColoursForServicesFlow()
    ) { nearestStopsStateWithDropdownMenus, serviceColours ->
        createUiContent(
            nearestStopsState = nearestStopsStateWithDropdownMenus.nearestStopsState,
            dropdownMenus = nearestStopsStateWithDropdownMenus.dropdownMenus,
            serviceColours = serviceColours
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val nearestStopsStateWithDropdownMenusFlow get() = nearestStopsRetriever
        .nearestStopsStateFlow
        .flatMapLatest(::getNearestStopsStateWithDropdownMenusFlow)

    private fun createUiContent(
        nearestStopsState: NearestStopsState,
        dropdownMenus: Map<StopIdentifier, UiNearestStopDropdownMenu>?,
        serviceColours: Map<ServiceDescriptor, ServiceColours>?
    ): UiContent {
        return when (nearestStopsState) {
            is NearestStopsState.AwaitingLocation -> UiContent.InProgress
            is NearestStopsState.Stops -> nearestStopsState.toUiContent(
                dropdownMenus = dropdownMenus,
                serviceColours = serviceColours
            )
            is NearestStopsState.Error.NoLocationFeature ->
                UiContent.Error.NoLocationFeature
            is NearestStopsState.Error.InsufficientLocationPermissions ->
                UiContent.Error.InsufficientLocationPermissions
            is NearestStopsState.Error.LocationOff -> UiContent.Error.LocationOff
            is NearestStopsState.Error.LocationUnknown -> UiContent.Error.LocationUnknown
        }
    }

    private fun NearestStopsState.Stops.toUiContent(
        dropdownMenus: Map<StopIdentifier, UiNearestStopDropdownMenu>?,
        serviceColours: Map<ServiceDescriptor, ServiceColours>?
    ): UiContent {
        val stops = stops
            ?.toUiNearestStops(
                serviceColours = serviceColours,
                dropdownMenus = dropdownMenus,
                serviceNameComparator = serviceNameComparator
            )
            ?.sortedBy { it.distanceMeters }

        return if (!stops.isNullOrEmpty()) {
            UiContent.Content(
                nearestStops = stops.toImmutableList(),
                locationAccuracy = locationAccuracy
            )
        } else {
            UiContent.Error.NoNearestStops(
                locationAccuracy = locationAccuracy
            )
        }
    }

    private fun getNearestStopsStateWithDropdownMenusFlow(
        nearestStopsState: NearestStopsState
    ): Flow<NearestStopsStateWithDropdownMenus> {
        return if (nearestStopsState is NearestStopsState.Stops) {
            val stopIdentifiers = nearestStopsState.stops?.map { it.stopIdentifier }?.toSet()

            if (!stopIdentifiers.isNullOrEmpty()) {
                dropdownMenuGenerator
                    .getDropdownMenuItemsForStopsFlow(stopIdentifiers)
                    .map {
                        NearestStopsStateWithDropdownMenus(
                            nearestStopsState = nearestStopsState,
                            dropdownMenus = it
                        )
                    }
            } else {
                flowOf(
                    NearestStopsStateWithDropdownMenus(
                        nearestStopsState = nearestStopsState
                    )
                )
            }
        } else {
            flowOf(
                NearestStopsStateWithDropdownMenus(
                    nearestStopsState = nearestStopsState
                )
            )
        }
    }

    private data class NearestStopsStateWithDropdownMenus(
        val nearestStopsState: NearestStopsState,
        val dropdownMenus: Map<StopIdentifier, UiNearestStopDropdownMenu>? = null
    )
}
