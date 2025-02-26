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

package uk.org.rivernile.android.bustracker.ui.about

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.app.AppRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseMetadata
import java.util.Date
import javax.inject.Inject

/**
 * This generates [UiAboutItem]s to show in the UI.
 *
 * @author Niall Scott
 */
internal interface AboutItemsGenerator {

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits the 'about' items to show.
     */
    val aboutItemsFlow: Flow<List<UiAboutItem>>

    /**
     * Create the [List] of [UiAboutItem]s.
     *
     * @param databaseMetadata The database metadata.
     * @return The [List] of [UiAboutItem]s.
     */
    fun createAboutItems(databaseMetadata: DatabaseMetadata? = null): List<UiAboutItem>
}

@ViewModelScoped
internal class RealAboutItemsGenerator @Inject constructor(
    private val appRepository: AppRepository,
    private val busStopDatabaseRepository: BusStopDatabaseRepository
) : AboutItemsGenerator {

    override val aboutItemsFlow get() = databaseMetadataFlow
        .map(this::createAboutItems)

    override fun createAboutItems(databaseMetadata: DatabaseMetadata?) =
        listOf(
            UiAboutItem.TwoLinesItem.AppVersion(
                appVersion.versionName,
                appVersion.versionCode
            ),
            UiAboutItem.TwoLinesItem.Author,
            UiAboutItem.TwoLinesItem.Website,
            UiAboutItem.TwoLinesItem.Twitter,
            UiAboutItem.TwoLinesItem.DatabaseVersion(
                databaseMetadata?.updateTimestamp?.let(::Date)
            ),
            UiAboutItem.TwoLinesItem.TopologyVersion(databaseMetadata?.topologyVersionId),
            UiAboutItem.OneLineItem.Credits,
            UiAboutItem.OneLineItem.PrivacyPolicy,
            UiAboutItem.OneLineItem.OpenSourceLicences
        )

    private val databaseMetadataFlow get() = busStopDatabaseRepository
        .databaseMetadataFlow
        .onStart { emit(null) }
        .distinctUntilChanged()

    /**
     * The app version details. This is lazily fetched on first access and then retained forever
     * afterwards as this property does not change between until the app is at least restarted.
     */
    private val appVersion by lazy {
        appRepository.appVersion
    }
}