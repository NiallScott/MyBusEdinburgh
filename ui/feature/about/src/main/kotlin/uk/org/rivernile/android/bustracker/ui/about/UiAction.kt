/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

/**
 * This sealed interface represents possible actions which can be performed on the 'about' screen.
 *
 * @author Niall Scott
 */
sealed interface UiAction {

    /** The privacy policy should be shown. */
    data object ShowPrivacyPolicy : UiAction

    /** The app's store listing should be shown. */
    data object ShowStoreListing : UiAction

    /** The app author's website should be shown. */
    data object ShowAuthorWebsite : UiAction

    /** The app's website should be shown. */
    data object ShowAppWebsite : UiAction

    /** The app's Twitter account should be shown. */
    data object ShowAppTwitter : UiAction
}