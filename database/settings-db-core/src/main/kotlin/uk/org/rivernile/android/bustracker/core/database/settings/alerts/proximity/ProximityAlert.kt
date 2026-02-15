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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity

import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import kotlin.time.Instant

/**
 * This defines a proximity alert which is persisted to the settings database.
 *
 * @author Niall Scott
 */
public interface ProximityAlert {

    /**
     * The ID of the alert.
     */
    public val id: Int

    /**
     * The time the alert was created at.
     */
    public val timeAdded: Instant

    /**
     * The stop this arrival alert is for.
     */
    public val stopIdentifier: StopIdentifier

    /**
     * The radius that the device must be within to trigger the alert, in meters.
     */
    public val radiusTriggerMeters: Int
}

/**
 * This provides an insertable [ProximityAlert] which can be added to [ProximityAlertDao].
 *
 * @property id See [ProximityAlert.id].
 * @property timeAdded See [ProximityAlert.timeAdded].
 * @property stopIdentifier See [ProximityAlert.stopIdentifier].
 * @property radiusTriggerMeters See [ProximityAlert.radiusTriggerMeters].
 */
public data class InsertableProximityAlert(
    override val id: Int = 0,
    override val timeAdded: Instant,
    override val stopIdentifier: StopIdentifier,
    override val radiusTriggerMeters: Int
) : ProximityAlert
