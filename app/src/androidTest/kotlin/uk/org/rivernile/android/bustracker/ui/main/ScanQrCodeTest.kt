/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import android.app.Activity
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [ScanQrCode].
 *
 * @author Niall Scott
 */
class ScanQrCodeTest {

    companion object {

        private const val BARCODE_ACTION = "com.google.zxing.client.android.SCAN"
        private const val BARCODE_EXTRA_QR_CODE_MODE = "QR_CODE_MODE"
        private const val BARCODE_EXTRA_SCAN_RESULT = "SCAN_RESULT"
    }

    private lateinit var contract: ScanQrCode

    @Before
    fun setUp() {
        contract = ScanQrCode()
    }

    @Test
    fun createIntentCreatesCorrectIntent() {
        val intent = contract.createIntent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            Unit)

        assertEquals(BARCODE_ACTION, intent.action)
        assertEquals(Intent.FLAG_ACTIVITY_NO_HISTORY, intent.flags)
        assert(intent.hasExtra(BARCODE_EXTRA_QR_CODE_MODE))
        assertTrue(intent.getBooleanExtra(BARCODE_EXTRA_QR_CODE_MODE, false))
    }

    @Test
    fun parseResultReturnsErrorResultWhenActivityResultIsNotOk() {
        val result = contract.parseResult(Activity.RESULT_CANCELED, null)

        assertEquals(ScanQrCodeResult.Error, result)
    }

    @Test
    fun parseResultReturnsSuccessWithNullStopCodeWhenIntentIsNullAndActivityResultIsOk() {
        val result = contract.parseResult(Activity.RESULT_OK, null)
        val expected = ScanQrCodeResult.Success(null)

        assertEquals(expected, result)
    }

    @Test
    fun parseResultReturnsSuccessWithNullStopCodeWhenIntentDoesNotContainResult() {
        val intent = Intent()
        val result = contract.parseResult(Activity.RESULT_OK, intent)
        val expected = ScanQrCodeResult.Success(null)

        assertEquals(expected, result)
    }

    @Test
    fun parseResultReturnsSuccessWithNullStopCodeWhenUriDoesNotContainParameter() {
        val intent = Intent().putExtra(BARCODE_EXTRA_SCAN_RESULT, "foobar")
        val result = contract.parseResult(Activity.RESULT_OK, intent)
        val expected = ScanQrCodeResult.Success(null)

        assertEquals(expected, result)
    }

    @Test
    fun parseResultReturnsSuccessWithNullStopCodeWhenParameterIsEmpty() {
        val intent = Intent().putExtra(BARCODE_EXTRA_SCAN_RESULT, "http://foo.bar/?busStopCode=")
        val result = contract.parseResult(Activity.RESULT_OK, intent)
        val expected = ScanQrCodeResult.Success(null)

        assertEquals(expected, result)
    }

    @Test
    fun parseResultReturnsSuccessWithStopCodeWhenParameterIsPopulated() {
        val intent = Intent()
            .putExtra(BARCODE_EXTRA_SCAN_RESULT, "http://foo.bar/?busStopCode=12345678")
        val result = contract.parseResult(Activity.RESULT_OK, intent)
        val expected = ScanQrCodeResult.Success("12345678")

        assertEquals(expected, result)
    }
}