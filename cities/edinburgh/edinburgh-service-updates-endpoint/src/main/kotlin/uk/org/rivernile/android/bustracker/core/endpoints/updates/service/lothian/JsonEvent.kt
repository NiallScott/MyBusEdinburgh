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

package uk.org.rivernile.android.bustracker.core.endpoints.updates.service.lothian

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class defines the structure for an event JSON object.
 *
 * @property id The ID of the event.
 * @property createdTime An ISO-8601 timestamp describing when this event was created at.
 * @property lastUpdatedTime An ISO-8601 timestamp describing when this event was last modified.
 * @property severity A string representing the severity enumeration.
 * @property descriptions A [Map] of `language_code -> description` containing localised versions of
 * the description text.
 * @property routesAffected A [List] of [JsonRouteAffected].
 * @property url A URL which contains more information about the service update event.
 * @author Niall Scott
 */
@Serializable
internal data class JsonEvent(
    @SerialName("id") val id: String? = null,
    @SerialName("created") val createdTime: Instant? = null,
    @SerialName("last_updated") val lastUpdatedTime: Instant? = null,
    @SerialName("severity") val severity: String? = null,
    @SerialName("description") val descriptions: Map<String, String>? = null,
    @SerialName("routes_affected") val routesAffected: List<JsonRouteAffected>? = null,
    @SerialName("url") val url: String? = null
)