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

package uk.org.rivernile.android.bustracker.ui.serviceschooser.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import uk.org.rivernile.android.bustracker.ui.serviceschooser.Arguments
import uk.org.rivernile.android.bustracker.ui.serviceschooser.OperatorAndServicesFetcher
import uk.org.rivernile.android.bustracker.ui.serviceschooser.RealArguments
import uk.org.rivernile.android.bustracker.ui.serviceschooser.RealOperatorAndServicesFetcher
import uk.org.rivernile.android.bustracker.ui.serviceschooser.RealState
import uk.org.rivernile.android.bustracker.ui.serviceschooser.RealUiContentFetcher
import uk.org.rivernile.android.bustracker.ui.serviceschooser.State
import uk.org.rivernile.android.bustracker.ui.serviceschooser.UiContentFetcher

/**
 * A module for supplying dependencies for
 * [uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserViewModel].
 *
 * @author Niall Scott
 */
@InstallIn(ViewModelComponent::class)
@Module
internal interface ServicesChooserViewModelModule {

    @Binds
    fun bindArguments(realArguments: RealArguments): Arguments

    @Binds
    fun bindOperatorAndServicesFetcher(
        realOperatorAndServicesFetcher: RealOperatorAndServicesFetcher
    ): OperatorAndServicesFetcher

    @Binds
    fun bindState(realState: RealState): State

    @Binds
    fun bindUiContentFetcher(realUiContentFetcher: RealUiContentFetcher): UiContentFetcher
}
