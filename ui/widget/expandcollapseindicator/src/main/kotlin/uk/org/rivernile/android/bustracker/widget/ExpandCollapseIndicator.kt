/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.widget

import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import uk.org.rivernile.android.bustracker.ui.widget.expandcollapseindicator.R

/**
 * This [android.view.View], which is an extension of [AppCompatImageView], shows an expand/collapse
 * indicator which rotates the indicator depending on its state.
 *
 * The icon should be set in the usual way with [AppCompatImageView].
 *
 * @param context The [android.app.Activity] [Context].
 * @param attrs The view attributes.
 * @param defStyleAttr The default style attributes.
 * @author Niall Scott
 */
class ExpandCollapseIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.expandCollapseIndicatorStyle)
    : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {

        private const val STATE_COLLAPSED = 0
        private const val STATE_EXPANDED = 1

        private const val COLLAPSED_DEGREES = 0f
        private const val EXPANDED_DEGREES = 180f
    }

    private val interpolator = LinearInterpolator()
    private val animationDuration = context
        .resources
        .getInteger(android.R.integer.config_shortAnimTime).toLong()
    private var collapsedContentDescription: CharSequence? = null
    private var expandedContentDescription: CharSequence? = null

    private var currentState = STATE_COLLAPSED

    init {
        attrs?.also {
            val a = context.theme.obtainStyledAttributes(
                it,
                R.styleable.ExpandCollapseIndicator,
                defStyleAttr,
                0)
            setState(
                a.getInt(
                    R.styleable.ExpandCollapseIndicator_expandedState,
                    STATE_COLLAPSED),
                false)
            setCollapsedContentDescription(
                a.getText(R.styleable.ExpandCollapseIndicator_collapsedContentDescription))
            setExpandedContentDescription(
                a.getText(R.styleable.ExpandCollapseIndicator_expandedContentDescription))
            a.recycle()
        } ?: run {
            setState(STATE_COLLAPSED, false)
            applyContentDescription()
        }
    }

    /**
     * Place the indicator in its expanded state.
     *
     * @param animated Whether the transition should be animated. If the indicator is already in the
     * expanded state, no animation will occur.
     */
    fun expand(animated: Boolean) {
        setState(STATE_EXPANDED, animated)
    }

    /**
     * Place the indicator in its collapsed state.
     *
     * @param animated Whether the transition should be animated. If the indicator is already in the
     * collapsed state, no animation will occur.
     */
    fun collapse(animated: Boolean) {
        setState(STATE_COLLAPSED, animated)
    }

    /**
     * Place the indicator in the given state. No animation will occur.
     *
     * @param state The new state to place the indicator in.
     */
    private fun setState(state: Int, animated: Boolean) {
        when (state) {
            STATE_COLLAPSED -> {
                if (animated) {
                    performCollapseArrowAnimation()
                } else {
                    rotation = COLLAPSED_DEGREES
                    applyCollapsedState()
                }
            }
            STATE_EXPANDED -> {
                if (animated) {
                    performExpandArrowAnimation()
                } else {
                    rotation = EXPANDED_DEGREES
                    applyExpandedState()
                }
            }
        }
    }

    /**
     * Set the collapsed state content description, for accessibility.
     *
     * @param contentDescription The new content description for the collapsed state.
     */
    private fun setCollapsedContentDescription(contentDescription: CharSequence?) {
        collapsedContentDescription = contentDescription
        applyContentDescription()
    }

    /**
     * Set the expanded state content description, for acessibility.
     *
     * @param contentDescription The new content description for the expanded state.
     */
    private fun setExpandedContentDescription(contentDescription: CharSequence?) {
        expandedContentDescription = contentDescription
        applyContentDescription()
    }

    /**
     * Apply the correct content description for the current state.
     */
    private fun applyContentDescription() {
        contentDescription = when (currentState) {
            STATE_COLLAPSED -> collapsedContentDescription
            STATE_EXPANDED -> expandedContentDescription
            else -> null
        }
    }

    /**
     * Animate the indicator to the expanded state. No animation will occur if the indicator is
     * already in its expanded state.
     */
    private fun performExpandArrowAnimation() {
        if (rotation != EXPANDED_DEGREES) {
            animate()
                .setInterpolator(interpolator)
                .setDuration(animationDuration)
                .rotation(EXPANDED_DEGREES)
                .withEndAction {
                    applyExpandedState()
                }
        }
    }

    /**
     * Animate the indicator to the collapsed state. No animation will occur if the indicator is
     * already in its collapsed state.
     */
    private fun performCollapseArrowAnimation() {
        if (rotation != COLLAPSED_DEGREES) {
            animate()
                .setInterpolator(interpolator)
                .setDuration(animationDuration)
                .rotation(COLLAPSED_DEGREES)
                .withEndAction {
                    applyCollapsedState()
                }
        }
    }

    /**
     * Apply the necessary state updates for the collapsed state.
     */
    private fun applyCollapsedState() {
        currentState = STATE_COLLAPSED
        applyContentDescription()
    }

    /**
     * Apply the necessary state updates for the expanded state.
     */
    private fun applyExpandedState() {
        currentState = STATE_EXPANDED
        applyContentDescription()
    }
}