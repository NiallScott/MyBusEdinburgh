/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.text

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * This class describes a service name which can be rendered on the screen. An optional [colours]
 * property can be defined which specifies with what colours the item should be rendered.
 *
 * @property serviceName The name of the service to be rendered.
 * @property colours The colours attributed to this service name rendering.
 * @author Niall Scott
 */
public data class UiServiceName(
    val serviceName: String,
    val colours: UiServiceColours? = null
)

/**
 * This class describes the colours attributed to a service rendering.
 *
 * @property backgroundColour The colour to render for the background of the service.
 * @property textColour The colour of the text to render on top of the background colour for the
 * service.
 * @author Niall Scott
 */
public data class UiServiceColours(
    val backgroundColour: Int,
    val textColour: Int
)

/**
 * This is a [ProvidableCompositionLocal] which specifies the default [UiServiceColours] to use
 * when a service does not have any colours attributed to it.
 *
 * @author Niall Scott
 */
public val LocalServiceColours: ProvidableCompositionLocal<UiServiceColours> =
    staticCompositionLocalOf {
        UiServiceColours(
            backgroundColour = Color.Black.toArgb(),
            textColour = Color.White.toArgb()
        )
    }

/**
 * A [Composable] which renders the given [services] in a [FlowRow] as
 * [SmallDecoratedServiceNameText].
 *
 * @param services The services to render. These are rendered in the same order as the supplied
 * [ImmutableList]. If this [ImmutableList] is `null` or empty then no composable will be rendered.
 * @param modifier Any [Modifier] to be applied.
 * @param horizontalArrangement See [FlowRow].
 * @param verticalArrangement See [FlowRow].
 * @param fontSize See [Text].
 * @param fontStyle See [Text].
 * @param fontWeight See [Text].
 * @param fontFamily See [Text].
 * @param letterSpacing See [Text].
 * @param lineHeight See [Text].
 * @param style See [Text].
 * @param backgroundShape See [SmallDecoratedServiceNameText].
 * @param itemPadding See [SmallDecoratedServiceNameText].
 * @see FlowRow
 * @see Text
 * @see SmallDecoratedServiceNameText
 * @author Niall Scott
 */
@Composable
public fun SmallDecoratedServiceNamesListingText(
    services: ImmutableList<UiServiceName>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, Alignment.Start),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp, Alignment.Top),
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    backgroundShape: Shape = MaterialTheme.shapes.extraSmall,
    itemPadding: PaddingValues = PaddingValues(8.dp)
) {
    if (services.isEmpty()) {
        return
    }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        services.forEach {
            SmallDecoratedServiceNameText(
                service = it,
                modifier = Modifier.alignByBaseline(),
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                lineHeight = lineHeight,
                style = style,
                backgroundShape = backgroundShape,
                padding = itemPadding
            )
        }
    }
}

/**
 * A [Composable] which renders the given [service] as a single item in the small style.
 *
 * @param service The [UiServiceName] to render.
 * @param modifier Any [Modifier] to be applied.
 * @param fontSize See [Text].
 * @param fontStyle See [Text].
 * @param fontWeight See [Text].
 * @param fontFamily See [Text].
 * @param letterSpacing See [Text].
 * @param lineHeight See [Text].
 * @param style See [Text].
 * @param backgroundShape See [SmallDecoratedServiceNameText].
 * @param padding See [SmallDecoratedServiceNameText].
 * @see Text
 * @author Niall Scott
 */
@Composable
public fun SmallDecoratedServiceNameText(
    service: UiServiceName,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    backgroundShape: Shape = MaterialTheme.shapes.extraSmall,
    padding: PaddingValues = PaddingValues(8.dp)
) {
    DecoratedServiceNameText(
        service = service,
        modifier = modifier,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        style = style,
        backgroundShape = backgroundShape,
        padding = padding
    )
}

internal const val TEST_TAG_DECORATED_SERVICE_NAME = "decorated-service-name"

/**
 * A [Composable] which renders the given [service] as a single item.
 *
 * @param service The [UiServiceName] to render.
 * @param modifier Any [Modifier] to be applied.
 * @param fontSize See [Text].
 * @param fontStyle See [Text].
 * @param fontWeight See [Text].
 * @param fontFamily See [Text].
 * @param letterSpacing See [Text].
 * @param lineHeight See [Text].
 * @param style See [Text].
 * @param backgroundShape See [SmallDecoratedServiceNameText].
 * @param padding See [SmallDecoratedServiceNameText].
 * @see Text
 * @author Niall Scott
 */
@Composable
private fun DecoratedServiceNameText(
    service: UiServiceName,
    modifier: Modifier,
    fontSize: TextUnit,
    fontStyle: FontStyle?,
    fontWeight: FontWeight?,
    fontFamily: FontFamily?,
    letterSpacing: TextUnit,
    lineHeight: TextUnit,
    style: TextStyle,
    backgroundShape: Shape,
    padding: PaddingValues
) {
    val colours = service.colours ?: LocalServiceColours.current

    Text(
        text = service.serviceName,
        modifier = modifier
            .background(Color(colours.backgroundColour), backgroundShape)
            .padding(padding)
            .widthIsAtLeastHeight()
            .semantics {
                testTag = TEST_TAG_DECORATED_SERVICE_NAME
            },
        color = Color(colours.textColour),
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textAlign = TextAlign.Center,
        lineHeight = lineHeight,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        minLines = 1,
        style = style
    )
}

/**
 * With this [Modifier], this ensures the width of this composeable is at least equal to or greater
 * than the height. This is used to prevent items with small widths looking unsightly.
 */
private fun Modifier.widthIsAtLeastHeight() =
    this then WidthAtLeastHeightElement(
        inspectorInfo = debugInspectorInfo {
            name = "widthIsAtLeastHeight"
        }
    )

private class WidthAtLeastHeightElement(
    val inspectorInfo: InspectorInfo.() -> Unit
) : ModifierNodeElement<WidthAtLeastHeightNode>() {

    override fun create() = WidthAtLeastHeightNode()

    override fun update(node: WidthAtLeastHeightNode) {
        // Nothing to do here.
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }

    override fun hashCode() = 1

    override fun equals(other: Any?) = other != null && other::class == this::class
}

private class WidthAtLeastHeightNode : LayoutModifierNode, Modifier.Node() {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)

        val height = placeable.height
        val width = placeable.width.coerceAtLeast(height)
        val x = if (width > placeable.width) {
            (width - placeable.width) / 2
        } else {
            0
        }

        return layout(
            width = width,
            height = height
        ) {
            placeable.placeRelative(
                x = x,
                y = 0
            )
        }
    }
}

@Preview(
    name = "Service names listing - light",
    group = "Service names listing",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Service names listing - dark",
    group = "Service names listing",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DecoratedServiceNamesListingTextPreview() {
    MyBusTheme {
        SmallDecoratedServiceNamesListingText(
            services = persistentListOf(
                UiServiceName(
                    serviceName = "1",
                    colours = UiServiceColours(
                        backgroundColour = Color.Blue.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "2",
                    colours = UiServiceColours(
                        backgroundColour = Color.DarkGray.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "300",
                    colours = UiServiceColours(
                        backgroundColour = Color.Red.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "Long service name",
                    colours = UiServiceColours(
                        backgroundColour = Color.Green.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "TRAM",
                    colours = null
                )
            ),
            modifier = Modifier.padding(16.dp),
            fontSize = 36.sp
        )
    }
}