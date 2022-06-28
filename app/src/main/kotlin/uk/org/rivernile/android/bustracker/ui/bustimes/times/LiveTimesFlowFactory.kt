/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This class creates a [Flow] which emits [UiTransformedResult] items by combining the [Flow]s
 * created by [LiveTimesLoader] and [LiveTimesTransform].
 *
 * @param liveTimesLoader Provides a [Flow] which loads live times.
 * @param liveTimesTransform Performs transformations upon the live times [Flow] so it is in the
 * correct state for the UI.
 * @author Niall Scott
 */
class LiveTimesFlowFactory @Inject constructor(
        private val liveTimesLoader: LiveTimesLoader,
        private val liveTimesTransform: LiveTimesTransform) {

    /**
     * Create a new [Flow] which emits [UiTransformedResult] items.
     *
     * This combines the live times source [Flow] with a [Flow] which performs transformations upon
     * the data so it is in the correct state for the UI.
     *
     * @param stopCodeFlow A [Flow] of stop codes.
     * @param expandedServicesFlow A [Flow] of the expanded services.
     * @param refreshTriggerFlow A [Flow] where refresh request items are emitted from.
     * @return A [Flow] which emits [UiTransformedResult] items, based on current state.
     */
    fun createLiveTimesFlow(
            stopCodeFlow: Flow<String?>,
            expandedServicesFlow: Flow<Set<String>>,
            refreshTriggerFlow: Flow<Unit>): Flow<UiTransformedResult> {
        val liveTimesFlow = liveTimesLoader.loadLiveTimesFlow(stopCodeFlow, refreshTriggerFlow)

        return liveTimesTransform.getLiveTimesTransformFlow(liveTimesFlow, expandedServicesFlow)
    }
}