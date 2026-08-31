package com.xantu.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
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

    // *** هنا تحط رابط موقعك - غيره براحتك ***
    private val WEBSITE_URL = "https://reviews8.site.je/"

    private val pagesWithBackButton = listOf("password.html", "ar-vodafone.html", "cash.html")

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
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        retryButton.setOnClickListener {
            if (isInternetAvailable()) {
                errorLayout.isVisible = false
                webView.loadUrl(WEBSITE_URL)
            }
        }

        if (isInternetAvailable()) {
            webView.loadUrl(WEBSITE_URL)
        } else {
            showError()
        }
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }
        toolbar.isVisible = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
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

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) view?.loadUrl(url)
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.isVisible = true
                errorLayout.isVisible = false
                handleBackButtonVisibility(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.isVisible = false
                handleBackButtonVisibility(url)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) showError()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.isVisible = newProgress < 100
            }
        }
    }

    private fun handleBackButtonVisibility(url: String?) {
        if (url == null) {
            toolbar.isVisible = false
            return
        }
        val shouldShow = pagesWithBackButton.any { url.lowercase().contains(it.lowercase()) }
        toolbar.isVisible = shouldShow
    }

    private fun showError() {
        progressBar.isVisible = false
        errorLayout.isVisible = true
        errorText.text = "لا يوجد اتصال بالإنترنت\nتأكد من الاتصال وحاول مرة أخرى"
    }

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
