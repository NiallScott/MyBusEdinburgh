/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.twitter.apiendpoint

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class describes a user object within the Twitter API.
 *
 * @property name The user's display name.
 * @property profileImageUrl The URL of the profile image.
 * @property screenName The username.
 * @author Niall Scott
 */
@Serializable
internal data class JsonUser(
    @SerialName("name") val name: String? = null,
    @SerialName("profile_image_url_https") val profileImageUrl: String? = null,
    @SerialName("screen_name") val screenName: String? = null)