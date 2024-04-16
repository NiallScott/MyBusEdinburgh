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

import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * Shows an [AlertDialog] which displays application credits.
 *
 * @param onDismissRequest A lambda which is executed when the dialog is dismissed.
 * @author Niall Scott
 */
@Composable
internal fun CreditsDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.creditsdialog_title))
        },
        text = {
            Text(
                text = stringResource(id = R.string.creditsdialog_body),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.close))
            }
        }
    )
}

@Preview(
    name = "Credits Dialog (light)",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun CreditsDialogLightPreview() {
    CreditsDialogPreview()
}

@Preview(
    name = "Credits Dialog (dark)",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CreditsDialogDarkPreview() {
    CreditsDialogPreview()
}

@Composable
private fun CreditsDialogPreview() {
    MyBusTheme {
        CreditsDialog(
            onDismissRequest = { }
        )
    }
}