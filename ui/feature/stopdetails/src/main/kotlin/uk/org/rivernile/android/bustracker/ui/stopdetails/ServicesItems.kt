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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.text.MediumDecoratedServiceNameText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_OPERATOR_ITEM = "operator-item"
internal const val TEST_TAG_SERVICE_ITEM = "services-item"
internal const val TEST_TAG_NO_SERVICES_ITEM = "no-services-item"
internal const val TEST_TAG_SERVICE_NAME = "service-name"
internal const val TEST_TAG_SERVICE_DESCRIPTION = "service-description"

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
            .safeDrawingPadding()
            .padding(
                horizontal = dimensionResource(Rcore.dimen.padding_double),
                vertical = dimensionResource(Rcore.dimen.padding_default)
            )
            .semantics {
                heading()
                testTag = TEST_TAG_OPERATOR_ITEM
            },
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium
    )
}

/**
 * Provides a service item to be displayed.
 *
 * @param service The service to be displayed.
 * @param modifier Any [Modifier] to be applied.
 * @author Niall Scott
 */
@Composable
internal fun ServiceItem(
    service: UiServicesItem.Service,
    modifier: Modifier = Modifier
) {
    val paddingDouble = dimensionResource(Rcore.dimen.padding_double)

    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .safeDrawingPadding()
            .padding(
                horizontal = paddingDouble,
                vertical = dimensionResource(Rcore.dimen.padding_default)
            )
            .semantics {
                testTag = TEST_TAG_SERVICE_ITEM
            },
        horizontalArrangement = Arrangement.spacedBy(paddingDouble),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServiceName(
            serviceName = service.serviceName
        )

        ServiceDescriptionText(
            description = service.description
        )
    }
}

/**
 * Provides a "no services" item to be displayed.
 *
 * @param modifier Any [Modifier] to be applied.
 * @author Niall Scott
 */
@Composable
internal fun NoServicesItem(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.stopdetails_no_services),
        modifier = modifier
            .heightIn(min = 56.dp)
            .padding(dimensionResource(Rcore.dimen.padding_double))
            .semantics {
                testTag = TEST_TAG_NO_SERVICES_ITEM
            },
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ServiceName(
    serviceName: UiServiceName,
    modifier: Modifier = Modifier
) {
    MediumDecoratedServiceNameText(
        service = serviceName,
        modifier = modifier
            .widthIn(min = dimensionResource(R.dimen.stopdetails_service_name_min_width))
            .semantics {
                testTag = TEST_TAG_SERVICE_NAME
            },
        padding = PaddingValues(dimensionResource(Rcore.dimen.padding_half))
    )
}

@Composable
private fun ServiceDescriptionText(
    description: String?,
    modifier: Modifier = Modifier
) {
    Text(
        text = description
            ?.trim()
            ?.ifBlank { null }
            ?: stringResource(R.string.stopdetails_item_service_unknown_description),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_SERVICE_DESCRIPTION
            },
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview(
    name = "Services item - light",
    group = "Services item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Services item - dark",
    group = "Services item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ServicesItemsPreview(
    @PreviewParameter(UiServicesItemProvider::class) item: UiServicesItem
) {
    MyBusTheme {
        when (item) {
            is UiServicesItem.Operator.Unknown -> OperatorItem(
                operatorName = stringResource(R.string.stopdetails_item_unknown_operator),
                modifier = Modifier
                    .fillMaxWidth()
            )
            is UiServicesItem.Operator.Named -> OperatorItem(
                operatorName = item.operatorName,
                modifier = Modifier
                    .fillMaxWidth()
            )
            is UiServicesItem.Service -> ServiceItem(
                service = item,
                modifier = Modifier
                    .fillMaxWidth()
            )
            is UiServicesItem.NoServices -> NoServicesItem(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

private class UiServicesItemProvider : PreviewParameterProvider<UiServicesItem> {

    override val values = sequenceOf(
        UiServicesItem.Operator.Unknown,
        UiServicesItem.Operator.Named(
            operatorId = "TEST1",
            operatorName = "Operator Name"
        ),
        UiServicesItem.Service(
            serviceDescriptor = ServiceDescriptor("1", "TEST1"),
            serviceName = UiServiceName(
                serviceName = "1",
                colours = UiServiceColours(
                    backgroundColour = Color.Red.toArgb(),
                    textColour = Color.White.toArgb()
                )
            ),
            description = "Route description"
        ),
        UiServicesItem.Service(
            serviceDescriptor = ServiceDescriptor("1", "TEST1"),
            serviceName = UiServiceName(
                serviceName = "1",
                colours = UiServiceColours(
                    backgroundColour = Color.Red.toArgb(),
                    textColour = Color.White.toArgb()
                )
            ),
            description = null
        ),
        UiServicesItem.Service(
            serviceDescriptor = ServiceDescriptor("1", "TEST1"),
            serviceName = UiServiceName(
                serviceName = "1",
                colours = UiServiceColours(
                    backgroundColour = Color.Red.toArgb(),
                    textColour = Color.White.toArgb()
                )
            ),
            description = "This is a really really really long description to test how the text " +
                "is treated in this condition"
        ),
        UiServicesItem.NoServices
    )
}
