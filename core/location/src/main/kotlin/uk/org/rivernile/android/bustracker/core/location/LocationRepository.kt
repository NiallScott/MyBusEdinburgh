/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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
 * This repository is used to access any device location properties, for the purpose of providing
 * location-aware functionality.
 *
 * @author Niall Scott
 */
public interface LocationRepository {

    /**
     * Does this device have location-aware features or not?
     */
    public val hasLocationFeature: Boolean

    /**
     * Does this device have a GPS location provider?
     */
    public val hasGpsLocationProvider: Boolean

    /**
     * Get a [Flow] which returns the location enabled status. Any updates to the status will be
     * emitted from the returned [Flow] until cancelled.
     */
    public val isLocationEnabledFlow: Flow<Boolean>

    /**
     * A [Flow] which emits whether the GPS provider is enabled or not.
     */
    public val isGpsLocationProviderEnabledFlow: Flow<Boolean>

    /**
     * A [Flow] which emits location updates.
     *
     * When location updates are available, then the first emission will be [LocationUpdate.Update]
     * if a suitable location is immediately available. If not, then
     * [LocationUpdate.AwaitingLocation] will be emitted while a location is determined.
     *
     * When location updates are not available, then a relevant [LocationUpdate.Error] type will be
     * emitted to signify the reason why location updates cannot be given. This may be emitted after
     * an [LocationUpdate.Update] is emitted because the system state can change.
     *
     * Android implementation note: it's not possible on Android to register a listener against the
     * system to determine when location permissions have changed. As such, the caller should keep
     * track of this and restart this [Flow] when the permissions have changed.
     */
    public val locationUpdatesFlow: Flow<LocationUpdate>

    /**
     * Get the distance, in meters, between [first] and [second].
     *
     * @param first The first location coordinate.
     * @param second The second location coordinate.
     * @return The number of meters between the two coordinates. A negative value implies the
     * distance could not be calculated.
     */
    public fun distanceBetween(first: LatLon, second: LatLon): Float
}
