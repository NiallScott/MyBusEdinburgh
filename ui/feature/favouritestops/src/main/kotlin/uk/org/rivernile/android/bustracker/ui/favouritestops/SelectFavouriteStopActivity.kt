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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * This [android.app.Activity] displays the user's saved favourite stops and allows them to select
 * a stop which will go on to be displayed on the device home screen.
 *
 * @author Niall Scott
 * @see FavouriteStopsFragment
 */
@AndroidEntryPoint
public class SelectFavouriteStopActivity : ComponentActivity() {

    private companion object {

        private const val LOG_TAG = "SelectFavouriteStopActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Intent.ACTION_CREATE_SHORTCUT != intent.action) {
            Log.w(
                LOG_TAG,
                "We have been started with the Intent action of \"${intent.action}\" but we are " +
                    "expecting \"${Intent.ACTION_CREATE_SHORTCUT}\". Finishing."
            )
            finish()
            return
        }

        enableEdgeToEdge()

        setContent {
            MyBusTheme {
                SelectFavouriteStopScreen { modifier ->
                    FavouriteStopsScreen(
                        modifier = modifier
                    )
                }
            }
        }
    }

    override val defaultViewModelCreationExtras: CreationExtras get() {
        val extras = MutableCreationExtras(super.defaultViewModelCreationExtras)
        val defaultArgs = (extras[DEFAULT_ARGS_KEY] ?: SavedState())
            .apply {
                putBoolean(ARG_IS_SHORTCUT_MODE, true)
            }

        return extras
            .apply {
                this[DEFAULT_ARGS_KEY] = defaultArgs
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectFavouriteStopScreen(
    modifier: Modifier = Modifier,
    screenContent: @Composable (Modifier) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SelectFavouriteStopTopAppBar(
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        screenContent(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectFavouriteStopTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.favouriteshortcut_title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior
    )
}

@Preview(
    name = "Select favourite stops - light",
    group = "Select favourite stops",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Select favourite stops - dark",
    group = "Select favourite stops",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SelectFavouriteStopScreenPreview() {
    MyBusTheme {
        SelectFavouriteStopScreen { modifier ->
            FavouriteStopsScreenWithState(
                state = UiState(
                    content = UiContent.Content(
                        favouriteStops = persistentListOf(
                            UiFavouriteStop(
                                stopCode = "1",
                                savedName = "Favourite 1",
                                services = persistentListOf(
                                    UiServiceName(
                                        serviceName = "1",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Black.toArgb(),
                                            textColour = Color.White.toArgb()
                                        )
                                    )
                                ),
                                dropdownMenu = null
                            ),
                            UiFavouriteStop(
                                stopCode = "2",
                                savedName = "Favourite 2",
                                services = null,
                                dropdownMenu = null
                            ),
                            UiFavouriteStop(
                                stopCode = "3",
                                savedName = "Favourite 3",
                                services = persistentListOf(
                                    UiServiceName(
                                        serviceName = "1",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Black.toArgb(),
                                            textColour = Color.White.toArgb()
                                        )
                                    ),
                                    UiServiceName(
                                        serviceName = "2",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Red.toArgb(),
                                            textColour = Color.White.toArgb()
                                        )
                                    ),
                                    UiServiceName(
                                        serviceName = "3",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Yellow.toArgb(),
                                            textColour = Color.Black.toArgb()
                                        )
                                    ),
                                    UiServiceName(
                                        serviceName = "4",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Green.toArgb(),
                                            textColour = Color.White.toArgb()
                                        )
                                    ),
                                    UiServiceName(
                                        serviceName = "5",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.LightGray.toArgb(),
                                            textColour = Color.Black.toArgb()
                                        )
                                    ),
                                ),
                                dropdownMenu = null
                            )
                        )
                    )
                ),
                modifier = modifier,
                onItemClicked = { },
                onOpenDropdownClicked = { },
                onDropdownMenuDismissed = { },
                onEditFavouriteNameClick = { },
                onRemoveFavouriteClick = { },
                onAddArrivalAlertClick = { },
                onRemoveArrivalAlertClick = { },
                onAddProximityAlertClick = { },
                onRemoveProximityAlertClick = { },
                onShowOnMapClick = { },
                onActionLaunched = { }
            )
        }
    }
}
