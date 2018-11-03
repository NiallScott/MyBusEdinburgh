/*
 * Copyright (C) 2014 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.main.sections;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * A {@code Section} is an item in the top level navigation of the application.
 * These are the items that will be shown by the navigation controller (such as
 * an app drawer).
 * 
 * @author Niall Scott
 */
public interface Section {
    
    /**
     * Get the title for a {@code Section}.
     * 
     * @param context A {@link Context} instance.
     * @return The title for a {@code Section}.
     */
    @NonNull
    public CharSequence getTitle(@NonNull Context context);
    
    /**
     * Get the icon resource for a {@code Section}.
     * 
     * @return The icon resource for a {@code Section}. When no icon is to be used, then {@code 0}
     * is returned.
     */
    @DrawableRes
    public int getIconResource();
    
    /**
     * Get the {@link Fragment} to use for a {@code Section}. If a {@link Fragment} is not being
     * used for the section, then {@code null} should be returned.
     * 
     * @return The {@link Fragment} to use for a {@code Section}, or {@code null} if an alternative
     * action is to be performed.
     */
    @Nullable
    public Fragment getFragment();
    
    /**
     * Get the tag to use for the {@link Fragment} for this {@code Section}. If an alternative
     * action is to be performed, return {@code null} here.
     * 
     * @return The tag to use for the {@link Fragment} for this {@code Section}, or {@code null} if
     * an alternative action is to be performed.
     */
    @Nullable
    public String getFragmentTag();
    
    /**
     * The alternative action to perform, if {@link #getFragmentTag()} returns {@code null}.
     * 
     * @param activity The current {@link FragmentActivity}.
     */
    public void doAlternativeAction(@NonNull FragmentActivity activity);
}