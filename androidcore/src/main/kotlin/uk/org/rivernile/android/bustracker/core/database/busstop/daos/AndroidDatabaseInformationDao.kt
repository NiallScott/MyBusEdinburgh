/*
 * Copyright (C) 2019 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import android.content.Context
import android.database.ContentObserver
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.DatabaseInformationContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.DatabaseMetadata
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * This is an Android concrete implementation of the [DatabaseInformationDao].
 *
 * @param context The application [Context].
 * @param contract The database contract, so we know how to talk to it.
 * @param ioDispatcher The IO [CoroutineDispatcher].
 * @author Niall Scott
 */
@Singleton
internal class AndroidDatabaseInformationDao @Inject constructor(
        private val context: Context,
        private val contract: DatabaseInformationContract,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher): DatabaseInformationDao {

    override fun getTopologyId() = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(DatabaseInformationContract.CURRENT_TOPOLOGY_ID),
            null,
            null,
            null)?.use {
        // Fill the Cursor window.
        it.count

        if (it.moveToFirst()) {
            it.getString(it.getColumnIndexOrThrow(DatabaseInformationContract.CURRENT_TOPOLOGY_ID))
        } else {
            null
        }
    }

    override val databaseMetadataFlow get() = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {

            override fun deliverSelfNotifications() = true

            override fun onChange(selfChange: Boolean) {
                launch {
                    getAndSendDatabaseMetadata()
                }
            }
        }

        context.contentResolver.registerContentObserver(contract.getContentUri(), false, observer)
        getAndSendDatabaseMetadata()

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    /**
     * Get the [DatabaseMetadata] from the database and send it to the channel.
     */
    private suspend fun ProducerScope<DatabaseMetadata?>.getAndSendDatabaseMetadata() {
        channel.send(getDatabaseMetadata())
    }

    /**
     * Get the [DatabaseMetadata].
     *
     * @return The [DatabaseMetadata]. Will be `null` if there was an error or it does not exist.
     */
    @VisibleForTesting
    internal suspend fun getDatabaseMetadata(): DatabaseMetadata? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                DatabaseInformationContract.LAST_UPDATE_TIMESTAMP,
                                DatabaseInformationContract.CURRENT_TOPOLOGY_ID),
                        null,
                        null,
                        null,
                        cancellationSignal)?.use {
                    // Fill the Cursor window.
                    it.count

                    if (it.moveToFirst()) {
                        val lastUpdateTimestampColumn = it.getColumnIndexOrThrow(
                                DatabaseInformationContract.LAST_UPDATE_TIMESTAMP)
                        val currentTopologyIdColumn = it.getColumnIndexOrThrow(
                                DatabaseInformationContract.CURRENT_TOPOLOGY_ID)

                        DatabaseMetadata(
                                it.getLong(lastUpdateTimestampColumn),
                                it.getString(currentTopologyIdColumn))
                    } else {
                        null
                    }
                }

                continuation.resume(result)
            }
        }
    }
}