/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.FragmentSearchBinding
import javax.inject.Inject

/**
 * This [Fragment] allows the user to perform a text search for stops.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class SearchFragment : Fragment() {

    @Inject
    lateinit var stopMapMarkerDecorator: StopMapMarkerDecorator
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private val viewModel by viewModels<SearchFragmentViewModel>()

    private lateinit var callbacks: Callbacks
    private lateinit var adapter: SearchAdapter

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: FragmentSearchBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (_: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                Callbacks::class.java.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = SearchAdapter(
            requireContext(),
            stopMapMarkerDecorator,
            textFormattingUtils,
            viewModel::onItemClicked
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentSearchBinding.inflate(inflater, container, false).also {
            _viewBinding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
                val insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout()
                )

                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                    rightMargin = insets.right
                }

                windowInsets
            }

            ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { view, windowInsets ->
                val insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout()
                )

                view.updatePadding(
                    bottom = resources.getDimensionPixelOffset(Rcore.dimen.padding_default) +
                        insets.bottom
                )

                WindowInsetsCompat.CONSUMED
            }

            recyclerView.adapter = this@SearchFragment.adapter
        }

        val viewLifecycleOwner = viewLifecycleOwner
        viewModel.searchResultsLiveData.observe(viewLifecycleOwner, adapter::submitList)
        viewModel.uiStateLiveData.observe(viewLifecycleOwner, this::handleUiStateChanged)
        viewModel.showStopLiveData.observe(viewLifecycleOwner, callbacks::onShowBusTimes)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    /**
     * The term to search and display results for.
     */
    var searchTerm: String?
        get() = viewModel.searchTerm
        set(value) {
            viewModel.searchTerm = value
        }

    /**
     * Handle the UI state changing.
     *
     * @param state The new [UiState].
     */
    private fun handleUiStateChanged(state: UiState) {
        viewBinding.apply {
            when (state) {
                is UiState.EmptySearchTerm -> {
                    txtError.apply {
                        setText(R.string.search_error_empty)
                        setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_error_search, 0, 0)
                    }

                    contentView.showErrorLayout()
                }
                is UiState.InProgress -> contentView.showProgressLayout()
                is UiState.NoResults -> {
                    txtError.apply {
                        setText(R.string.search_error_no_results)
                        setCompoundDrawablesWithIntrinsicBounds(
                            0, R.drawable.ic_error_directions_bus, 0, 0)
                    }

                    contentView.showErrorLayout()
                }
                is UiState.Content -> contentView.showContentLayout()
            }
        }
    }

    /**
     * Any [android.app.Activity] which host this [Fragment] must implement this interface to
     * handle navigation events.
     */
    interface Callbacks : OnShowBusTimesListener
}
