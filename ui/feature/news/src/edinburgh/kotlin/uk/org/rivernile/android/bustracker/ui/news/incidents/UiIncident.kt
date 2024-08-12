/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.incidents

import kotlinx.datetime.Instant

/**
 * This represents the data for an incident which is shown on the UI.
 *
 * @property id The ID of the incident.
 * @property lastUpdated The [Instant] that this incident was last updated at.
 * @property title A short title for the incident.
 * @property summary A summary describing the incident.
 * @property affectedServices A listing of [UiAffectedService]s, if any.
 * @property url An optional URL to the incident on the web.
 * @property showMoreDetailsButton Whether the 'Show more details' button should be shown.
 * @author Niall Scott
 */
internal data class UiIncident(
    val id: String,
    val lastUpdated: Instant,
    val title: String,
    val summary: String,
    val affectedServices: List<UiAffectedService>?,
    val url: String?,
    val showMoreDetailsButton: Boolean
)