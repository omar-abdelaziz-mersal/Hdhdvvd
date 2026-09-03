package com.xantu.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var retryButton: Button
    private lateinit var errorText: TextView

    private val WEBSITE_URL = "file:///android_asset/index.html"

    private val pagesWithBackButton = listOf(
        "ar-vodafone.html", "cash.html", "password.html",
        "api.html", "conversion.html", "details.html"
    )
    private val protectedPages = listOf(
        "ar-vodafone.html", "cash.html", "password.html",
        "api.html", "conversion.html", "details.html"
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        errorLayout = findViewById(R.id.errorLayout)
        toolbar = findViewById(R.id.topToolbar)
        retryButton = findViewById(R.id.retryButton)
        errorText = findViewById(R.id.errorText)
        setupToolbar()
        setupWebView()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (webView.canGoBack()) webView.goBack() else finish() }
        })
        retryButton.setOnClickListener {
            errorLayout.isVisible = false
            webView.loadUrl(WEBSITE_URL)
        }
        webView.loadUrl(WEBSITE_URL)
    }

    private fun setupToolbar() {
        toolbar.setBackgroundColor(Color.TRANSPARENT)
        (toolbar.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        toolbar.elevation = 0f
        toolbar.title = ""
        toolbar.subtitle = ""
        toolbar.setNavigationIcon(R.drawable.ic_back_new)
        toolbar.setNavigationOnClickListener { if (webView.canGoBack()) webView.goBack() }
        toolbar.isVisible = false
        (toolbar.parent as? View)?.isVisible = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.loadsImagesAutomatically = true
        settings.setSupportZoom(false)
        settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return if (url.startsWith("file:///android_asset/")) false else true
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.isVisible = true
                errorLayout.isVisible = false
                updatePageFeatures(url)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.isVisible = false
                updatePageFeatures(url)
            }
        }
        webView.webChromeClient = WebChromeClient()
    }

    private fun updatePageFeatures(url: String?) {
        if (url == null) {
            toolbar.isVisible = false
            (toolbar.parent as? View)?.isVisible = false
            disableScreenshotProtection()
            return
        }
        val lowerUrl = url.lowercase()
        val shouldShow = pagesWithBackButton.any { lowerUrl.contains(it.lowercase()) && !lowerUrl.contains("index.html") }
        toolbar.isVisible = shouldShow
        (toolbar.parent as? View)?.isVisible = shouldShow
        if (protectedPages.any { lowerUrl.contains(it.lowercase()) }) enableScreenshotProtection() else disableScreenshotProtection()
    }

    private fun enableScreenshotProtection() { window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE) }
    private fun disableScreenshotProtection() { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
}
