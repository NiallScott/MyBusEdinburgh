/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.busstops

import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName as DatabaseStopName

/**
 * A name of a stop, which consists of a name and an optional locality.
 *
 * @author Niall Scott
 */
public interface StopName {

    /**
     * The name of the stop.
     */
    public val name: String

    /**
     * The locality of the stop.
     */
    public val locality: String?
}

internal fun DatabaseStopName.toStopName(): StopName = WrappedStopName(this)

@JvmInline
internal value class WrappedStopName(
    val databaseStopName: DatabaseStopName
) : StopName {

    override val name get() = databaseStopName.name

    override val locality get() = databaseStopName.locality
}
