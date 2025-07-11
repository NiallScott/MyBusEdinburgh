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

package uk.org.rivernile.android.bustracker.core.database.busstop

import java.io.File
import java.io.FileInputStream

/**
 * This represents a bus stop database file, as an abstraction away from operating directly on the
 * real filesystem, so that file operations can be mocked or faked at testing time.
 *
 * @author Niall Scott
 */
internal interface BusStopDatabaseFile {

    /**
     * See [File.getAbsolutePath].
     *
     * @see File.getAbsolutePath
     */
    val absolutePath: String

    /**
     * See [File.delete].
     *
     * @return See [File.delete].
     * @see File.delete
     */
    fun delete(): Boolean

    /**
     * See [File.exists].
     *
     * @return See [File.exists].
     * @see File.exists
     */
    fun exists(): Boolean

    /**
     * See [File.inputStream].
     *
     * @return See [File.inputStream].
     * @see File.inputStream
     */
    fun inputStream(): FileInputStream

    /**
     * See [File.renameTo].
     *
     * @param destination See [File.renameTo].
     * @return See [File.renameTo].
     * @see File.renameTo
     */
    fun renameTo(destination: File): Boolean
}

/**
 * From the current [File] object, convert this in to a [BusStopDatabaseFile] object.
 */
internal fun File.toBusStopDatabaseFile(): BusStopDatabaseFile = JvmBusStopDatabaseFile(this)

@JvmInline
private value class JvmBusStopDatabaseFile(
    private val file: File
) : BusStopDatabaseFile {

    override val absolutePath: String get() = file.absolutePath

    override fun delete() = file.delete()

    override fun exists() = file.exists()

    override fun inputStream() = file.inputStream()

    override fun renameTo(destination: File) = file.renameTo(destination)
}