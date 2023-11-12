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

package uk.org.rivernile.android.bustracker.core.startup

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope

/**
 * An app [Initializer] which performs cleanup of data from previous application versions.
 *
 * @author Niall Scott
 */
@Suppress("unused")
class EdinburghCleanUpInitialiser : Initializer<Unit> {

    override fun create(context: Context) {
        val hiltEntryPoint = EarlyEntryPoints.get(
            context,
            EdinburghCleanUpInitialiserEntryPoint::class.java)

        hiltEntryPoint.applicationCoroutineScope.launch {
            hiltEntryPoint.edinburghCleanUpTask.performCleanUp()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface EdinburghCleanUpInitialiserEntryPoint {

        val edinburghCleanUpTask: EdinburghCleanUpTask

        @get:ForApplicationCoroutineScope
        val applicationCoroutineScope: CoroutineScope
    }
}