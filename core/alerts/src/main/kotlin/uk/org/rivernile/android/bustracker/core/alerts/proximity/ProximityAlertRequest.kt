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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.InsertableProximityAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.ProximityAlert
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import kotlin.time.Instant

/**
 * This class holds data for a proximity alert that the user has requested.
 *
 * @property stopIdentifier The stop this proximity alert is for.
 * @property distanceFrom At what maximum distance from the stop should the alert fire at? Or, what
 * is the radius of the proximity area.
 * @author Niall Scott
 */
public data class ProximityAlertRequest(
    val stopIdentifier: StopIdentifier,
    val distanceFrom: Int
)

internal fun ProximityAlertRequest.toProximityAlert(timeAdded: Instant): ProximityAlert {
    return InsertableProximityAlert(
        id = 0,
        timeAdded = timeAdded,
        stopIdentifier = stopIdentifier,
        radiusTriggerMeters = distanceFrom
    )
}
