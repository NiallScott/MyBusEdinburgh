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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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

private const val LOCATION_UNKNOWN_PROGRESS_MILLIS = 10000L

internal class RealUiContentRetriever @Inject constructor(
    private val nearestStopsRetriever: NearestStopsRetriever,
    private val servicesRepository: ServicesRepository,
    private val dropdownMenuGenerator: UiNearestStopDropdownMenuGenerator,
    private val serviceNameComparator: Comparator<String>
) : UiContentRetriever {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val uiContentFlow get() = stateWithServiceColoursFlow
        .flatMapLatest(::getUiContentFlowWithStateAndServices)

    private fun getUiContentFlowWithStateAndServices(
        nearestStopsStateWithServiceColours: NearestStopsStateWithServiceColours
    ): Flow<UiContent> {
        return when (val state = nearestStopsStateWithServiceColours.nearestStopsState) {
            is NearestStopsState.Stops -> getUiContentFlowWithStops(
                stops = state.stops,
                serviceColours = nearestStopsStateWithServiceColours.serviceColours
            )
            is NearestStopsState.Error.NoLocationFeature ->
                flowOf(UiContent.Error.NoLocationFeature)
            is NearestStopsState.Error.InsufficientLocationPermissions ->
                flowOf(UiContent.Error.InsufficientLocationPermissions)
            is NearestStopsState.Error.LocationOff -> flowOf(UiContent.Error.LocationOff)
            is NearestStopsState.Error.LocationUnknown -> flow {
                emit(UiContent.InProgress)
                delay(LOCATION_UNKNOWN_PROGRESS_MILLIS.milliseconds)
                emit(UiContent.Error.LocationUnknown)
            }
        }
    }

    private fun getUiContentFlowWithStops(
        stops: List<NearestStop>?,
        serviceColours: Map<ServiceDescriptor, ServiceColours>?
    ): Flow<UiContent> {
        return if (!stops.isNullOrEmpty()) {
            val stopIdentifiers = stops.map { it.stopIdentifier }.toSet()

            dropdownMenuGenerator
                .getDropdownMenuItemsForStopsFlow(stopIdentifiers)
                .map { dropdownMenus ->
                    UiContent.Content(
                        nearestStops = stops
                            .toUiNearestStops(
                                serviceColours = serviceColours,
                                dropdownMenus = dropdownMenus,
                                serviceNameComparator = serviceNameComparator
                            )
                            .sortedBy { it.distanceMeters }
                            .toImmutableList()
                    )
                }
        } else {
            flowOf(UiContent.Error.NoNearestStops)
        }
    }

    private val stateWithServiceColoursFlow: Flow<NearestStopsStateWithServiceColours> get() =
        combine(
            nearestStopsRetriever.nearestStopsStateFlow,
            servicesRepository.getColoursForServicesFlow(),
            ::NearestStopsStateWithServiceColours
        )

    private data class NearestStopsStateWithServiceColours(
        val nearestStopsState: NearestStopsState,
        val serviceColours: Map<ServiceDescriptor, ServiceColours>?
    )
}
