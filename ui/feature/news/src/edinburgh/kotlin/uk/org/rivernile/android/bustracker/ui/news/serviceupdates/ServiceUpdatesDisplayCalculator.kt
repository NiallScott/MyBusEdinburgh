/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import javax.inject.Inject

/**
 * An implementation which calculates the new [ServiceUpdatesDisplay] based on the state of the
 * previous [ServiceUpdatesDisplay] and the newly provided [UiServiceUpdatesResult].
 *
 * @author Niall Scott
 */
internal interface ServiceUpdatesDisplayCalculator {

    /**
     * Calculate the new [ServiceUpdatesDisplay] based on [previousDisplay] and the new [result].
     *
     * The following table encapsulates the calculation logic;
     *
     * | Old state  | Loaded [UiServiceUpdatesResult] | Calculation result       |
     * |------------|---------------------------------|--------------------------|
     * | InProgress | InProgress                      | InProgress               |
     * | InProgress | Error                           | Error                    |
     * | InProgress | Success                         | Populated                |
     * | Error      | InProgress                      | InProgress               |
     * | Error      | Error                           | Error                    |
     * | Error      | Success                         | Populated                |
     * | Populated  | InProgress                      | Populated (with refresh) |
     * | Populated  | Error                           | Populated (with error)   |
     * | Populated  | Populated                       | Populated                |
     *
     * (When the [UiServiceUpdatesResult] is [UiServiceUpdatesResult.Success] but there are
     * no items to display, i.e. when there are no Service Updates, then the calculation result
     * becomes Error, containing [UiError.EMPTY])
     *
     * The general design of the logic is to always prioritise showing the user some sort of loaded
     * data. So if there was a previous success case but on this loading attempt there was a
     * transient error, we don't want to scrub the data which was loaded before. Instead, we show
     * the Populated content, but we add the error to the Populated object so a more discreet error
     * UI can be shown instead of entirely replacing the content.
     *
     * @param previousDisplay The previously loaded [ServiceUpdatesDisplay] to use as a context for
     * calculating the new [previousDisplay].
     * @param result The newly fetched data to be displayed.
     * @return The newly calculated [ServiceUpdatesDisplay].
     */
    fun calculateServiceUpdatesDisplayForDiversions(
        previousDisplay: ServiceUpdatesDisplay<UiDiversion>,
        result: UiServiceUpdatesResult<UiDiversion>
    ): ServiceUpdatesDisplay<UiDiversion>

    /**
     * Calculate the new [ServiceUpdatesDisplay] based on [previousDisplay] and the new [result].
     *
     * The following table encapsulates the calculation logic;
     *
     * | Old state  | Loaded [UiServiceUpdatesResult] | Calculation result       |
     * |------------|---------------------------------|--------------------------|
     * | InProgress | InProgress                      | InProgress               |
     * | InProgress | Error                           | Error                    |
     * | InProgress | Success                         | Populated                |
     * | Error      | InProgress                      | InProgress               |
     * | Error      | Error                           | Error                    |
     * | Error      | Success                         | Populated                |
     * | Populated  | InProgress                      | Populated (with refresh) |
     * | Populated  | Error                           | Populated (with error)   |
     * | Populated  | Populated                       | Populated                |
     *
     * (When the [UiServiceUpdatesResult] is [UiServiceUpdatesResult.Success] but there are
     * no items to display, i.e. when there are no Service Updates, then the calculation result
     * becomes Error, containing [UiError.EMPTY])
     *
     * The general design of the logic is to always prioritise showing the user some sort of loaded
     * data. So if there was a previous success case but on this loading attempt there was a
     * transient error, we don't want to scrub the data which was loaded before. Instead, we show
     * the Populated content, but we add the error to the Populated object so a more discreet error
     * UI can be shown instead of entirely replacing the content.
     *
     * @param previousDisplay The previously loaded [ServiceUpdatesDisplay] to use as a context for
     * calculating the new [previousDisplay].
     * @param result The newly fetched data to be displayed.
     * @return The newly calculated [ServiceUpdatesDisplay].
     */
    fun calculateServiceUpdatesDisplayForIncidents(
        previousDisplay: ServiceUpdatesDisplay<UiIncident>,
        result: UiServiceUpdatesResult<UiIncident>
    ): ServiceUpdatesDisplay<UiIncident>
}

internal class RealServiceUpdatesDisplayCalculator @Inject constructor()
    : ServiceUpdatesDisplayCalculator {

    override fun calculateServiceUpdatesDisplayForDiversions(
        previousDisplay: ServiceUpdatesDisplay<UiDiversion>,
        result: UiServiceUpdatesResult<UiDiversion>
    ): ServiceUpdatesDisplay<UiDiversion> {
        return calculateServiceUpdatesDisplay(previousDisplay, result)
    }

    override fun calculateServiceUpdatesDisplayForIncidents(
        previousDisplay: ServiceUpdatesDisplay<UiIncident>,
        result: UiServiceUpdatesResult<UiIncident>
    ): ServiceUpdatesDisplay<UiIncident> {
        return calculateServiceUpdatesDisplay(previousDisplay, result)
    }

    private fun <T : UiServiceUpdate> calculateServiceUpdatesDisplay(
        previousDisplay: ServiceUpdatesDisplay<T>,
        result: UiServiceUpdatesResult<T>
    ): ServiceUpdatesDisplay<T> {
        return when (result) {
            is UiServiceUpdatesResult.InProgress -> handleProgress(previousDisplay)
            is UiServiceUpdatesResult.Success -> handleSuccess(result)
            is UiServiceUpdatesResult.Error -> handleError(previousDisplay, result)
        }
    }

    private fun <T : UiServiceUpdate> handleProgress(
        previousDisplay: ServiceUpdatesDisplay<T>
    ): ServiceUpdatesDisplay<T> {
        return when (previousDisplay) {
            is ServiceUpdatesDisplay.InProgress, is ServiceUpdatesDisplay.Error ->
                ServiceUpdatesDisplay.InProgress
            is ServiceUpdatesDisplay.Populated -> previousDisplay.copy(isRefreshing = true)
        }
    }

    private fun <T : UiServiceUpdate> handleSuccess(
        result: UiServiceUpdatesResult.Success<T>
    ): ServiceUpdatesDisplay<T> {
        return result
            .serviceUpdates
            ?.ifEmpty { null }
            ?.let {
                ServiceUpdatesDisplay.Populated(
                    isRefreshing = false,
                    items = it,
                    error = null,
                    loadTimeMillis = result.loadTimeMillis
                )
            }
            ?: ServiceUpdatesDisplay.Error(
                error = UiError.EMPTY
            )
    }

    private fun <T : UiServiceUpdate> handleError(
        previousDisplay: ServiceUpdatesDisplay<T>,
        result: UiServiceUpdatesResult.Error
    ): ServiceUpdatesDisplay<T> {
        return if (previousDisplay is ServiceUpdatesDisplay.Populated) {
            previousDisplay.copy(
                isRefreshing = false,
                error = result.error
            )
        } else {
            ServiceUpdatesDisplay.Error(
                error = result.error
            )
        }
    }
}