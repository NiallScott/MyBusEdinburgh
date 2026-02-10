/*
 * Copyright (C) 2024 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor

/**
 * A fake implementation of [ServiceDao] to be used in testing.
 *
 * @author Niall Scott
 */
class FakeServiceDao(
    private val onAllServiceNamesWithColourFlow:
        () -> Flow<List<ServiceWithColour>?> = { emptyFlow() },
    private val onGetServiceNamesWithColourFlow:
        (String) -> Flow<List<ServiceWithColour>?> = { emptyFlow() },
    private val onServiceCountFlow: () -> Flow<Int?> = { emptyFlow() },
    private val onGetColoursForServicesFlow:
        (Set<ServiceDescriptor>?) -> Flow<Map<ServiceDescriptor, ServiceColours>?> =
        { emptyFlow() },
    private val onGetServiceDetailsFlow:
        (String) -> Flow<List<ServiceDetails>?> = { emptyFlow() }
) : ServiceDao {

    override val allServiceNamesWithColourFlow get() = onAllServiceNamesWithColourFlow()

    override fun getServiceNamesWithColourFlow(stopNaptanCode: String) =
        onGetServiceNamesWithColourFlow(stopNaptanCode)

    override val serviceCountFlow get() = onServiceCountFlow()

    override fun getColoursForServicesFlow(services: Set<ServiceDescriptor>?) =
        onGetColoursForServicesFlow(services)

    override fun getServiceDetailsFlow(naptanCode: String) =
        onGetServiceDetailsFlow(naptanCode)
}
