/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabase

/**
 * The proxy implementation of [ServiceDao] which responds to the database opening/closing and
 * switches [kotlinx.coroutines.flow.Flow]s when this happens.
 *
 * @param database A reference to the database.
 * @author Niall Scott
 */
internal class ProxyServiceDao(
    private val database: AndroidBusStopDatabase) : ServiceDao {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val allServiceNamesFlow get() =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomServiceDao.allServiceNamesFlow
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val serviceCountFlow get() =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomServiceDao.serviceCountFlow
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getColoursForServicesFlow(services: Set<String>?) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomServiceDao.getColoursForServicesFlow(services)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getServiceDetailsFlow(stopCode: String) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomServiceDao.getServiceDetailsFlow(stopCode)
                } else {
                    emptyFlow()
                }
            }
}