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

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [Fragment] will show the user 'about' information for the application as a list of items.
 *
 * @author Niall Scott
 */
class AboutFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var callbacks: Callbacks
    private lateinit var adapter: AboutAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            callbacks = context as Callbacks
        } catch (e: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                    Callbacks::class.java.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidSupportInjection.inject(this)
        val viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(AboutViewModel::class.java)

        adapter = AboutAdapter(requireContext())
        adapter.items = viewModel.items
        adapter.itemClickedListener = viewModel::onItemClicked

        viewModel.showStoreListing.observe(this, Observer {
            handleAppVersionItemClick()
        })
        viewModel.showAuthorWebsite.observe(this, Observer {
            handleAuthorItemClick()
        })
        viewModel.showAppWebsite.observe(this, Observer {
            handleWebsiteItemClick()
        })
        viewModel.showAppTwitter.observe(this, Observer {
            handleTwitterItemClick()
        })
        viewModel.showCredits.observe(this, Observer {
            callbacks.onShowCredits()
        })
        viewModel.showOpenSourceLicences.observe(this, Observer {
            callbacks.onShowLicences()
        })
        viewModel.databaseVersionItem.observe(this, Observer(adapter::rebindItem))
        viewModel.topologyVersionItem.observe(this, Observer(adapter::rebindItem))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.about_fragment, container, false).also {
            val recyclerView = it.findViewById(android.R.id.list) as RecyclerView

            recyclerView.setHasFixedSize(true)
            recyclerView.addItemDecoration(DividerItemDecoration(requireContext(),
                    DividerItemDecoration.VERTICAL))
            recyclerView.adapter = adapter
        }
    }

    /**
     * Handle the app version item being clicked.
     */
    private fun handleAppVersionItemClick() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${requireActivity().packageName}")
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fail silently.
        }
    }

    /**
     * Handle the author item being clicked.
     */
    private fun handleAuthorItemClick() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(getString(R.string.app_author_website))
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fail silently.
        }
    }

    /**
     * Handle the website item being clicked.
     */
    private fun handleWebsiteItemClick() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(getString(R.string.app_website))
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fail silently.
        }
    }

    /**
     * Handle the Twitter item being clicked.
     */
    private fun handleTwitterItemClick() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(getString(R.string.app_twitter))
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fail silently.
        }
    }

    /**
     * Any [Activities][Activity] which host this [Fragment] must implement this
     * interface to handle navigation events.
     */
    internal interface Callbacks {

        /**
         * This is called when the user wants to see credits.
         */
        fun onShowCredits()

        /**
         * This is called when the user wants to see the open source licences.
         */
        fun onShowLicences()
    }
}