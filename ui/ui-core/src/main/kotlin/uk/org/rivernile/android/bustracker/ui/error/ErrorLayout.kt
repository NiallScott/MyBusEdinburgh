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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.org.rivernile.android.bustracker.ui.core.R
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * A layout which shows a high-level error to a user. For example, when some content has failed to
 * load, or an empty state should be shown.
 *
 * The error consists of mandatory title text, as well as an optional icon and optional description
 * text.
 *
 * @param titleText A [Composable] which renders the title text.
 * @param modifier Any [Modifier]s to be applied.
 * @param icon A [Composable] which renders the error icon.
 * @param descriptionText A [Composable] which renders the description text.
 * @param actions Any actions which appear at the bottom of the error.
 * @author Niall Scott
 */
@Composable
public fun ErrorLayout(
    titleText: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    descriptionText: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.primary
            ) {
                icon()
            }

            Spacer(
                modifier = Modifier
                    .height(dimensionResource(R.dimen.padding_double))
            )
        }

        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyLarge.merge(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        ) {
            titleText()
        }

        if (descriptionText != null) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge.merge(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            ) {
                descriptionText()
            }
        }

        if (actions != null) {
            val paddingDefault = dimensionResource(R.dimen.padding_default)

            Spacer(
                modifier = Modifier
                    .height(paddingDefault)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(paddingDefault)
            ) {
                actions()
            }
        }
    }
}

@Preview(
    name = "Error layout - without icon - light",
    group = "Error layout - without icon",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Error layout - without icon - dark",
    group = "Error layout - without icon",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ErrorLayoutWithoutIconPreview() {
    MyBusTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ErrorLayout(
                titleText = {
                    Text(
                        text = "Test text"
                    )
                },
                descriptionText = {
                    Text(
                        text = "Description text that is really long so that it wraps on to a " +
                            "new line."
                    )
                }
            )
        }
    }
}

@Preview(
    name = "Error layout - with icon - light",
    group = "Error layout - with icon",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Error layout - with icon - dark",
    group = "Error layout - with icon",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ErrorLayoutWithIconPreview() {
    MyBusTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ErrorLayout(
                titleText = {
                    Text(
                        text = "Test text"
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(android.R.drawable.star_on),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                    )
                },
                descriptionText = {
                    Text(
                        text = "Description text that is really long so that it wraps on to a " +
                            "new line."
                    )
                }
            )
        }
    }
}

@Preview(
    name = "Error layout - with actions - light",
    group = "Error layout - with actions",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Error layout - with actions - dark",
    group = "Error layout - with actions",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ErrorLayoutWithActionsPreview() {
    MyBusTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ErrorLayout(
                titleText = {
                    Text(
                        text = "Test text"
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(android.R.drawable.star_on),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                    )
                },
                descriptionText = {
                    Text(
                        text = "Description text that is really long so that it wraps on to a " +
                            "new line."
                    )
                },
                actions = {
                    TextButton(
                        onClick = { }
                    ) {
                        Text(
                            text = "Button 1"
                        )
                    }

                    TextButton(
                        onClick = { }
                    ) {
                        Text(
                            text = "Button 2"
                        )
                    }
                }
            )
        }
    }
}
