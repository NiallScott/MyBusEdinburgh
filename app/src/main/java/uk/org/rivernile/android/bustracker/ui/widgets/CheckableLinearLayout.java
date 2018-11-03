/*
 * Copyright (C) 2017 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This is a {@link LinearLayout} which has an additional state of being {@link Checkable}.
 *
 * @author Niall Scott
 * @see LinearLayout
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

    private static final int[] STATE_CHECKED = {
            android.R.attr.state_checked
    };

    private boolean checked;

    /**
     * See {@link LinearLayout#LinearLayout(Context)}.
     *
     * @param context {@link LinearLayout#LinearLayout(Context)}.
     */
    public CheckableLinearLayout(@NonNull final Context context) {
        super(context);
    }

    /**
     * See {@link LinearLayout#LinearLayout(Context, AttributeSet)}.
     *
     * @param context See {@link LinearLayout#LinearLayout(Context, AttributeSet)}.
     * @param attrs See {@link LinearLayout#LinearLayout(Context, AttributeSet)}.
     */
    public CheckableLinearLayout(@NonNull final Context context,
            @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * See {@link LinearLayout#LinearLayout(Context, AttributeSet, int)}.
     *
     * @param context See {@link LinearLayout#LinearLayout(Context, AttributeSet, int)}.
     * @param attrs See {@link LinearLayout#LinearLayout(Context, AttributeSet, int)}.
     * @param defStyleAttr See {@link LinearLayout#LinearLayout(Context, AttributeSet, int)}.
     */
    public CheckableLinearLayout(@NonNull final Context context,
            @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (isChecked()) {
            mergeDrawableStates(drawableState, STATE_CHECKED);
        }

        return drawableState;
    }

    @Override
    public void setChecked(final boolean checked) {
        this.checked = checked;
        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }
}
