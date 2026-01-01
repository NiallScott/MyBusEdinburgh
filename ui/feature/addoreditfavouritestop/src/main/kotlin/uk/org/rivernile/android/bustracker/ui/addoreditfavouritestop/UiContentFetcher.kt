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

package uk.org.rivernile.android.bustracker.ui.addoreditfavouritestop

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import javax.inject.Inject

/**
 * Fetches content and exposes this as [UiContent] objects.
 *
 * @author Niall Scott
 */
internal interface UiContentFetcher {

    /**
     * A [Flow] which emits [UiContent] objects which reflect the current content state.
     */
    val uiContentFlow: Flow<UiContent>
}

internal class RealUiContentFetcher @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val favouritesRepository: FavouritesRepository,
    private val busStopsRepository: BusStopsRepository
) : UiContentFetcher {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val uiContentFlow get() = arguments
        .stopCodeFlow
        .flatMapLatest(::loadUiContent)
        .distinctUntilChanged()

    private fun loadUiContent(stopCode: String?): Flow<UiContent> {
        return if (!stopCode.isNullOrBlank()) {
            loadFavouriteStopAndDetails(stopCode = stopCode)
                .onStart { emit(UiContent.InProgress) }
        } else {
            flowOf(UiContent.InProgress)
        }
    }

    private fun loadFavouriteStopAndDetails(stopCode: String) = combine(
        favouritesRepository.getFavouriteStopFlow(stopCode),
        busStopsRepository.getNameForStopFlow(stopCode),
        state.stopNameTextFlow
    ) { favouriteStop, stopName, editableStopNameText ->
        createUiContent(
            stopCode = stopCode,
            favouriteStop = favouriteStop,
            stopName = stopName,
            editableStopNameText = editableStopNameText
        )
    }

    private fun createUiContent(
        stopCode: String,
        favouriteStop: FavouriteStop?,
        stopName: StopName?,
        editableStopNameText: String?
    ): UiContent {
        return if (favouriteStop != null) {
            UiContent.Mode.Edit(
                stopCode = stopCode,
                stopName = stopName?.toUiStopName(),
                isPositiveButtonEnabled = isStopNameValid(editableStopNameText),
                savedName = favouriteStop.stopName
            )
        } else {
            UiContent.Mode.Add(
                stopCode = stopCode,
                stopName = stopName?.toUiStopName(),
                isPositiveButtonEnabled = isStopNameValid(editableStopNameText)
            )
        }
    }

    private fun StopName.toUiStopName(): UiStopName {
        return UiStopName(
            name = name,
            locality = locality
        )
    }
}
