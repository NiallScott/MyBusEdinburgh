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

package uk.org.rivernile.android.bustracker.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.IntDef
import uk.org.rivernile.android.bustracker.androidcore.R

/**
 * This [View] allows easy management of a common content view of progress, content and error
 * states. The [View]s which represent these states are added to children of this [View] and are
 * linked up by their IDs.
 *
 * @author Niall Scott
 */
class ContentView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.contentViewStyle,
        defStyleRes: Int = 0): FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {

        /**
         * A constant for representing the progress layout.
         */
        const val CONTENT_VIEW_PROGRESS = 0
        /**
         * A constant for representing the content layout.
         */
        const val CONTENT_VIEW_CONTENT = 1
        /**
         * A constant for representing the error layout.
         */
        const val CONTENT_VIEW_ERROR = 2

        @IntDef(value = [
            CONTENT_VIEW_PROGRESS,
            CONTENT_VIEW_CONTENT,
            CONTENT_VIEW_ERROR
        ])
        @Retention(AnnotationRetention.SOURCE)
        annotation class ContentLayout
    }

    /**
     * The property to access the current state.
     */
    @ContentLayout
    var contentLayout = CONTENT_VIEW_PROGRESS
        set (value) {
            field = value
            applyLayoutStates()
        }

    /**
     * The ID of the [View] which is the root of the progress layout.
     */
    @IdRes
    var progressLayoutId = View.NO_ID
        set (value) {
            field = value
            applyLayoutStates()
        }
    /**
     * The ID of the [View] which is the root of the content layout.
     */
    @IdRes
    var contentLayoutId = View.NO_ID
        set (value) {
            field = value
            applyLayoutStates()
        }
    /**
     * The ID of the [View] which is the root of the error layout.
     */
    @IdRes
    var errorLayoutId = View.NO_ID
        set (value) {
            field = value
            applyLayoutStates()
        }

    init {
        attrs?.let {
            val a = context.theme.obtainStyledAttributes(it, R.styleable.ContentView,
                    defStyleAttr, defStyleRes)
            contentLayout = a.getInt(R.styleable.ContentView_contentLayout, CONTENT_VIEW_PROGRESS)
            progressLayoutId = a.getResourceId(R.styleable.ContentView_progressView, View.NO_ID)
            contentLayoutId = a.getResourceId(R.styleable.ContentView_contentView, View.NO_ID)
            errorLayoutId = a.getResourceId(R.styleable.ContentView_errorView, View.NO_ID)
            a.recycle()
        }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)

        applyLayoutStates()
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)

        applyLayoutStates()
    }

    /**
     * Set the progress layout to be the currently shown layout.
     */
    fun showProgressLayout() {
        contentLayout = CONTENT_VIEW_PROGRESS
    }

    /**
     * Set the content layout to be the currently shown layout.
     */
    fun showContentLayout() {
        contentLayout = CONTENT_VIEW_CONTENT
    }

    /**
     * Set the error layout to be the currently shown layout.
     */
    fun showErrorLayout() {
        contentLayout = CONTENT_VIEW_ERROR
    }

    /**
     * Apply the current state to all layouts.
     */
    private fun applyLayoutStates() {
        applyProgressLayoutState()
        applyContentLayoutState()
        applyErrorLayoutState()
    }

    /**
     * Apply the current state to the progress layout.
     */
    private fun applyProgressLayoutState() {
        if (progressLayoutId != View.NO_ID) {
            findViewById<View>(progressLayoutId)
                    ?.visibility = if (contentLayout == CONTENT_VIEW_PROGRESS) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    /**
     * Apply the current state to the content layout.
     */
    private fun applyContentLayoutState() {
        if (contentLayoutId != View.NO_ID) {
            findViewById<View>(contentLayoutId)
                    ?.visibility = if (contentLayout == CONTENT_VIEW_CONTENT) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    /**
     * Apply the current state to the error layout.
     */
    private fun applyErrorLayoutState() {
        if (errorLayoutId != View.NO_ID) {
            findViewById<View>(errorLayoutId)
                    ?.visibility = if (contentLayout == CONTENT_VIEW_ERROR) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}