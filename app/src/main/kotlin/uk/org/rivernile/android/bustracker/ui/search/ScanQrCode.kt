/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/**
 * This [ActivityResultContract] creates an [Intent] to scan a QR code for stop data and returns
 * the result to the calling [Activity].
 *
 * @author Niall Scott
 */
class ScanQrCode : ActivityResultContract<Unit, ScanQrCodeResult>() {

    companion object {

        private const val BARCODE_ACTION = "com.google.zxing.client.android.SCAN"
        private const val BARCODE_EXTRA_QR_CODE_MODE = "QR_CODE_MODE"
        private const val BARCODE_EXTRA_SCAN_RESULT = "SCAN_RESULT"

        private const val URI_QUERY_PARAMETER_STOP_CODE = "busStopCode"
    }

    override fun createIntent(context: Context, input: Unit) =
            Intent(BARCODE_ACTION)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .putExtra(BARCODE_EXTRA_QR_CODE_MODE, true)

    override fun parseResult(resultCode: Int, intent: Intent?): ScanQrCodeResult {
        return if (resultCode == Activity.RESULT_OK) {
            val stopCode = intent?.getStringExtra(BARCODE_EXTRA_SCAN_RESULT)
                    ?.let(Uri::parse)
                    ?.takeIf(Uri::isHierarchical)
                    ?.getQueryParameter(URI_QUERY_PARAMETER_STOP_CODE)

            ScanQrCodeResult.Success(stopCode)
        } else {
            ScanQrCodeResult.Error
        }
    }
}