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
 * A fake [BusStopDatabaseFile] for testing.
 *
 * @author Niall Scott
 */
internal class FakeBusStopDatabaseFile(
    val name: String,
    private val onAbsolutePath: () -> String = { throw NotImplementedError() },
    private val onDelete: () -> Boolean = { throw NotImplementedError() },
    private val onExists: () -> Boolean = { throw NotImplementedError() },
    private val onInputStream: () -> FileInputStream = { throw NotImplementedError() },
    private val onRenameTo: (File) -> Boolean = { throw NotImplementedError() }
) : BusStopDatabaseFile {

    override val absolutePath get() = onAbsolutePath()

    override fun delete() = onDelete()

    override fun exists() = onExists()

    override fun inputStream() = onInputStream()

    override fun renameTo(destination: File) = onRenameTo(destination)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is FakeBusStopDatabaseFile) {
            return false
        }

        return name == other.name
    }

    override fun hashCode() = name.hashCode()
}