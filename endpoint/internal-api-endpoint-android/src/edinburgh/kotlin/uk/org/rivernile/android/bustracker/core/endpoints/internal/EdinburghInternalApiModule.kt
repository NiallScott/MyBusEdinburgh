/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.internal

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.endpoint.internal.BuildConfig
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApi
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiAppName
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiKey
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiSchemaName

/**
 * This [Module] provides dependencies for the Edinburgh version of the internal API.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class EdinburghInternalApiModule {

    @Provides
    @ForInternalApi
    fun provideApiBaseUrl(): String = "http://edinb.us/api/"

    @Provides
    @ForInternalApiKey
    fun provideApiKey(): String = BuildConfig.INTERNAL_API_KEY

    @Provides
    @ForInternalApiAppName
    fun provideApiAppName(): String = "MBE"

    @Provides
    @ForInternalApiSchemaName
    fun provideApiSchemaName(): String = "MBE_10"
}