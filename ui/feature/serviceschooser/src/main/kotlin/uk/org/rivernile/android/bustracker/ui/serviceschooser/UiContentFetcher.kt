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

import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import javax.inject.Inject

/**
 * Fetches content and exposes this as [UiContent] objects.
 *
 * @author Niall Scott
 */
internal interface UiContentFetcher {

    /**
     * A [Flow] which emits [UiContent] objects which reflect the current content state.
     */
    val uiContentFlow: Flow<UiContent>
}

internal class RealUiContentFetcher @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val operatorAndServicesFetcher: OperatorAndServicesFetcher,
    private val comparator: Comparator<String>
) : UiContentFetcher {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val uiContentFlow get() = _uiContentFlow
        .onStart { emit(UiContent.InProgress) }
        .distinctUntilChanged()

    private val _uiContentFlow get() = combine(
        arguments.paramsFlow,
        operatorAndServicesFetcher.operatorAndServicesFlow,
        state.selectedServicesFlow,
        ::createUiContent
    )

    private fun createUiContent(
        params: ServicesChooserParams?,
        operatorServices: Map<UiServiceChooserItem.Operator, List<ServiceWithColour>>?,
        selectedServices: Set<ServiceDescriptor>
    ): UiContent {
        if (params == null) {
            return UiContent.InProgress
        }

        val serviceChooserItems = operatorServices
            ?.toUiServiceChooserItemList(
                selectedServices = selectedServices,
                comparator = comparator
            )

        return if (!serviceChooserItems.isNullOrEmpty()) {
            UiContent.Content(serviceChooserItems.toPersistentList())
        } else {
            params.toError()
        }
    }

    private fun ServicesChooserParams.toError(): UiContent.Error {
        return when (this) {
            is ServicesChooserParams.AllServices -> UiContent.Error.NoGlobalServices
            is ServicesChooserParams.Stop -> UiContent.Error.NoServicesForStop
        }
    }
}
