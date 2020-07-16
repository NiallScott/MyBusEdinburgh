/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.dagger

import dagger.Binds
import dagger.Module
import uk.org.rivernile.android.bustracker.endpoints.BusTrackerEndpoint
import uk.org.rivernile.android.bustracker.endpoints.HttpBusTrackerEndpoint
import uk.org.rivernile.android.bustracker.endpoints.HttpTwitterEndpoint
import uk.org.rivernile.android.bustracker.endpoints.TwitterEndpoint
import uk.org.rivernile.android.bustracker.endpoints.UrlBuilder
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterParser
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterParserImpl
import uk.org.rivernile.edinburghbustracker.android.parser.livetimes.EdinburghParser
import uk.org.rivernile.edinburghbustracker.android.utils.EdinburghUrlBuilder

/**
 * This [Module] provides and declares bindings for legacy classes, which will be removed later.
 *
 * The idea is once the classes have been re-written, this module can just be removed.
 *
 * @author Niall Scott
 */
@Module
interface LegacyModule {

    @Suppress("unused")
    @Binds
    fun bindUrlBuilder(edinburghUrlBuilder: EdinburghUrlBuilder): UrlBuilder

    @Suppress("unused")
    @Binds
    fun bindBusParser(edinburghParser: EdinburghParser): BusParser

    @Suppress("unused")
    @Binds
    fun bindBusTrackerEndpoint(httpBusTrackerEndpoint: HttpBusTrackerEndpoint): BusTrackerEndpoint

    @Suppress("unused")
    @Binds
    fun bindTwitterParser(twitterParserImpl: TwitterParserImpl): TwitterParser

    @Suppress("unused")
    @Binds
    fun bindTwitterEndpoint(httpTwitterEndpoint: HttpTwitterEndpoint): TwitterEndpoint
}