/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.ui.about;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * This is an item in the 'about' list.
 *
 * @author Niall Scott
 */
class AboutItem {

    private final String title;
    private final String subTitle;
    private final boolean isClickable;

    /**
     * Create a new 'about' item to display to a user.
     *
     * @param title The title of the item. Must not be {@code null}.
     * @param subTitle The subtitle of the item. If there is to be no subtitle, then set this as
     *                 {@code null}.
     * @param isClickable Is this item clickable?
     */
    AboutItem(@NonNull final String title, @Nullable final String subTitle,
              final boolean isClickable) {
        this.title = title;
        this.subTitle = subTitle;
        this.isClickable = isClickable;
    }

    /**
     * Get the title to display for the item.
     *
     * @return The title to display for the item. This will not be {@code null}.
     */
    @NonNull
    String getTitle() {
        return title;
    }

    /**
     * Get the sub title to display for the item.
     *
     * @return The sub title to display for the item. If this is {@code null}, it means there is
     * no subtitle for this item.
     * @see #hasSubTitle()
     */
    @Nullable
    String getSubTitle() {
        return subTitle;
    }

    /**
     * Is this item clickable?
     *
     * @return {@code true} if the item is clickable, {@code false} if not.
     */
    boolean isClickable() {
        return isClickable;
    }

    /**
     * Is there a sub title?
     *
     * @return {@code true} if there is a sub title, {@code false} if there is not.
     * @see #getSubTitle()
     */
    boolean hasSubTitle() {
        return !TextUtils.isEmpty(subTitle);
    }

    /**
     * Do an action for this item. The default implementation does nothing - sub-classes should
     * provide the implementation.
     *
     * @param activity An {@link Activity} instance to provide possibly required resources.
     * @param callbacks Used to send navigation related callbacks.
     */
    void doAction(@NonNull final Activity activity,
                  @NonNull final AboutFragment.Callbacks callbacks) {
        // Implementation supplied by sub-classes.
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}
