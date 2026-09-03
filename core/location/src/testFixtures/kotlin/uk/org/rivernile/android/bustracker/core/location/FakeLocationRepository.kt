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

package uk.org.rivernile.android.bustracker.core.location

import kotlinx.coroutines.flow.Flow

/**
 * A fake [LocationRepository] for testing.
 *
 * @author Niall Scott
 */
class FakeLocationRepository(
    private val onHasLocationFeature: () -> Boolean = { throw NotImplementedError() },
    private val onHasGpsLocationProvider: () -> Boolean = { throw NotImplementedError() },
    private val onIsLocationEnabledFlow: () -> Flow<Boolean> = { throw NotImplementedError() },
    private val onIsGpsLocationProviderEnabledFlow: () -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onLocationUpdatesFlow: () -> Flow<LocationUpdate> = { throw NotImplementedError() },
    private val onDistanceBetween: (LatLon, LatLon) -> Float =
        { _, _ -> throw NotImplementedError() }
) : LocationRepository {

    override val hasLocationFeature get() = onHasLocationFeature()

    override val hasGpsLocationProvider get() = onHasGpsLocationProvider()

    override val isLocationEnabledFlow get() = onIsLocationEnabledFlow()

    override val isGpsLocationProviderEnabledFlow get() = onIsGpsLocationProviderEnabledFlow()

    override val locationUpdatesFlow get() = onLocationUpdatesFlow()

    override fun distanceBetween(
        first: LatLon,
        second: LatLon
    ) = onDistanceBetween(first, second)
}
