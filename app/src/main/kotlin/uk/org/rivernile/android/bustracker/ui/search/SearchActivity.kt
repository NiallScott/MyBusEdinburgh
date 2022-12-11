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

package uk.org.rivernile.android.bustracker.ui.search

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.android.AndroidInjection
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ActivitySearchBinding
import javax.inject.Inject

/**
 * This [android.app.Activity] allows the user to perform a text search for stops. Also, this is the
 * entry point to allow the user to perform a QR code scan for stops.
 *
 * @author Niall Scott
 */
class SearchActivity : AppCompatActivity(), InstallBarcodeScannerDialogFragment.Callbacks {

    companion object {

        /**
         * If this [android.app.Activity] was started with [startActivityForResult], an [Intent]
         * extra with this key will be included in the result which contains the selected stop code.
         */
        const val EXTRA_STOP_CODE = "stopCode"

        private const val DIALOG_INSTALL_QR_SCANNER = "installQrScannerDialog"

        private const val BARCODE_APP_PACKAGE =
                "market://details?id=com.google.zxing.client.android"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var searchManager: SearchManager
    @Inject
    lateinit var stopMapMarkerDecorator: StopMapMarkerDecorator
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private val viewModel: SearchActivityViewModel by viewModels { viewModelFactory }

    private lateinit var adapter: SearchAdapter

    private lateinit var viewBinding: ActivitySearchBinding

    private var menuItemScan: MenuItem? = null

    private val scanQrCodeLauncher = registerForActivityResult(ScanQrCode()) {
        viewModel.onQrScanned(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        viewBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBinding.appBarLayout.statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(this)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
                leftMargin = insets.left
                rightMargin = insets.right
            }

            windowInsets
        }

        adapter = SearchAdapter(
                this,
                stopMapMarkerDecorator,
                textFormattingUtils,
                viewModel::onItemClicked)

        viewBinding.apply {
            recyclerView.apply {
                adapter = this@SearchActivity.adapter
                setHasFixedSize(true)
            }

            searchView.apply {
                setSearchableInfo(searchManager.getSearchableInfo(componentName))
                setOnQueryTextListener(queryTextListener)
            }
        }

        viewModel.searchResultsLiveData.observe(this, adapter::submitList)
        viewModel.uiStateLiveData.observe(this, this::handleUiStateChanged)
        viewModel.isScanMenuItemVisibleLiveData.observe(this, this::handleIsScanMenuItemVisible)
        viewModel.showStopLiveData.observe(this, this::handleShowStop)
        viewModel.showQrCodeScannerLiveData.observe(this) {
            handleShowQrCodeScanner()
        }
        viewModel.showInstallQrScannerDialogLiveData.observe(this) {
            handleShowInstallQrScannerDialog()
        }
        viewModel.showInvalidQrCodeErrorLiveData.observe(this) {
            showInvalidQrCodeError()
        }

        addMenuProvider(menuProvider)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        this.intent = intent
        handleIntent(intent)
    }

    override fun onShowInstallBarcodeScanner() {
        try {
            Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(BARCODE_APP_PACKAGE))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let(this::startActivity)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                    this,
                    R.string.barcodescannerdialog_noplaystore,
                    Toast.LENGTH_LONG)
                    .show()
        }
    }

    /**
     * Handle a new [Intent] for this [android.app.Activity].
     *
     * @param intent The new [Intent].
     */
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH != intent.action) {
            return
        }

        val searchTerm = intent.getStringExtra(SearchManager.QUERY)
        viewBinding.searchView.setQuery(searchTerm, false)
        viewModel.searchTerm = searchTerm
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
     * Handle the visibility of the scan QR code menu item changing.
     *
     * @param isVisible Is the scan QR code menu item visible?
     */
    private fun handleIsScanMenuItemVisible(isVisible: Boolean) {
        menuItemScan?.isVisible = isVisible
    }

    /**
     * Handle how to show the stop. If this [android.app.Activity] was started by another Activity
     * which wishes to obtain a result, then we call [finishWithStopCode], otherwise we call
     * [showDisplayStopDetails].
     *
     * @param stopCode The selected stop code.
     */
    private fun handleShowStop(stopCode: String) {
        callingActivity?.let {
            finishWithStopCode(stopCode)
        } ?: showDisplayStopDetails(stopCode)
    }

    /**
     * Launch [DisplayStopDataActivity] with the selected [stopCode].
     *
     * @param stopCode The stop code to launch [DisplayStopDataActivity] with.
     */
    private fun showDisplayStopDetails(stopCode: String) {
        Intent(this, DisplayStopDataActivity::class.java)
                .putExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, stopCode)
                .let(this::startActivity)
    }

    /**
     * When this [android.app.Activity] has been started by another Activity and we're finishing
     * (because either a valid QR code was scanned, or the user selected a search result item),
     * then set the result and then [finish] ourselves.
     *
     * @param stopCode The selected stop code.
     */
    private fun finishWithStopCode(stopCode: String) {
        val intent = Intent().putExtra(EXTRA_STOP_CODE, stopCode)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Attempt to launch the QR code scanner application.
     */
    private fun handleShowQrCodeScanner() {
        try {
            scanQrCodeLauncher.launch()
        } catch (ignored: ActivityNotFoundException) {
            viewModel.onQrScannerNotFound()
        }
    }

    /**
     * Show a dialog to the user asking them if they wish to install the QR scanner application.
     */
    private fun handleShowInstallQrScannerDialog() {
        InstallBarcodeScannerDialogFragment()
                .show(supportFragmentManager, DIALOG_INSTALL_QR_SCANNER)
    }

    /**
     * Show a [Toast] notification which informs the user they scanned an invalid QR code.
     */
    private fun showInvalidQrCodeError() {
        Toast.makeText(this, R.string.search_invalid_qrcode, Toast.LENGTH_SHORT).show()
    }

    private val queryTextListener = object : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            viewModel.submitSearchTerm(query)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            viewModel.searchTerm = newText
            return false
        }
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.search_option_menu, menu)
            menuItemScan = menu.findItem(R.id.search_option_menu_scan)
        }

        override fun onPrepareMenu(menu: Menu) {
            handleIsScanMenuItemVisible(viewModel.isScanMenuItemVisibleLiveData.value ?: false)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
            R.id.search_option_menu_scan -> {
                viewModel.onScanMenuItemClicked()
                true
            }
            else -> false
        }
    }
}