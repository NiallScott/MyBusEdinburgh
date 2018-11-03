/*
 * Copyright (C) 2009 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

/**
 * This class extends the {@link DialogPreference} class in the Android SDK to allow a dialog to
 * be created and for the buttons to be able carry out actions as the API does not allow this.
 *
 * @author Niall Scott
 */
public class GenericDialogPreference extends DialogPreference {

    private DialogInterface.OnClickListener onClick;
    private DialogInterface.OnDismissListener onDismiss;

    /**
     * Create a new {@code GenericDialogPreference} instance. This simply calls the constructor
     * in the super class.
     *
     * @param context The {@link Context} to use.
     * @param attrs An {@link AttributeSet} instance.
     * @param defStyle DefStyle.
     */
    public GenericDialogPreference(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Create a new {@code GenericDialogPreference} instance. This simply calls the constructor
     * in the super class.
     *
     * @param context The {@link Context} to use.
     * @param attrs An {@link AttributeSet} instance.
     */
    public GenericDialogPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set the callback to use when a button on the dialog is clicked.
     *
     * @param onClick The callback to use when a button on the dialog is clicked.
     */
    public void setOnClickListener(@Nullable final DialogInterface.OnClickListener onClick) {
        this.onClick = onClick;
    }

    /**
     * Set the callback to use when the dialog is dismissed.
     *
     * @param onDismiss The callback to use when the dialog is dismissed.
     */
    public void setOnDismissListener(@Nullable final DialogInterface.OnDismissListener onDismiss) {
        this.onDismiss = onDismiss;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (onClick != null) {
            onClick.onClick(dialog, which);
        } else {
            super.onClick(dialog, which);
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (onDismiss != null) {
            onDismiss.onDismiss(dialog);
        } else {
            super.onDismiss(dialog);
        }
    }
}