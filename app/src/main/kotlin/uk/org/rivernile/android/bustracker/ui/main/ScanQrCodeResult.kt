/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.main

/**
 * This sealed interface encapsulates the result of scanning a QR code.
 *
 * @author Niall Scott
 */
sealed interface ScanQrCodeResult {

    /**
     * Scanning the QR code was successful (the remote [android.app.Activity] returned a result).
     * However, this does not necessarily mean the data was valid. For example, the given [stopCode]
     * could still be `null` or some other invalid value.
     *
     * @property stopCode The scanned stop code - may be valid or invalid data.
     */
    data class Success(
            val stopCode: String?) : ScanQrCodeResult

    /**
     * There was an error scanning the QR code, e.g. operation was cancelled.
     */
    object Error : ScanQrCodeResult
}