/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.androidcore.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.dagger.EdinburghModule
import uk.org.rivernile.android.bustracker.core.services.AndroidServiceColourProvider
import uk.org.rivernile.android.bustracker.core.services.ServiceColourProvider
import uk.org.rivernile.android.bustracker.core.startup.CleanUpTask
import uk.org.rivernile.android.bustracker.core.startup.EdinburghCleanUpTask

/**
 * Any dependencies which are flavour-specific should go here.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module(includes = [ EdinburghModule::class ])
internal interface FlavourModule {

    @Suppress("unused")
    @Binds
    fun bindCleanUpTask(edinburghCleanUpTask: EdinburghCleanUpTask): CleanUpTask

    @Suppress("unused")
    @Binds
    fun bindServiceColourProvider(
            androidServiceColourProvider: AndroidServiceColourProvider): ServiceColourProvider
}