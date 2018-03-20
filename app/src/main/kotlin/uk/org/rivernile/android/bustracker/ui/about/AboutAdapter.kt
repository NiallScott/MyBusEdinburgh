/*
 * Copyright (C) 2015 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.about

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import uk.org.rivernile.edinburghbustracker.android.R

internal typealias OnItemClickedListener = (item: AboutItem) -> Unit

/**
 * This adapter populates a list of 'about' items in a [RecyclerView].
 *
 * @param context The [android.app.Activity] [Context].
 * @author Niall Scott
 */
internal class AboutAdapter(context: Context) : RecyclerView.Adapter<AboutAdapter.ViewHolder>() {

    companion object {

        private const val VIEW_TYPE_SINGLE = 0
        private const val VIEW_TYPE_DOUBLE = 1
    }

    internal var items: List<AboutItem>? = null
        set (value) {
            field = value
            notifyDataSetChanged()
        }

    internal var itemClickedListener: OnItemClickedListener? = null
    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == VIEW_TYPE_SINGLE) {
            R.layout.simple_list_item_1
        } else {
            R.layout.simple_list_item_2
        }

        return ViewHolder(inflater.inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bindItem(it)
        }
    }

    override fun getItemCount() = items?.size ?: 0

    override fun getItemId(position: Int) = getItem(position)?.id?.toLong() ?: 0L

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return if (item?.subtitle == null) {
            VIEW_TYPE_SINGLE
        } else {
            VIEW_TYPE_DOUBLE
        }
    }

    /**
     * Get the item at the given `position`.
     *
     * @param position The position to get the item at.
     * @return The item at the given `position`, or `null` if there is no item at the position.
     */
    fun getItem(position: Int) = items?.get(position)

    /**
     * Rebind an item at the given index.
     *
     * @param item The item to rebind.
     */
    fun rebindItem(item: AboutItem?) {
        val index = items?.indexOf(item) ?: -1

        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    /**
     * The [RecyclerView.ViewHolder] for this adapter.
     *
     * @param itemView The view for this view holder.
     */
    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView? = itemView.findViewById(android.R.id.text2)

        override fun onClick(v: View) {
            val position = adapterPosition

            if (position != RecyclerView.NO_POSITION) {
                getItem(position)?.let {
                    itemClickedListener?.invoke(it)
                }
            }
        }

        /**
         * Bind an [AboutItem] to the [View]s in this [ViewHolder].
         *
         * @param item The item to bind to the [View]s.
         */
        internal fun bindItem(item: AboutItem) {
            text1.text = item.title
            text2?.text = item.subtitle
            setIsClickable(item.clickable)
        }

        /**
         * Should the row be clickable?
         *
         * @param clickable `true` if the row should be clickable, `false` if not.
         */
        private fun setIsClickable(clickable: Boolean) {
            itemView.setOnClickListener(if (clickable) this else null)
            itemView.isClickable = clickable
        }
    }
}