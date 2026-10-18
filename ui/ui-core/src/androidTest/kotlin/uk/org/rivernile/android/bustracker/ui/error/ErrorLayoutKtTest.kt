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

package uk.org.rivernile.android.bustracker.ui.error

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test

private const val TEST_TAG_ICON = "icon"
private const val TEST_TAG_TITLE_TEXT = "title-text"
private const val TEST_TAG_DESCRIPTION_TEXT = "description-text"

private const val TITLE_TEXT = "Title text"
private const val DESCRIPTION_TEXT = "Description text"

/**
 * Tests for `ErrorLayout.kt`.
 *
 * @author Niall Scott
 */
class ErrorLayoutKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorLayoutRendersTitle() {
        composeTestRule.setContent {
            MyBusTheme {
                ErrorLayoutWithDefaults()
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE_TEXT)
            .apply {
                assertExists()
                assertTextEquals(TITLE_TEXT)
            }
    }

    @Test
    fun errorLayoutDoesNotRenderIconWhenIconIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                ErrorLayoutWithDefaults(
                    icon = null
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ICON)
            .assertDoesNotExist()
    }

    @Test
    fun errorLayoutRendersIconWhenIconIsNotNull() {
        composeTestRule.setContent {
            MyBusTheme {
                ErrorLayoutWithDefaults()
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ICON)
            .assertExists()
    }

    @Test
    fun errorLayoutDoesNotRenderDescriptionTextWhenDescriptionTextIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                ErrorLayoutWithDefaults(
                    descriptionText = null
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_DESCRIPTION_TEXT)
            .assertDoesNotExist()
    }

    @Test
    fun errorLayoutRendersDescriptionTextWhenDescriptionTextIsNotNull() {
        composeTestRule.setContent {
            MyBusTheme {
                ErrorLayoutWithDefaults()
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_DESCRIPTION_TEXT)
            .apply {
                assertExists()
                assertTextEquals(DESCRIPTION_TEXT)
            }
    }

    @Composable
    private fun ErrorLayoutWithDefaults(
        modifier: Modifier = Modifier,
        titleText: @Composable () -> Unit = {
            Text(
                text = TITLE_TEXT,
                modifier = Modifier
                    .semantics {
                        testTag = TEST_TAG_TITLE_TEXT
                    }
            )
        },
        icon: (@Composable () -> Unit)? = {
            Icon(
                painter = painterResource(android.R.drawable.star_on),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .semantics {
                        testTag = TEST_TAG_ICON
                    }
            )
        },
        descriptionText: (@Composable () -> Unit)? = {
            Text(
                text = DESCRIPTION_TEXT,
                modifier = Modifier
                    .semantics {
                        testTag = TEST_TAG_DESCRIPTION_TEXT
                    }
            )
        }
    ) {
        ErrorLayout(
            titleText = titleText,
            modifier = modifier,
            icon = icon,
            descriptionText = descriptionText
        )
    }
}
