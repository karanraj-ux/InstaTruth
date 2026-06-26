package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewDownloaderScreen(
    instagramUrl: String,
    onNavigateBack: () -> Unit,
    onVideoExtracted: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Web Extractor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Automating third-party downloader...", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "If a captcha appears, please solve it.", 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val urlStr = request?.url?.toString() ?: ""
                                if (urlStr.contains(".mp4")) {
                                    onVideoExtracted(urlStr)
                                    onNavigateBack()
                                    return true
                                }
                                // Block redirects to other domains to prevent ad popups
                                val isAllowedDomain = urlStr.contains("savefrom.net") || 
                                                      urlStr.contains("instagram.com") ||
                                                      urlStr.contains("cdn") ||
                                                      urlStr.contains("fbcdn")
                                if (!isAllowedDomain) {
                                    return true // Cancel load
                                }
                                return false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (url != null && url.contains("savefrom.net")) {
                                    // Inject JavaScript to automatically paste the URL and trigger download
                                    val js = """
                                        javascript:(function() {
                                            if (!window.injectedUrlPaste) {
                                                window.injectedUrlPaste = true;
                                                var inputs = document.querySelectorAll('input[type="text"], input[type="url"], input#sf_url');
                                                if (inputs.length > 0) {
                                                    inputs[0].value = '$instagramUrl';
                                                    var buttons = document.querySelectorAll('button[type="submit"], button#sf_submit');
                                                    if (buttons.length > 0) {
                                                        inputs[0].dispatchEvent(new Event('input', { bubbles: true }));
                                                        setTimeout(function() { buttons[0].click(); }, 800);
                                                    }
                                                }
                                            }
                                            
                                            setInterval(function() {
                                                var links = document.querySelectorAll('a');
                                                for (var i = 0; i < links.length; i++) {
                                                    var href = links[i].href || '';
                                                    var text = (links[i].textContent || '').toLowerCase();
                                                    var isMp4Link = href.indexOf('.mp4') !== -1;
                                                    var hasDownloadAttr = links[i].getAttribute('download') !== null;
                                                    
                                                    if (isMp4Link || (hasDownloadAttr && text.indexOf('mp4') !== -1)) {
                                                        window.location.href = href;
                                                        break; // only click first one
                                                    }
                                                }
                                            }, 1000);
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(js, null)
                                }
                            }
                        }

                        // Listen to download events which fastdl triggers after processing
                        setDownloadListener { downloadUrl, _, _, mimetype, _ ->
                            if (downloadUrl.contains(".mp4") || mimetype.contains("video")) {
                                onVideoExtracted(downloadUrl)
                                onNavigateBack()
                            }
                        }

                        // Also attempt to intercept any direct MP4 URL clicks if the download listener misses it
                        setOnTouchListener { view, event ->
                            val hr = (view as WebView).hitTestResult
                            val extra = hr.extra
                            if (extra != null && extra.contains(".mp4")) {
                                onVideoExtracted(extra)
                                onNavigateBack()
                                true
                            } else {
                                false
                            }
                        }

                        loadUrl("https://en1.savefrom.net/11Qc/download-from-instagram")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
