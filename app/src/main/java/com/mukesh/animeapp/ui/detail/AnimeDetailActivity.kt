package com.mukesh.animeapp.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.mukesh.animeapp.app.AnimeApplication
import com.mukesh.animeapp.R
import com.mukesh.animeapp.constants.AppConstants
import com.mukesh.animeapp.data.api.NetworkModule
import com.mukesh.animeapp.data.db.AnimeDatabase
import com.mukesh.animeapp.data.model.AnimeDetail
import com.mukesh.animeapp.data.respository.AnimeRepository
import com.mukesh.animeapp.databinding.ActivityAnimeDetailBinding
import com.mukesh.animeapp.util.Resource
import kotlinx.coroutines.launch

class AnimeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimeDetailBinding
    private lateinit var genreAdapter: AnimeGenreAdapter
    private lateinit var viewModel: AnimeDetailViewModel
    private var networkSnackbar: Snackbar? = null

    private lateinit var posterUrl: String
    private var isFullscreen = false
    private var titleExtra: String = ""
    private var isOnline = true
    private var id = -1

    private lateinit var insetsController: WindowInsetsControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        insetsController = WindowInsetsControllerCompat(window, window.decorView)

        binding = ActivityAnimeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getIntExtra(AppConstants.EXTRA_ANIME_ID, -1)
        posterUrl = intent.getStringExtra(AppConstants.EXTRA_ANIME_POSTER_URL) ?: ""
        titleExtra = intent.getStringExtra(AppConstants.EXTRA_ANIME_TITLE)
            ?: getString(R.string.anime_details_title)

        setupToolbar()
        setupWebView()
        setupUI()
        setupViewModel()
        observeNetworkChanges()
        setupBackHandler()
        loadDetails(id)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        updateToolbarTitle(titleExtra)

        binding.toolbar.setNavigationOnClickListener {
            if (isFullscreen) exitFullscreen()
            else onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootFrame) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, top, 0, 0)
            insets
        }
    }

    private fun updateToolbarTitle(title: String?) {
        val safeTitle = title ?: titleExtra
        binding.toolbar.title = safeTitle
        binding.title.text = safeTitle
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        binding.webview.webChromeClient = FullscreenChromeClient()
        binding.webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val host = request.url.host?.lowercase() ?: return false
                return host.contains("youtube.com") || host.contains("youtu.be")
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                showFallbackText(getString(R.string.error_loading_trailer))
            }
        }
    }

    private inner class FullscreenChromeClient : WebChromeClient() {

        private var fullscreenView: View? = null
        private var callback: CustomViewCallback? = null

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            if (fullscreenView != null) {
                callback.onCustomViewHidden()
                return
            }

            fullscreenView = view
            this.callback = callback
            isFullscreen = true

            binding.toolbar.visibility = View.GONE

            (window.decorView as FrameLayout).addView(
                view,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            insetsController.hide(
                WindowInsetsCompat.Type.systemBars()
            )
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        override fun onHideCustomView() {
            exitFullscreen()
        }
    }

    private fun exitFullscreen() {
        val decorView = window.decorView as FrameLayout
        decorView.removeViewAt(decorView.childCount - 1)

        binding.toolbar.visibility = View.VISIBLE
        insetsController.show(WindowInsetsCompat.Type.systemBars())

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        isFullscreen = false
    }

    // ---------------- UI ----------------

    private fun setupUI() {
        genreAdapter = AnimeGenreAdapter()
        binding.genresRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.genresRecycler.adapter = genreAdapter
    }

    // ---------------- ViewModel ----------------

    private fun setupViewModel() {
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

        viewModel = AnimeDetailViewModel(repo, networkMonitor)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { resource ->
                    when (resource) {
                        is Resource.Success -> bindAnimeData(resource.data)
                        is Resource.Error ->
                            showUserError(resource.message)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun bindAnimeData(anime: AnimeDetail) {
        updateToolbarTitle(anime.title)
        binding.rating.text = "⭐ ${anime.score ?: "N/A"}"
        binding.episodes.text = "${anime.episodes ?: "N/A"} Episodes"
        binding.synopsis.text = anime.synopsis ?: getString(R.string.no_synopsis)

        genreAdapter.submitList(anime.genres)

        val trailer = anime.trailer?.embedUrl ?: anime.trailer?.youtubeId
        when {
            !isOnline -> {
                showFallbackPoster(posterUrl)
            }
            !trailer.isNullOrBlank() -> {
                loadTrailer(trailer)
            }
            else -> {
                showFallbackPoster(posterUrl)            }
        }
    }

    // ---------------- Helpers ----------------

    private fun loadTrailer(value: String) {
        binding.trailerFallback.visibility = View.GONE
        val url = if (value.contains("embed")) value
        else "https://www.youtube-nocookie.com/embed/$value"
        loadEmbedUrl(url)
    }

    private fun loadEmbedUrl(embedUrl: String) {
        binding.webview.visibility = View.VISIBLE
        binding.trailerFallback.visibility = View.GONE
        binding.trailerImg.visibility = View.GONE
        val html = """ <!DOCTYPE html> <html> 
            <head> 
            <meta name="viewport" 
            content="width=device-width, 
            initial-scale=1"> 
            <style> 
            body { margin: 0; padding: 0; background: #000; overflow:hidden; height:100vh; }
             .video-container { position: absolute; top:0; left:0; width: 100%; height: 100%; border:0; }
              iframe { width: 100%; height: 100%; border: 0; } </style> </head> <body> <div class="video-container"> <iframe src="$embedUrl" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen> </iframe> </div> </body> </html> """.trimIndent()
        binding.webview.loadDataWithBaseURL( "https://www.youtube-nocookie.com", html, "text/html", "UTF-8", null ) }

    private fun showFallbackText(message: String) {
        binding.trailerFallback.text = message
        binding.trailerFallback.visibility = View.VISIBLE
        binding.webview.visibility = View.GONE
        binding.trailerImg.visibility = View.GONE
    }

    private fun showFallbackPoster(url: String) {
        binding.trailerFallback.visibility = View.GONE
        binding.trailerImg.visibility = View.VISIBLE
        Glide.with(this).load(url).into(binding.trailerImg)
    }

    private fun observeNetworkChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isOnline.collect { online ->
                    isOnline = online

                    if (!online) {
                        showFallbackText(getString(R.string.error_loading_trailer))
                        if (binding.webview.visibility == View.VISIBLE) {
                            showFallbackText(getString(R.string.error_loading_trailer))
                        }
                    } else {
                        networkSnackbar?.dismiss()
                        networkSnackbar = null
                        loadDetails(id)                    }
                }
            }
        }
    }


    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this) {
            if (isFullscreen) exitFullscreen()
            else finish()
        }
    }

    private fun showUserError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun loadDetails(id: Int) {
        if (id != -1) {
            viewModel.load(id)
        } else {
            showUserError(getString(R.string.invalid_anime_id))
            finish()
        }
    }
}
