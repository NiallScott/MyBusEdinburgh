/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.interop

import android.view.Menu
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Tests for `MenuProvider.kt`.
 *
 * @author Niall Scott
 */
class MenuProviderKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun menuProviderWithEmptyMenuDoesNotThrowAnyException() {
        composeTestRule.setContent {
            MyBusTheme {
                MenuProvider(
                    onCreateMenu = { _, _ -> },
                    onPrepareMenu = { },
                    onMenuItemSelected = { fail("Not expecting onMenuItemSelected to be called.") },
                    onMenuClosed = { }
                )
            }
        }
    }

    @Test
    fun menuProviderAddsMenuAndHandlesMenuItemSelectedEvents() {
        val menuEvents = mutableListOf<MenuEvents>()
        composeTestRule.setContent {
            MyBusTheme {
                MenuProvider(
                    onCreateMenu = { menu, _ ->
                        menuEvents += MenuEvents.ON_CREATE
                        menu.add(
                            Menu.NONE,
                            1,
                            Menu.NONE,
                            "Test"
                        )
                    },
                    onPrepareMenu = {
                        menuEvents += MenuEvents.ON_PREPARE
                        checkNotNull(it.findItem(1)).title = "Test 1"
                    },
                    onMenuItemSelected = {
                        menuEvents += MenuEvents.ON_ITEM_SELECTED
                        if (it.itemId == 1) {
                            true
                        } else {
                            fail("Expecting an item with ID 1")
                        }
                    },
                    onMenuClosed = { }
                )
            }
        }

        assertEquals(
            listOf(
                MenuEvents.ON_CREATE,
                MenuEvents.ON_PREPARE
            ),
            menuEvents
        )
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        onView(withText("Test 1"))
            .perform(click())

        assertEquals(
            listOf(
                MenuEvents.ON_CREATE,
                MenuEvents.ON_PREPARE,
                MenuEvents.ON_PREPARE, // Called twice - first after creation, then before shown.
                MenuEvents.ON_ITEM_SELECTED
            ),
            menuEvents
        )
    }

    private enum class MenuEvents {

        ON_CREATE,
        ON_PREPARE,
        ON_ITEM_SELECTED
    }
}