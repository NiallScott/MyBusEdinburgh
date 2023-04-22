/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.http

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.buffer
import okio.sink
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import java.io.File
import javax.inject.Inject
import javax.net.SocketFactory

/**
 * This class is used to download files.
 *
 * @param okHttpClient The HTTP implementation.
 * @param exceptionLogger Used to log exceptions.
 * @param ioDispatcher The IO [CoroutineDispatcher].
 * @author Niall Scott
 */
class FileDownloader @Inject internal constructor(
    private val okHttpClient: OkHttpClient,
    private val exceptionLogger: ExceptionLogger,
    @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Download a file from the given [url] to [toLocation]. If [socketFactory] is not-`null`, this
     * is used to provide what socket the connection should be made from, i.e. network interface
     * binding.
     *
     * @param url The URL of the file to download.
     * @param toLocation The location where the file should be downloaded to.
     * @param socketFactory An optional [SocketFactory] to create sockets. This is used for network
     * interface binding.
     * @return The response from performing this download, to indicate status.
     */
    suspend fun downloadFile(
            url: String,
            toLocation: File,
            socketFactory: SocketFactory? = null): FileDownloadResponse {
        val okHttpClient = socketFactory?.let {
            this.okHttpClient.newBuilder()
                    .socketFactory(it)
                    .build()
        } ?: this.okHttpClient

        return withContext(ioDispatcher) {
            try {
                toLocation.sink().buffer()
            } catch (e: IOException) {
                exceptionLogger.log(e)
                return@withContext FileDownloadResponse.Error.IoError(e)
            }.use { fileSink ->
                val request = Request.Builder()
                        .url(url)
                        .build()

                try {
                    val response = okHttpClient.newCall(request).executeAsync()

                    if (response.isSuccessful) {
                        response.body?.let {
                            fileSink.writeAll(it.source())
                            FileDownloadResponse.Success
                        } ?: FileDownloadResponse.Error.ServerError
                    } else {
                        FileDownloadResponse.Error.ServerError
                    }
                } catch (e: IOException) {
                    exceptionLogger.log(e)
                    FileDownloadResponse.Error.IoError(e)
                }
            }
        }
    }
}