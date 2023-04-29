/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.daos

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.settings.FavouritesContract
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * This is an Android-specific implementation of [FavouritesDao] which uses a
 * [android.content.ContentProvider] to communicate with the database.
 *
 * @param context The application [Context].
 * @param contract The contract for talking with the favourites table.
 * @param ioDispatcher The IO [CoroutineDispatcher].
 * @author Niall Scott
 */
@Singleton
internal class AndroidFavouritesDao @Inject constructor(
        private val context: Context,
        private val contract: FavouritesContract,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) : FavouritesDao {

    private val listeners = mutableListOf<FavouritesDao.OnFavouritesChangedListener>()
    private val observer = Observer()

    @Synchronized
    override fun addOnFavouritesChangedListener(
            listener: FavouritesDao.OnFavouritesChangedListener) {
        listeners.apply {
            add(listener)

            if (size == 1) {
                context.contentResolver.registerContentObserver(contract.getContentUri(), true,
                        observer)
            }
        }
    }

    @Synchronized
    override fun removeOnFavouritesChangedListener(
            listener: FavouritesDao.OnFavouritesChangedListener) {
        listeners.apply {
            remove(listener)

            if (isEmpty()) {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    override suspend fun isStopAddedAsFavourite(stopCode: String): Boolean {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(FavouritesContract.COUNT),
                        "${FavouritesContract.STOP_CODE} = ?",
                        arrayOf(stopCode),
                        null,
                        cancellationSignal)?.use {
                            // Fill Cursor window.
                            it.count

                            it.moveToFirst() && it.getInt(
                                    it.getColumnIndex(FavouritesContract.COUNT)) > 0
                        } ?: false

                continuation.resume(result)
            }
        }
    }

    override suspend fun addFavouriteStop(favouriteStop: FavouriteStop) {
        withContext(ioDispatcher) {
            context.contentResolver.insert(
                    contract.getContentUri(),
                    mapToContentValues(favouriteStop))
        }
    }

    override suspend fun updateFavouriteStop(favouriteStop: FavouriteStop) {
        withContext(ioDispatcher) {
            val values = ContentValues().apply {
                put(FavouritesContract.STOP_NAME, favouriteStop.stopName)
            }
            val rowUri = ContentUris.withAppendedId(contract.getContentUri(), favouriteStop.id)

            context.contentResolver.update(rowUri, values, null, null)
        }
    }

    override suspend fun removeFavouriteStop(stopCode: String) {
        withContext(ioDispatcher) {
            context.contentResolver.delete(
                    contract.getContentUri(),
                    "${FavouritesContract.STOP_CODE} = ?",
                    arrayOf(stopCode))
        }
    }

    override suspend fun getFavouriteStop(stopCode: String): FavouriteStop? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                FavouritesContract.ID,
                                FavouritesContract.STOP_NAME),
                        "${FavouritesContract.STOP_CODE} = ?",
                        arrayOf(stopCode),
                        null,
                        cancellationSignal)?.use {
                    // Fill Cursor window.
                    it.count

                    if (it.moveToFirst()) {
                        val idColumn = it.getColumnIndex(FavouritesContract.ID)
                        val stopNameColumn = it.getColumnIndex(FavouritesContract.STOP_NAME)

                        FavouriteStop(
                                it.getLong(idColumn),
                                stopCode,
                                it.getString(stopNameColumn))
                    } else {
                        null
                    }
                }

                continuation.resume(result)
            }
        }
    }

    override suspend fun getFavouriteStops(): List<FavouriteStop>? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                FavouritesContract.ID,
                                FavouritesContract.STOP_CODE,
                                FavouritesContract.STOP_NAME),
                        null,
                        null,
                        "${FavouritesContract.STOP_NAME} ASC",
                        cancellationSignal)?.use {
                    val count = it.count

                    if (count > 0) {
                        val result = ArrayList<FavouriteStop>(count)
                        val idColumn = it.getColumnIndex(FavouritesContract.ID)
                        val stopCodeColumn = it.getColumnIndex(FavouritesContract.STOP_CODE)
                        val stopNameColumn = it.getColumnIndex(FavouritesContract.STOP_NAME)

                        while (it.moveToNext()) {
                            result.add(mapToFavouriteStop(
                                    it,
                                    idColumn,
                                    stopCodeColumn,
                                    stopNameColumn))
                        }

                        result
                    } else {
                        null
                    }
                }

                continuation.resume(result)
            }
        }
    }

    /**
     * Given a [Cursor], map the current row to a [FavouriteStop].
     *
     * This method does not mutate the [Cursor] state in any wat.
     *
     * @param cursor The [Cursor] pointing to the row we are interested in.
     * @param idColumn The column index containing the row ID.
     * @param stopCodeColumn The column index containing the stop code.
     * @param stopNameColumn The column index containing the stop name.
     * @return A [FavouriteStop] containing the data at the current [Cursor] row.
     */
    private fun mapToFavouriteStop(
            cursor: Cursor,
            idColumn: Int,
            stopCodeColumn: Int,
            stopNameColumn: Int) =
            FavouriteStop(
                    cursor.getLong(idColumn),
                    cursor.getString(stopCodeColumn),
                    cursor.getString(stopNameColumn))

    /**
     * Given a [FavouriteStop], map it to a [ContentValues] object so it can be interested in to
     * the database.
     *
     * @param favouriteStop The [FavouriteStop] being added to the database.
     * @return The [FavouriteStop] as a [ContentValues] object.
     */
    private fun mapToContentValues(favouriteStop: FavouriteStop) =
            ContentValues().apply {
                put(FavouritesContract.STOP_CODE, favouriteStop.stopCode)
                put(FavouritesContract.STOP_NAME, favouriteStop.stopName)
            }

    /**
     * For all of the currently registers listeners, dispatch a favourite change to them.
     */
    private fun dispatchOnFavouritesChangedListeners() {
        listeners.forEach { listener ->
            listener.onFavouritesChanged()
        }
    }

    /**
     * This inner class is used as the [ContentObserver] for observing changes to favourites.
     */
    private inner class Observer : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun deliverSelfNotifications() = true

        override fun onChange(selfChange: Boolean) {
            dispatchOnFavouritesChangedListeners()
        }
    }
}