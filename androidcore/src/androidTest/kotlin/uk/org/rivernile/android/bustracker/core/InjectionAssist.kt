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

package uk.org.rivernile.android.bustracker.core

import androidx.test.core.app.ApplicationProvider
import uk.org.rivernile.android.bustracker.core.dagger.DaggerCoreTestApplicationComponent
import uk.org.rivernile.android.bustracker.core.dagger.FakeAlertsModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeCoreModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeSearchDatabaseModule
import uk.org.rivernile.android.bustracker.core.dagger.FakeSettingsDatabaseModule

/**
 * Assists with injecting testing resources.
 *
 * @param application The [TestApplication] instance. Required.
 * @param alertsModule The [FakeAlertsModule] to use. Defaults to default constructor instance.
 * @param coreModule The [FakeCoreModule] to use. Defaults to default constructor instance.
 * @param settingsDatabaseModule The [FakeSettingsDatabaseModule] to use. Defaults to default
 * constructor instance.
 * @author Niall Scott
 */
fun assistInject(
        application: TestApplication,
        alertsModule: FakeAlertsModule = FakeAlertsModule(),
        coreModule: FakeCoreModule = FakeCoreModule(),
        searchDatabaseModule: FakeSearchDatabaseModule = FakeSearchDatabaseModule(),
        settingsDatabaseModule: FakeSettingsDatabaseModule = FakeSettingsDatabaseModule()) {
    DaggerCoreTestApplicationComponent
            .builder()
            .application(application)
            .alertsModule(alertsModule)
            .coreModule(coreModule)
            .searchDatabaseModule(searchDatabaseModule)
            .settingsDatabaseModule(settingsDatabaseModule)
            .build()
            .inject(application)
}

/**
 * A convenience to get the [TestApplication] instance.
 *
 * @return The [TestApplication] instance.
 */
fun getApplication() = ApplicationProvider.getApplicationContext() as TestApplication