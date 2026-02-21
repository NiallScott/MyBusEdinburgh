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

package uk.org.rivernile.android.bustracker.ui.favouritestops.addoredit

import kotlinx.coroutines.flow.Flow

/**
 * A fake [State] for testing.
 *
 * @author Niall Scott
 */
internal class FakeState(
    private val onGetStopNameText: () -> String? = { throw NotImplementedError() },
    private val onSetStopNameText: (String?) -> Unit = { throw NotImplementedError() },
    private val onStopNameTextFlow: () -> Flow<String?> = { throw NotImplementedError() },
    private val onGetAction: () -> UiAction? = { throw NotImplementedError() },
    private val onSetAction: (UiAction?) -> Unit = { throw NotImplementedError() },
    private val onActionFlow: () -> Flow<UiAction?> = { throw NotImplementedError() }
) : State {

    override var stopNameText
        get() = onGetStopNameText()
        set(value) {
            onSetStopNameText(value)
        }

    override val stopNameTextFlow get() = onStopNameTextFlow()

    override var action: UiAction?
        get() = onGetAction()
        set(value) {
            onSetAction(value)
        }

    override val actionFlow get() = onActionFlow()
}
