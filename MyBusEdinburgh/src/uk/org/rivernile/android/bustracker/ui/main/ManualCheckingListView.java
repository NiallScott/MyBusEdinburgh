/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * This subclass of {@link ListView} exists to get around a problem with the
 * platform's version of the class. When an item is clicked, its checked
 * status is automatically updated by the click handler. This is not desired in
 * this implementation as not all items should be in the clicked state. This
 * is manipulated externally to this class.
 * 
 * <p>
 * This class should only be used when the {@link ListView#setChoiceMode(int)}
 * is set as {@link ListView#CHOICE_MODE_SINGLE}.
 * </p>
 * 
 * @author Niall Scott
 */
public class ManualCheckingListView extends ListView {
    
    private int checkedItem;

    /**
     * Create a new {@code ManualCheckingListView}.
     * 
     * @param context A {@link Context} instance.
     * @see ListView#ListView(android.content.Context)
     */
    public ManualCheckingListView(final Context context) {
        super(context);
    }

    /**
     * Create a new {@code ManualCheckingListView}.
     * 
     * @param context A {@link Context} instance.
     * @param attrs The {@link AttributeSet} to customise the view.
     * @see ListView#ListView(android.content.Context, android.util.AttributeSet)
     */
    public ManualCheckingListView(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Create a new {@code ManualCheckingListView}.
     * 
     * @param context A {@link Context} instance.
     * @param attrs The {@link AttributeSet} to customise the view.
     * @param defStyle The defined style.
     * @see ListView#ListView(android.content.Context, android.util.AttributeSet, int)
     */
    public ManualCheckingListView(final Context context,
            final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean performItemClick(final View view, final int position,
            final long id) {
        // Cache the currently checked item position.
        checkedItem = getCheckedItemPosition();
        // Perform the super implementation.
        final boolean result = super.performItemClick(view, position, id);
        // Set the currently checked item as the cached result, or the item that
        // has been set in setItemChecked() in the mean time.
        setItemChecked(checkedItem, true);
        
        return result;
    }

    @Override
    public void setItemChecked(final int position, final boolean value) {
        super.setItemChecked(position, value);
        
        // This may be called during performItemClick(), so store its result if
        // it comes in during this time.
        if (value) {
            checkedItem = position;
        }
    }
}