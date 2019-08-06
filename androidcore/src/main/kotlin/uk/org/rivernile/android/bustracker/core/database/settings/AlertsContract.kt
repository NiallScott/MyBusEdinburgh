/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings

import android.net.Uri
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForSettingsDatabase
import uk.org.rivernile.android.bustracker.core.database.TableContract
import uk.org.rivernile.android.bustracker.core.utils.OpenForTesting
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fields for interacting with alerts. Each row of this table represents a single alert. The
 * following fields must be included when inserting a new alert;
 *
 * - [ID]
 * - [TYPE]
 * - [TIME_ADDED]
 * - [STOP_CODE]
 *
 * When the type is [ALERTS_TYPE_TIME], then [SERVICE_NAMES] and [TIME_TRIGGER] must also be
 * supplied.
 *
 * When the type is [ALERTS_TYPE_PROXIMITY], then [DISTANCE_FROM] must also be supplied.
 *
 * @param authority The [android.content.ContentProvider] authority.
 * @author Niall Scott
 */
@Singleton
@OpenForTesting
internal class AlertsContract @Inject constructor(
        @ForSettingsDatabase authority: String): TableContract {

    companion object {

        /**
         * Expose [TableContract.ID]
         */
        const val ID = TableContract.ID

        /**
         * Expose [TableContract.COUNT]
         */
        const val COUNT = TableContract.COUNT

        /**
         * The type of alert. Column name.
         *
         * Type: INTEGER (one of [.ALERTS_TYPE_PROXIMITY] or [.ALERTS_TYPE_TIME])
         */
        const val TYPE = "type"

        /**
         * A proximity alert type.
         */
        const val ALERTS_TYPE_PROXIMITY: Byte = 1
        /**
         * A time alert type.
         */
        const val ALERTS_TYPE_TIME: Byte = 2

        /**
         * The time the alert was added. Column name.
         *
         * Type: INTEGER (long)
         */
        const val TIME_ADDED = "timeAdded"

        /**
         * The stop code for this alert. Column name.
         *
         * Type: STRING
         */
        const val STOP_CODE = "stopCode"

        /**
         * The maximum distance from the stop the alert should activate. Column name.
         *
         * Type: INTEGER
         */
        const val DISTANCE_FROM = "distanceFrom"

        /**
         * The names of the services the alert concerns. Column name.
         *
         * Type: STRING (comma separated list)
         */
        const val SERVICE_NAMES = "serviceNames"

        /**
         * The maximum amount of time before the service is due to the alert should activate. Column
         * name.
         *
         * Type: INTEGER
         */
        const val TIME_TRIGGER = "timeTrigger"

        private const val TABLE_NAME = "active_alerts"
    }

    private val typeSingle = "${TableContract.SUBTYPE_SINGLE}/vnd.$authority.$TABLE_NAME"
    private val typeMulltiple = "${TableContract.SUBTYPE_MULTIPLE}/vnd.$authority.$TABLE_NAME"
    private val uri = Uri.parse("content://$authority/$TABLE_NAME")

    override fun getSingleItemType() = typeSingle

    override fun getMultipleItemsType() = typeMulltiple

    override fun getContentUri(): Uri  = uri
}