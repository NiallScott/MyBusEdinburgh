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

package uk.org.rivernile.android.bustracker.ui.about

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [OpenSourceLicenceDialog].
 *
 * @author Niall Scott
 */
class OpenSourceLicenceDialogKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenOnClickIsCalledThenOnDismissRequestIsCalled() {
        val dismissRequestTracker = DismissRequestTracker()

        composeTestRule.setContent {
            MyBusTheme {
                OpenSourceLicenceDialog(onDismissRequest = dismissRequestTracker)
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(Rcore.string.close))
            .performClick()

        assertEquals(1, dismissRequestTracker.numberOfInvocations)
    }

    private class DismissRequestTracker : () -> Unit {

        var numberOfInvocations = 0
            private set

        override fun invoke() {
            numberOfInvocations++
        }
    }
}