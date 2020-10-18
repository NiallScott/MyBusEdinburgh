/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.dagger

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForGlobalCoroutineScope
import uk.org.rivernile.android.bustracker.core.di.ForShortBackgroundTasks
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * A module for providing fake implementations of core module resources.
 *
 * @param backgroundExecutor An alternative background [Executor] instance. Defaults to
 * [Executors.newCachedThreadPool].
 * @author Niall Scott
 */
@Module(includes = [
    FakeCoreModule.Bindings::class
])
class FakeCoreModule(
        private val backgroundExecutor: Executor = Executors.newCachedThreadPool(),
        @ForGlobalCoroutineScope private val globalCoroutineScope: CoroutineScope = GlobalScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher =
                Dispatchers.Default) {

    @Provides
    @Singleton
    @ForShortBackgroundTasks
    fun provideBackgroundExecutor() = backgroundExecutor

    @Provides
    @Singleton
    @ForGlobalCoroutineScope
    fun provideGlobalCoroutineScope() = globalCoroutineScope

    @Provides
    @Singleton
    @ForDefaultDispatcher
    fun provideDefaultDispatcher() = defaultDispatcher

    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindApplicationContext(application: Application): Context
    }
}