/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject

/**
 * This class is used to create new [FileDownloadSession]s.
 *
 * @param okHttpClient The HTTP implementation.
 * @author Niall Scott
 */
class FileDownloader @Inject constructor(private val okHttpClient: OkHttpClient) {

    /**
     * Create a new [FileDownloadSession] to download a file to a location.
     *
     * @param url The URL of the file, as a [String].
     * @param toLocation A [File] object describing the location to download the file to.
     */
    fun createFileDownloadSession(url: String, toLocation: File)
            = FileDownloadSession(okHttpClient, url, toLocation)
}