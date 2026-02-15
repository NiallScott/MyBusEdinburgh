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

package uk.org.rivernile.android.bustracker.core.alerts

import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.ArrivalAlert
    as DatabaseArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.ProximityAlert
    as DatabaseProximityAlert
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import kotlin.time.Instant

/**
 * This is the base type for user alerts.
 *
 * @author Niall Scott
 */
public sealed interface Alert {

    /**
     * The ID of this alert.
     */
    public val id: Int

    /**
     * The time that this alert was created at.
     */
    public val timeAdded: Instant

    /**
     * What stop does the alert concern?
     */
    public val stopIdentifier: StopIdentifier
}

/**
 * This data class describes an arrival alert that is persisted in the settings database.
 *
 * @property id The ID of this alert.
 * @property timeAdded The time the arrival alert was created at.
 * @property stopIdentifier What stop does the alert concern?
 * @property services A non-empty [List] of services to trigger the alert for.
 * @property timeTriggerMinutes The alert should be fired when any of the named services is due at
 * the named stop at this value or less.
 * @author Niall Scott
 */
public data class ArrivalAlert(
    override val id: Int,
    override val timeAdded: Instant,
    override val stopIdentifier: StopIdentifier,
    val services: Set<ServiceDescriptor>,
    val timeTriggerMinutes: Int
) : Alert

/**
 * This data class describes a proximity alert that is persisted in the settings database.
 *
 * @property id The ID of this alert.
 * @property timeAdded The time the proximity alert was created at.
 * @property stopIdentifier What code does the alert concern?
 * @property distanceFromMeters At what maximum distance from the stop should the alert fire at? Or,
 * what is the radius of the proximity area.
 * @author Niall Scott
 */
public data class ProximityAlert(
    override val id: Int,
    override val timeAdded: Instant,
    override val stopIdentifier: StopIdentifier,
    val distanceFromMeters: Int
) : Alert

internal fun List<DatabaseArrivalAlert>.toArrivalAlertList(): List<ArrivalAlert> =
    map { it.toArrivalAlert() }

internal fun DatabaseArrivalAlert.toArrivalAlert(): ArrivalAlert {
    return ArrivalAlert(
        id = id,
        timeAdded = timeAdded,
        stopIdentifier = stopIdentifier,
        services = services,
        timeTriggerMinutes = timeTriggerMinutes
    )
}

internal fun List<DatabaseProximityAlert>.toProximityAlertList(): List<ProximityAlert> =
    map { it.toProximityAlert() }

internal fun DatabaseProximityAlert.toProximityAlert(): ProximityAlert {
    return ProximityAlert(
        id = id,
        timeAdded = timeAdded,
        stopIdentifier = stopIdentifier,
        distanceFromMeters = radiusTriggerMeters
    )
}
