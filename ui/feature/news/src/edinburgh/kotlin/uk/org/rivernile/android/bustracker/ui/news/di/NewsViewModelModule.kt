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

package uk.org.rivernile.android.bustracker.ui.news.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.RealServiceUpdatesDisplayCalculator
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.RealServiceUpdatesFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.RealUiContentFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.RealServiceUpdatesDisplayFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ServiceUpdatesDisplayCalculator
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ServiceUpdatesFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiContentFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ServiceUpdatesDisplayFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.DiversionsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.RealDiversionsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.IncidentsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.RealIncidentsViewModelState

/**
 * A [Module] which provides dependencies for
 * [uk.org.rivernile.android.bustracker.ui.news.NewsViewModel].
 *
 * @author Niall Scott
 */
@InstallIn(ViewModelComponent::class)
@Module
internal interface NewsViewModelModule {

    @Suppress("unused")
    @Binds
    fun bindDiversionsViewModelState(
        realDiversionsViewModelState: RealDiversionsViewModelState
    ): DiversionsViewModelState

    @Suppress("unused")
    @Binds
    fun bindIncidentsViewModelState(
        realIncidentsViewModelState: RealIncidentsViewModelState
    ): IncidentsViewModelState

    @Suppress("unused")
    @Binds
    fun bindServiceUpdatesDisplayCalculator(
        realServiceUpdatesDisplayCalculator: RealServiceUpdatesDisplayCalculator
    ): ServiceUpdatesDisplayCalculator

    @Suppress("unused")
    @Binds
    fun bindServiceUpdatesFetcher(
        realServiceUpdatesFetcher: RealServiceUpdatesFetcher
    ): ServiceUpdatesFetcher

    @Suppress("unused")
    @Binds
    fun bindUiContentFetcher(realUiContentFetcher: RealUiContentFetcher): UiContentFetcher

    @Suppress("unused")
    @Binds
    fun bindUiServiceUpdatesFetcher(
        realUiContentFetcher: RealServiceUpdatesDisplayFetcher
    ): ServiceUpdatesDisplayFetcher
}