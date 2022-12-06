/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemAbout2LineBinding
import java.text.DateFormat

/**
 * This [RecyclerView.ViewHolder] shows a two-line 'about' item.
 *
 * @param viewBinding The view binding.
 * @param itemClickedListener The click listener which is called when the item is clicked.
 * @author Niall Scott
 */
class TwoLinesItemViewHolder(
        private val viewBinding: ListItemAbout2LineBinding,
        private val itemClickedListener: OnItemClickedListener)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private val dateFormat = DateFormat.getDateTimeInstance()
    private var item: UiAboutItem.TwoLinesItem? = null

    init {
        viewBinding.root.setOnClickListener {
            handleItemClicked()
        }
    }

    /**
     * Populate the item.
     *
     * @param item The item to populate with.
     */
    fun populate(item: UiAboutItem.TwoLinesItem?) {
        this.item = item

        viewBinding.apply {
            item?.let {
                text1.setText(getItemTitleText(it))
                text2.text = getItemSubtitleText(it)
                root.isClickable = it.isClickable
                root.isFocusable = it.isClickable
            } ?: run {
                text1.text = null
                text2.text = null
                root.isClickable = false
                root.isFocusable = false
            }
        }
    }

    /**
     * Get the text String resource for the item.
     *
     * @param item The item.
     * @return The String resource ID for the item.
     */
    @StringRes
    private fun getItemTitleText(item: UiAboutItem.TwoLinesItem) = when (item) {
        is UiAboutItem.TwoLinesItem.AppVersion -> R.string.about_version
        is UiAboutItem.TwoLinesItem.Author -> R.string.about_author
        is UiAboutItem.TwoLinesItem.Website -> R.string.about_website
        is UiAboutItem.TwoLinesItem.Twitter -> R.string.about_twitter
        is UiAboutItem.TwoLinesItem.DatabaseVersion -> R.string.about_database_version
        is UiAboutItem.TwoLinesItem.TopologyVersion -> R.string.about_topology_version
    }

    /**
     * Get the [String] for an item's subtitle.
     *
     * @param item The item.
     * @return A [String] for the item's subtitle.
     */
    private fun getItemSubtitleText(item: UiAboutItem.TwoLinesItem) = when (item) {
        is UiAboutItem.TwoLinesItem.AppVersion -> getAppVersionText(item)
        is UiAboutItem.TwoLinesItem.Author -> getAuthorText()
        is UiAboutItem.TwoLinesItem.Website -> getWebsiteText()
        is UiAboutItem.TwoLinesItem.Twitter -> getTwitterText()
        is UiAboutItem.TwoLinesItem.DatabaseVersion -> getDatabaseVersionText(item)
        is UiAboutItem.TwoLinesItem.TopologyVersion -> getTopologyVersionText(item)
    }

    /**
     * Get the subtitle text for a [UiAboutItem.TwoLinesItem.AppVersion] item.
     *
     * @param item The item.
     * @return The [String] to display for this item's subtitle.
     */
    private fun getAppVersionText(item: UiAboutItem.TwoLinesItem.AppVersion) =
            viewBinding.root.context.getString(
                    R.string.about_version_format,
                    item.versionName,
                    item.versionCode)

    /**
     * Get the subtitle text for a [UiAboutItem.TwoLinesItem.Author] item.
     *
     * @return The [String] to display for this item's subtitle.
     */
    private fun getAuthorText() =
            viewBinding.root.context.getString(R.string.app_author)

    /**
     * Get the subtitle text for a [UiAboutItem.TwoLinesItem.Website] item.
     *
     * @return The [String] to display for this item's subtitle.
     */
    private fun getWebsiteText() =
            viewBinding.root.context.getString(R.string.app_website)

    /**
     * Get the subtitle text for a [UiAboutItem.TwoLinesItem.Twitter] item.
     *
     * @return The [String] to display for this item's subtitle.
     */
    private fun getTwitterText() =
            viewBinding.root.context.getString(R.string.app_twitter)

    /**
     * Get the subtitle text for a [UiAboutItem.TwoLinesItem.DatabaseVersion] item.
     *
     * @param item The item.
     * @return The [String] to display for this item's subtitle.
     */
    private fun getDatabaseVersionText(item: UiAboutItem.TwoLinesItem.DatabaseVersion): String {
        return item.date?.let {
            viewBinding.root.context.getString(
                    R.string.about_database_version_format,
                    it.time,
                    dateFormat.format(it))
        } ?: viewBinding.root.context.getString(R.string.about_database_version_loading)
    }

    /**
     * Get the subtitle text for a [UiAboutItem.TwoLinesItem.TopologyVersion] item.
     *
     * @param item The item.
     * @return The [String] to display for this item's subtitle.
     */
    private fun getTopologyVersionText(item: UiAboutItem.TwoLinesItem.TopologyVersion): String {
        return item.topologyId
                ?: viewBinding.root.context.getString(R.string.about_topology_version_loading)
    }

    /**
     * Handle the item being clicked.
     */
    private fun handleItemClicked() {
        item?.let {
            if (it.isClickable) {
                itemClickedListener.onItemClicked(it)
            }
        }
    }
}