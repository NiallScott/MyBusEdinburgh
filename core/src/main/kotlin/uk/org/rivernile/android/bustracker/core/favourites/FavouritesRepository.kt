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

package uk.org.rivernile.android.bustracker.core.favourites

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.settings.daos.FavouritesDao
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access favourites data.
 *
 * @param favouritesDao The DAO to access the favourites data store.
 * @param ioDispatcher The [CoroutineDispatcher] to perform IO operations on.
 * @author Niall Scott
 */
@Singleton
class FavouritesRepository @Inject internal constructor(
        private val favouritesDao: FavouritesDao,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Get a [Flow] which returns whether the given `stopCode` is added as a favourite or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the favourite status of the given `stopCode`.
     */
    @ExperimentalCoroutinesApi
    fun isStopAddedAsFavouriteFlow(stopCode: String): Flow<Boolean> = callbackFlow {
        val listener = object : FavouritesDao.OnFavouritesChangedListener {
            override fun onFavouritesChanged() {
                launch {
                    getAndSendIsStopAddedAsFavourite(channel, stopCode)
                }
            }
        }

        favouritesDao.addOnFavouritesChangedListener(listener)
        getAndSendIsStopAddedAsFavourite(channel, stopCode)

        awaitClose {
            favouritesDao.removeOnFavouritesChangedListener(listener)
        }
    }

    /**
     * A suspended function which obtains the favourite status of the given `stopCode` and then
     * sends it to the given `channel`.
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     * @param stopCode The `stopCode` to obtain the favourite status for.
     */
    private suspend fun getAndSendIsStopAddedAsFavourite(
            channel: SendChannel<Boolean>,
            stopCode: String) = withContext(ioDispatcher) {
        channel.send(favouritesDao.isStopAddedAsFavourite(stopCode))
    }
}