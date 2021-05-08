/*
 * Copyright (C) 2019 - 2021 Niall 'Rivernile' Scott
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

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import uk.org.rivernile.android.bustracker.core.extensions.closeSafely
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This object represents a single transfer sessions for a file download. This instance should only
 * be used once. To re-attempt the same transfer, a new instance should be obtained from
 * [FileDownloader].
 *
 * @param okHttpClient The HTTP implementation.
 * @param url The URL of the file to download.
 * @param toLocation A [File] describing the intended location of the downloaded file.
 * @author Niall Scott
 */
class FileDownloadSession internal constructor(
        private val okHttpClient: OkHttpClient,
        private val url: String,
        private val toLocation: File) {

    private var call: Call? = null
    private val hasRun = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)

    /**
     * Using the parameters supplied to this session instance, download a file to a [File] location.
     *
     * @throws FileDownloadException When there was an issue downloading the file.
     * @throws IllegalStateException When attempting to use this instance more than once.
     */
    @Throws(FileDownloadException::class)
    fun downloadFile() {
        if (!hasRun.compareAndSet(false, true)) {
            throw IllegalStateException("Do not reuse this object. Obtain a new instance from " +
                    "FileDownloader.")
        }

        ensureNotCancelled(null)

        val fileSink = createFileSink()
        val call = createCall()
        this.call = call

        ensureNotCancelled {
            call.cancel()
            fileSink.closeSafely()
        }

        try {
            val response = call.execute()

            if (response.isSuccessful) {
                response.body?.let {
                    fileSink.writeAll(it.source())
                } ?: throw FileDownloadException("No body received from server.")
            } else {
                throw FileDownloadException("Received error response from server. Response code " +
                        "= ${response.code}")
            }
        } catch (e: IOException) {
            throw FileDownloadException(e)
        } finally {
            fileSink.closeSafely()
        }
    }

    /**
     * Cancel an in-flight transfer if there is one. Otherwise, prevent a new transfer from
     * beginning.
     */
    fun cancel() {
        cancelled.set(true)
        call?.cancel()
    }

    /**
     * Ensure the session is not cancelled. If it is, invoke the `cleanupBlock`.
     *
     * @param cleanupBlock A block of code to execute to perform cleanup if this session has been
     * cancelled.
     * @throws FileDownloadException When the session has been previously cancelled.
     */
    @Throws(FileDownloadException::class)
    private fun ensureNotCancelled(cleanupBlock: (() -> Unit)?) {
        if (cancelled.get()) {
            cleanupBlock?.invoke()
            throw FileDownloadException("Session cancelled.")
        }
    }

    /**
     * Create the [File] [okio.Sink] to output the HTTP response body to.
     *
     * @return The [File] [okio.Sink] to output the HTTP response body to.
     * @throws FileDownloadException When there was an issue opening the file path.
     */
    @Throws(FileDownloadException::class)
    private fun createFileSink() = try {
        toLocation.sink().buffer()
    } catch (e: IOException) {
        throw FileDownloadException(e)
    }

    /**
     * Create the Okhttp [Call] to initiate the request to the endpoint.
     *
     * @return The Okhttp [Call] object.
     */
    private fun createCall() = Request.Builder()
            .url(url)
            .build()
            .run(okHttpClient::newCall)
}