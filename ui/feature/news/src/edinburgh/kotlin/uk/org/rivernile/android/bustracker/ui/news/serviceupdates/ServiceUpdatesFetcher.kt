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

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdateRepository
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult
import javax.inject.Inject

/**
 * A mechanism for getting and refreshing [ServiceUpdatesResult]s.
 *
 * @author Niall Scott
 */
internal interface ServiceUpdatesFetcher : AutoCloseable {

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits [ServiceUpdatesResult]s. The initial
     * subscription to this may already have loaded data or may cause a new load to happen. To
     * cause new data loads (i.e. to refresh the data), call [refresh].
     */
    val serviceUpdatesFlow: Flow<ServiceUpdatesResult>

    /**
     * Trigger a refresh on the Service Updates data. The new data will be emitted from
     * [serviceUpdatesFlow]. If this method is called while a load is taking place, it will cause
     * the current load to be aborted a new fresh loading attempt is performed.
     */
    fun refresh()
}

@ViewModelScoped
internal class RealServiceUpdatesFetcher @Inject constructor(
    private val serviceUpdateRepository: ServiceUpdateRepository,
    @ForViewModelCoroutineScope private val viewModelCoroutineScope: CoroutineScope
) : ServiceUpdatesFetcher {

    private val refreshChannel = Channel<Unit>(capacity = 1).apply { trySend(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val serviceUpdatesFlow = refreshChannel
        .receiveAsFlow()
        .flatMapLatest { serviceUpdateRepository.serviceUpdatesFlow }
        .shareIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.Lazily,
            replay = 1
        )

    override fun refresh() {
        refreshChannel.trySend(Unit)
    }

    override fun close() {
        refreshChannel.close()
    }
}