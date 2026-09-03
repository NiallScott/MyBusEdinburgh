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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import javax.inject.Inject

/**
 * This calculates and emits the [UiContent] for the stop details screen.
 *
 * @author Niall Scott
 */
internal interface UiContentRetriever {

    /**
     * A [Flow] which emits the current [UiContent].
     */
    val uiContentFlow: Flow<UiContent>
}

internal class RealUiContentRetriever @Inject constructor(
    private val arguments: Arguments,
    private val stopDetailsRetriever: UiStopDetailsRetriever,
    private val servicesItemsRetriever: UiServicesItemsRetriever
) : UiContentRetriever {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val uiContentFlow get() = arguments
        .stopIdentifierFlow
        .flatMapLatest(::getUiContentForStopIdentifier)

    private fun getUiContentForStopIdentifier(stopIdentifier: StopIdentifier?): Flow<UiContent> {
        return if (stopIdentifier != null) {
            stopDetailsRetriever
                .getUiStopDetailsFlow(stopIdentifier)
                .combine(
                    servicesItemsRetriever.getUiServicesItemsFlow(stopIdentifier),
                    ::createUiContent
                )
                .onStart { emit(UiContent.InProgress) }
        } else {
            flowOf(UiContent.NoStopDetailsError)
        }
    }

    private fun createUiContent(
        stopDetails: UiStopDetails?,
        serviceItems: List<UiServicesItem>
    ): UiContent {
        return if (stopDetails != null) {
            UiContent.Content(
                stopDetails = stopDetails,
                servicesItems = serviceItems.toImmutableList()
            )
        } else {
            UiContent.NoStopDetailsError
        }
    }
}
