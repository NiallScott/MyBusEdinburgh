/*
 * Copyright (C) 2022 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.bundle

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import java.io.Serializable

/**
 * Android Tiramisu introduces a new method to obtain a [Parcelable] from a [Bundle] and deprecates
 * the old way. This extension method allows this to be accessed in a compatible way.
 *
 * @param key See [Bundle.getParcelable].
 * @param T See [Bundle.getParcelable].
 * @return [Bundle.getParcelable].
 * @see Bundle.getParcelable
 */
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? =
    BundleCompat.getParcelable(this, key, T::class.java)

/**
 * Android Tiramisu introduces a new method to obtain a [Serializable] from a [Bundle] and
 * deprecates the old way. This extension method allows this to be accessed in a compatible way.
 *
 * @param key See [Bundle.getSerializable].
 * @param T See [Bundle.getSerializable].
 * @return See [Bundle.getSerializable].
 * @see Bundle.getSerializable
 */
inline fun <reified T : Serializable> Bundle.getSerializableCompat(key: String): T? =
    BundleCompat.getSerializable(this, key, T::class.java)
