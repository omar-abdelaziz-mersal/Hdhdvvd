package com.xantu.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    private val WEBSITE_URL = "https://reviews8.site.je/"

    // ================== اكواد المسارات ==================
    // 1- مسارات زر الرجوع
    private val pagesWithBackButton = listOf(
        "ar-vodafone.html",
        "cash.html",
        "password.html"
    )

    // 2- مسارات منع السكرين شوت
    private val protectedPages = listOf(
        "ar-vodafone.html",
        "cash.html",
        "password.html"
    )
    // ===================================================

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
            if (isInternetAvailable()) {
                errorLayout.isVisible = false
                webView.loadUrl(WEBSITE_URL)
            }
        }
        if (isInternetAvailable()) webView.loadUrl(WEBSITE_URL) else showError()
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { if (webView.canGoBack()) webView.goBack() }
        toolbar.isVisible = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportZoom(false)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.mediaPlaybackRequiresUserGesture = false
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { if (url != null) view?.loadUrl(url); return true }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean { view?.loadUrl(request?.url.toString()); return true }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { super.onPageStarted(view, url, favicon); progressBar.isVisible = true; errorLayout.isVisible = false; updatePageFeatures(url) }
            override fun onPageFinished(view: WebView?, url: String?) { super.onPageFinished(view, url); progressBar.isVisible = false; updatePageFeatures(url) }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) { super.onReceivedError(view, request, error); if (request?.isForMainFrame == true) showError() }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) { progressBar.isVisible = newProgress < 100 }
            override fun onPermissionRequest(request: PermissionRequest?) { request?.grant(request.resources) }
        }
    }

    private fun updatePageFeatures(url: String?) {
        if (url == null) { toolbar.isVisible = false; disableScreenshotProtection(); return }
        val lowerUrl = url.lowercase()
        toolbar.isVisible = pagesWithBackButton.any { lowerUrl.contains(it.lowercase()) }
        val shouldProtect = protectedPages.any { lowerUrl.contains(it.lowercase()) }
        if (shouldProtect) enableScreenshotProtection() else disableScreenshotProtection()
    }

    private fun enableScreenshotProtection() { window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE) }
    private fun disableScreenshotProtection() { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    private fun showError() { progressBar.isVisible = false; errorLayout.isVisible = true; errorText.text = "لا يوجد اتصال" }
    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    override fun onPause() { super.onPause(); webView.onPause() }
    override fun onResume() { super.onResume(); webView.onResume(); webView.resumeTimers() }
}
