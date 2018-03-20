package uk.org.rivernile.android.bustracker.ui.about

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract
import uk.org.rivernile.android.bustracker.database.busstop.loaders.DatabaseInformationLoader
import uk.org.rivernile.edinburghbustracker.android.R
import java.text.DateFormat
import java.util.Date

/**
 * This [Fragment] will show the user 'about' information for the application as a list of items.
 *
 * @author Niall Scott
 */
class AboutFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    companion object {

        private const val LOADER_BUS_STOP_DATABASE_INFO = 1

        private const val ITEM_ID_APP_VERSION = 1
        private const val ITEM_ID_AUTHOR = 2
        private const val ITEM_ID_WEBSITE = 3
        private const val ITEM_ID_TWITTER = 4
        private const val ITEM_ID_DATABASE_VERSION = 5
        private const val ITEM_ID_TOPOLOGY_VERSION = 6
        private const val ITEM_ID_CREDITS = 7
        private const val ITEM_ID_OPEN_SOURCE_LICENCES = 8
    }

    private val dateFormat = DateFormat.getDateTimeInstance()
    private lateinit var callbacks: Callbacks
    private lateinit var adapter: AboutAdapter

    private lateinit var itemDatabaseVersion: AboutItem
    private lateinit var itemTopologyVersion: AboutItem

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

        adapter = AboutAdapter(requireContext())
        adapter.itemClickedListener = this::handleItemClicked
        adapter.items = createItems()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.about_fragment, container, false)
        val recyclerView = v.findViewById(android.R.id.list) as RecyclerView

        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loaderManager.initLoader(LOADER_BUS_STOP_DATABASE_INFO, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return DatabaseInformationLoader(requireContext())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        when (loader.id) {
            LOADER_BUS_STOP_DATABASE_INFO -> handleBusStopDatabaseInformationLoaded(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Nothing to do here.
    }

    /**
     * Handle the bus stop database information being loaded.
     *
     * @param cursor The [Cursor] containing the information.
     */
    private fun handleBusStopDatabaseInformationLoaded(cursor: Cursor?) {
        if (cursor != null && cursor.moveToFirst()) {
            val version = Date(cursor.getLong(cursor.getColumnIndex(
                    BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)))
            itemDatabaseVersion.subtitle = getString(R.string.about_database_version_format,
                    version.time, dateFormat.format(version))
            itemTopologyVersion.subtitle = cursor.getString(
                    cursor.getColumnIndex(
                            BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID))
        } else {
            itemDatabaseVersion.subtitle = getString(R.string.about_database_version_error)
            itemTopologyVersion.subtitle = getString(R.string.about_topology_version_error)
        }

        adapter.rebindItem(itemDatabaseVersion)
        adapter.rebindItem(itemTopologyVersion)
    }

    /**
     * Create the items to be displayed by the adapter.
     */
    private fun createItems(): List<AboutItem> {
        // These items are stored in member variables in this class as they are updated later.
        itemDatabaseVersion = createDatabaseVersionItem()
        itemTopologyVersion = createTopologyVersionItem()

        return listOf(
                createVersionItem(),
                createAuthorItem(),
                createWebsiteItem(),
                createTwitterItem(),
                itemDatabaseVersion,
                itemTopologyVersion,
                createCreditsItem(),
                createOpenSourceLicencesItem()
        )
    }

    /**
     * Create the version item.
     *
     * @return The version item.
     */
    private fun createVersionItem() = AboutItem(ITEM_ID_APP_VERSION,
            getString(R.string.about_version), getVersionString(), true)

    /**
     * Create the author item.
     *
     * @return The author item.
     */
    private fun createAuthorItem() = AboutItem(ITEM_ID_AUTHOR, getString(R.string.about_author),
            getString(R.string.app_author), true)

    /**
     * Create the website item.
     *
     * @return The website item.
     */
    private fun createWebsiteItem() = AboutItem(ITEM_ID_WEBSITE, getString(R.string.about_website),
            getString(R.string.app_website), true)

    /**
     * Create the Twitter item.
     *
     * @return The Twitter item.
     */
    private fun createTwitterItem() = AboutItem(ITEM_ID_TWITTER, getString(R.string.about_twitter),
            getString(R.string.app_twitter), true)

    /**
     * Create the database version item.
     *
     * @return The database version item.
     */
    private fun createDatabaseVersionItem() = AboutItem(ITEM_ID_DATABASE_VERSION,
            getString(R.string.about_database_version),
            getString(R.string.about_database_version_loading))

    /**
     * Create the topology version item.
     *
     * @return The topology version item.
     */
    private fun createTopologyVersionItem() = AboutItem(ITEM_ID_TOPOLOGY_VERSION,
            getString(R.string.about_topology_version),
            getString(R.string.about_topology_version_loading))

    /**
     * Create the credits item.
     *
     * @return The credits item.
     */
    private fun createCreditsItem() = AboutItem(ITEM_ID_CREDITS, getString(R.string
            .about_credits), clickable = true)

    /**
     * Create the open source licences item.
     *
     * @return The open source licences item.
     */
    private fun createOpenSourceLicencesItem() = AboutItem(ITEM_ID_OPEN_SOURCE_LICENCES,
            getString(R.string.about_open_source), clickable = true)

    /**
     * Get the version [String].
     *
     * @return The version [String].
     */
    private fun getVersionString(): String {
        return try {
            val context = requireContext()
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            getString(R.string.about_version_format, info.versionName, info.versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            // This should never happen.
            ""
        }
    }

    /**
     * Handle an item in the adapter being clicked.
     *
     * @param item The clicked item.
     */
    private fun handleItemClicked(item: AboutItem) {
        when (item.id) {
            ITEM_ID_APP_VERSION -> handleAppVersionItemClick()
            ITEM_ID_AUTHOR -> handleAuthorItemClick()
            ITEM_ID_WEBSITE -> handleWebsiteItemClick()
            ITEM_ID_TWITTER -> handleTwitterItemClick()
            ITEM_ID_CREDITS -> callbacks.onShowCredits()
            ITEM_ID_OPEN_SOURCE_LICENCES -> callbacks.onShowLicences()
        }
    }

    /**
     * Handle the app version item being clicked.
     */
    private fun handleAppVersionItemClick() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=${requireActivity().packageName}")

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
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.app_author_website))

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
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.app_website))

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
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.app_twitter))

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