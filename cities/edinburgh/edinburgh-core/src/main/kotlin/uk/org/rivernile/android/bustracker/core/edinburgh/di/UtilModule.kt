/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.edinburgh.di

import com.davekoelle.alphanum.AlphanumComparator
import dagger.Binds
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.config.BuildConfiguration
import uk.org.rivernile.android.bustracker.core.config.EdinburghBuildConfiguration
import uk.org.rivernile.android.bustracker.core.livetimes.EdinburghIsNightServiceDetector
import uk.org.rivernile.android.bustracker.core.livetimes.IsNightServiceDetector
import uk.org.rivernile.android.bustracker.core.services.EdinburghServiceColourOverride
import uk.org.rivernile.android.bustracker.core.services.ServiceColourOverride

/**
 * This [Module] provides utility dependencies for the Edinburgh library.
 *
 * @author Niall Scott
 */
@Module
internal interface UtilModule {

    @Suppress("unused")
    @Binds
    fun bindIsNightServiceDetector(
        edinburghIsNightServiceDetector: EdinburghIsNightServiceDetector
    ): IsNightServiceDetector

    @Suppress("unused")
    @Binds
    fun bindBuildConfiguration(
        edinburghBuildConfiguration: EdinburghBuildConfiguration
    ): BuildConfiguration

    @Suppress("unused")
    @Binds
    fun bindServiceColourOverride(
        edinburghServiceColourOverride: EdinburghServiceColourOverride
    ): ServiceColourOverride

    companion object {

        @Provides
        fun provideServiceComparator(): Comparator<String> = AlphanumComparator()
    }
}