package com.mukesh.animeapp.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.mukesh.animeapp.app.AnimeApplication
import com.mukesh.animeapp.R
import com.mukesh.animeapp.constants.AppConstants
import com.mukesh.animeapp.data.api.NetworkModule
import com.mukesh.animeapp.data.db.AnimeDatabase
import com.mukesh.animeapp.data.respository.AnimeRepository
import com.mukesh.animeapp.ui.detail.AnimeDetailActivity
import com.mukesh.animeapp.util.NetworkUtil
import com.mukesh.animeapp.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AnimeListActivity : AppCompatActivity() {

    private lateinit var viewModel: AnimeListViewModel
    private var networkSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_anime_list)

        setupToolbar(getString(R.string.app_name))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar)) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val progress = findViewById<ProgressBar>(R.id.progress)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AnimeDatabase.create(this)
        val repo = AnimeRepository(
            NetworkModule.api,
            db,
            db.animeDao(),
            db.animeDetailDao(),
            this
        )
        val networkMonitor =
            (application as AnimeApplication).networkMonitor

        viewModel = AnimeListViewModel(repo, networkMonitor)

        val adapter = AnimeListAdapter {
            startActivity(
                Intent(this, AnimeDetailActivity::class.java)
                    .putExtra(AppConstants.EXTRA_ANIME_ID, it.id)
                    .putExtra(AppConstants.EXTRA_ANIME_TITLE, it.title)
                    .putExtra(AppConstants.EXTRA_ANIME_POSTER_URL, it.images)
            )
        }

        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.animePaging.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { state ->

                progress.isVisible = state.refresh is LoadState.Loading

                val error = when {
                    state.refresh is LoadState.Error ->
                        (state.refresh as LoadState.Error).error

                    state.append is LoadState.Error ->
                        (state.append as LoadState.Error).error

                    else -> null
                }

                error?.let { showUserError(it) }

                val isEmpty =
                    state.refresh is LoadState.NotLoading &&
                            adapter.itemCount == 0

                if (isEmpty && !networkMonitor.isOnline.value) {
                    showOfflineEmptyState()
                }
            }
        }

        observeNetworkChanges()
    }
    private fun observeNetworkChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isOnline.collect { online ->
                    if (online) showOnlineSnackbar()
                    else showOfflineSnackbar()
                }
            }
        }
    }
    private fun showOfflineSnackbar() {
        if (networkSnackbar?.isShown == true) return

        networkSnackbar = Snackbar.make(
            findViewById(R.id.recyclerView),
            getString(R.string.offline_showing_cached),
            Snackbar.LENGTH_INDEFINITE
        )
        networkSnackbar?.show()
    }

    private fun showOnlineSnackbar() {
        networkSnackbar?.dismiss()
        networkSnackbar = null

        Snackbar.make(
            findViewById(R.id.recyclerView),
            getString(R.string.working_online),
            Snackbar.LENGTH_SHORT
        ).show()
    }
    private fun setupToolbar(title: String) {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(false)
        }

        // Extend toolbar color into status bar
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.black)
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }

    private fun showOfflineEmptyState() {
        Snackbar.make(
            findViewById(R.id.recyclerView),
            getString(R.string.offline_no_cached_data),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(R.string.retry)) {
                // Safe: Paging handles retry internally
                (findViewById<RecyclerView>(R.id.recyclerView).adapter
                        as? PagingDataAdapter<*, *>)?.retry()
            }
            .show()
    }

    private fun showUserError(error: Throwable) {
        val message = when (error) {
            is IOException ->
                getString(R.string.offline_showing_cached)
            is HttpException ->
                getString(R.string.server_error_try_again)
            else ->
                getString(R.string.something_went_wrong)
        }

        Snackbar.make(
            findViewById(R.id.recyclerView),
            message,
            Snackbar.LENGTH_LONG
        )
            .setAction(getString(R.string.retry)) {
                (findViewById<RecyclerView>(R.id.recyclerView).adapter
                        as? PagingDataAdapter<*, *>)?.retry()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_anime_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_refresh,
            R.id.action_sync -> {
                handleManualRefresh()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleManualRefresh() {

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = recyclerView.adapter as? AnimeListAdapter ?: return

        if (!viewModel.isOnline.value) {
            Snackbar.make(
                recyclerView,
                getString(R.string.you_are_offline_connect_to_internet_to_sync),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        adapter.refresh()

        Snackbar.make(
            recyclerView,
            getString(R.string.refresh_anime_list),
            Snackbar.LENGTH_SHORT
        ).show()
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val refreshItem = menu.findItem(R.id.action_refresh)
        val syncItem = menu.findItem(R.id.action_sync)

        val online = viewModel.isOnline.value
        refreshItem.isEnabled = online
        syncItem.isEnabled = online

        return super.onPrepareOptionsMenu(menu)
    }
}
