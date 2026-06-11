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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

internal const val TEST_TAG_OPERATOR_ITEM = "operator-item"
internal const val TEST_TAG_SERVICE_ITEM = "service-item"

/**
 * Provides an operator text item to be displayed.
 *
 * @param operatorName The name of the operator to display.
 * @param modifier Any [Modifier] to be applied.
 * @author Niall Scott
 */
@Composable
internal fun OperatorItem(
    operatorName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = operatorName,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_OPERATOR_ITEM
            },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.titleMedium
    )
}

/**
 * Provides a service selectable item to be displayed.
 *
 * @param service The data of the service to display.
 * @param onClick A lambda which is called when the service has been clicked.
 * @param modifier Any [Modifier] to be applied.
 * @author Niall Scott
 */
@Composable
internal fun ServiceItem(
    service: UiServiceChooserItem.Service,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serviceColours = serviceColours(service.serviceName)

    OutlinedIconToggleButton(
        checked = service.isSelected,
        onCheckedChange = { onClick() },
        modifier = modifier
            .aspectRatio(1f)
            .semantics {
                testTag = TEST_TAG_SERVICE_ITEM
            },
        shape = MaterialTheme.shapes.small,
        colors = serviceToggleButtonColours(serviceColours = serviceColours),
        border = BorderStroke(
            width = 3.dp,
            color = Color(serviceColours.backgroundColour)
        )
    ) {
        Text(
            text = service.serviceName.serviceName,
            modifier = Modifier
                .padding(dimensionResource(Rcore.dimen.padding_default)),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun serviceToggleButtonColours(
    serviceColours: UiServiceColours
): IconToggleButtonColors {
    return IconButtonDefaults
        .outlinedIconToggleButtonColors()
        .copy(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            checkedContainerColor = Color(serviceColours.backgroundColour),
            checkedContentColor = Color(serviceColours.textColour)
        )
}

@Composable
@ReadOnlyComposable
private fun serviceColours(serviceName: UiServiceName?): UiServiceColours {
    return serviceName?.colours ?: UiServiceColours(
        backgroundColour = MaterialTheme.colorScheme.tertiary.toArgb(),
        textColour = MaterialTheme.colorScheme.onTertiary.toArgb()
    )
}

@Preview(
    name = "Services chooser item - light",
    group = "Services chooser item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Services chooser item - dark",
    group = "Services chooser item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ServicesChooserItemPreview(
    @PreviewParameter(UiServiceChooserItemProvider::class) item: UiServiceChooserItem
) {
    MyBusTheme {
        when (item) {
            is UiServiceChooserItem.Operator.Unknown -> OperatorItem(
                operatorName = stringResource(R.string.serviceschooserdialog_item_unknown_operator),
                modifier = Modifier
                    .padding(4.dp)
            )
            is UiServiceChooserItem.Operator.Named -> OperatorItem(
                operatorName = item.operatorName,
                modifier = Modifier
                    .padding(4.dp)
            )
            is UiServiceChooserItem.Service -> ServiceItem(
                service = item,
                onClick = { },
                modifier = Modifier
                    .width(72.dp)
                    .padding(4.dp)
            )
        }
    }
}

private class UiServiceChooserItemProvider : PreviewParameterProvider<UiServiceChooserItem> {

    override val values get() = sequenceOf(
        UiServiceChooserItem.Operator.Unknown,
        UiServiceChooserItem.Operator.Named(
            operatorId = "TEST1",
            operatorName = "Operator Name"
        ),
        UiServiceChooserItem.Service(
            serviceDescriptor = ServiceDescriptor("1", "TEST1"),
            serviceName = UiServiceName(
                serviceName = "1",
                colours = UiServiceColours(
                    backgroundColour = Color.Red.toArgb(),
                    textColour = Color.White.toArgb()
                )
            ),
            isSelected = false
        ),
        UiServiceChooserItem.Service(
            serviceDescriptor = ServiceDescriptor("1", "TEST1"),
            serviceName = UiServiceName(
                serviceName = "1",
                colours = UiServiceColours(
                    backgroundColour = Color.Red.toArgb(),
                    textColour = Color.White.toArgb()
                )
            ),
            isSelected = true
        ),
        UiServiceChooserItem.Service(
            serviceDescriptor = ServiceDescriptor("1", "TEST1"),
            serviceName = UiServiceName(
                serviceName = "1",
                colours = null
            ),
            isSelected = false
        ),
        UiServiceChooserItem.Service(
            serviceDescriptor = ServiceDescriptor("1", "TEST1"),
            serviceName = UiServiceName(
                serviceName = "1",
                colours = null
            ),
            isSelected = true
        )
    )
}
