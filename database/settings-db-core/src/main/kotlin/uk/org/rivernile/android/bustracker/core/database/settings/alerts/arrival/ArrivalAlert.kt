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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival

import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import kotlin.time.Instant

/**
 * This defines an arrival alert that is persisted to the settings database.
 *
 * @author Niall Scott
 */
public interface ArrivalAlert {

    /**
     * The ID of the alert.
     */
    public val id: Int

    /**
     * The time the alert was created at.
     */
    public val timeAdded: Instant

    /**
     * The stop code this arrival alert is for.
     */
    public val stopIdentifier: StopIdentifier

    /**
     * The alert should be fired when any of the named services are due at this value or less (in
     * minutes).
     */
    public val timeTriggerMinutes: Int

    /**
     * The services that the arrival alert should track.
     */
    public val services: Set<ServiceDescriptor>
}

/**
 * This provides an insertable [ArrivalAlert] which can be added to [ArrivalAlertDao].
 *
 * @property id See [ArrivalAlert.id].
 * @property timeAdded See [ArrivalAlert.timeAdded].
 * @property stopIdentifier See [ArrivalAlert.stopIdentifier].
 * @property timeTriggerMinutes See [ArrivalAlert.timeTriggerMinutes].
 * @property services See [ArrivalAlert.services].
 */
public data class InsertableArrivalAlert(
    override val id: Int = 0,
    override val timeAdded: Instant,
    override val stopIdentifier: StopIdentifier,
    override val timeTriggerMinutes: Int,
    override val services: Set<ServiceDescriptor>
) : ArrivalAlert
