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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

/**
 * This describes a single row shown in the live times listing.
 *
 * @property serviceName The name of the service.
 * @property serviceColour The colour of the service.
 * @property vehicle The specific vehicle this data is for, where the live time is located.
 * @property position The position of this item within its service grouping.
 * @property expanded Is this item part of an expanded grouping?
 * @author Niall Scott
 */
data class UiLiveTimesItem(
        val serviceName: String,
        val serviceColour: Int?,
        val vehicle: UiVehicle,
        val position: Int,
        val expanded: Boolean) {

    /**
     * Is this item a parent item? That is, is it the first item within a service grouping?
     */
    val isParent = position == 0
}