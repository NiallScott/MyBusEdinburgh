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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * A fake [State] for testing.
 *
 * @author Niall Scott
 */
internal class FakeState(
    private val onActionFlow: () -> Flow<UiAction?> = { throw NotImplementedError() },
    private val onGetAction: () -> UiAction? = { throw NotImplementedError() },
    private val onSetAction: (UiAction?) -> Unit = { throw NotImplementedError() },
    private val onPermissionsStateFlow: () -> Flow<PermissionsState?> =
        { throw NotImplementedError() },
    private val onGetPermissionsState: () -> PermissionsState? = { throw NotImplementedError() },
    private val onSetPermissionsState: (PermissionsState?) -> Unit = { throw NotImplementedError() },
    private val onSelectedStopIdentifierFlow: () -> Flow<StopIdentifier?> =
        { throw NotImplementedError() },
    private val onGetSelectedStopIdentifier: () -> StopIdentifier? =
        { throw NotImplementedError() },
    private val onSetSelectedStopIdentifier: (StopIdentifier?) -> Unit =
        { throw NotImplementedError() },
    private val onSelectedServicesFlow: () -> Flow<Set<ServiceDescriptor>?> =
        { throw NotImplementedError() },
    private val onGetSelectedServices: () -> Set<ServiceDescriptor>? =
        { throw NotImplementedError() },
    private val onSetSelectedServices: (Set<ServiceDescriptor>?) -> Unit =
        { throw NotImplementedError() },
    private val onUpdateSelectedStopIdentifier: ((StopIdentifier?) -> StopIdentifier?) -> Unit =
        { throw NotImplementedError() }
) : State {

    override val actionFlow get() = onActionFlow()

    override var action: UiAction?
        get() = onGetAction()
        set(value) {
            onSetAction(value)
        }

    override val permissionsStateFlow get() = onPermissionsStateFlow()

    override var permissionsState: PermissionsState?
        get() = onGetPermissionsState()
        set(value) {
            onSetPermissionsState(value)
        }

    override val selectedStopIdentifierFlow get() = onSelectedStopIdentifierFlow()

    override var selectedStopIdentifier: StopIdentifier?
        get() = onGetSelectedStopIdentifier()
        set(value) {
            onSetSelectedStopIdentifier(value)
        }

    override val selectedServicesFlow get() = onSelectedServicesFlow()

    override var selectedServices: Set<ServiceDescriptor>?
        get() = onGetSelectedServices()
        set(value) {
            onSetSelectedServices(value)
        }

    override fun updateSelectedStopIdentifier(function: (StopIdentifier?) -> StopIdentifier?) {
        onUpdateSelectedStopIdentifier(function)
    }
}
