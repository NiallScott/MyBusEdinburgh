/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

/**
 * This declares an interface to a platform-dependant geofencing manager.
 *
 * @author Niall Scott
 */
interface GeofencingManager {

    /**
     * Add a new geofence to be tracked by the implementation.
     *
     * @param id The ID of the geofence, to be later referenced in [removeGeofence].
     * @param latitude The latitude of the center point of the geofence.
     * @param longitude The longitude of the center point of the geofence.
     * @param radius How large the geofence should be, as a radius, in meters.
     * @param duration How long, in milliseconds, the geofence should track for.
     */
    fun addGeofence(id: Int, latitude: Double, longitude: Double, radius: Float, duration: Long)

    /**
     * Remove a previously set geofence.
     *
     * @param id The ID of the geofence to remove.
     */
    fun removeGeofence(id: Int)
}