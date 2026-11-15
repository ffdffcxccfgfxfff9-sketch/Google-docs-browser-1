package com.example.desktopdocs

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var currentScale = 0.45f

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        setupWebView()
        setupBackNavigation()
        setupActionButtons()

        webView.loadUrl("https://docs.google.com")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        // تفعيل الكوكيز لحفظ تسجيل الدخول في حساب Google
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // إبقاء التصفح داخل WebView وعدم التحويل لتطبيق خارجي
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectMobileFriendlyCSS()
            }
        }
    }

    private fun injectMobileFriendlyCSS() {
        val jsCode = """
            (function() {
                var meta = document.querySelector('meta[name="viewport"]');
                if (!meta) {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    document.head.appendChild(meta);
                }
                meta.content = 'width=1024, initial-scale=0.45, maximum-scale=3, user-scalable=yes';
                var existing = document.getElementById('docsdroid-injected-css');
                if (existing) existing.remove();
                var style = document.createElement('style');
                style.id = 'docsdroid-injected-css';
                style.textContent = "/* جعل شريط الأدوات قابلاً للسحب أفقياً باللمس */\n#docs-toolbar-wrapper {\n    overflow-x: auto !important;\n    white-space: nowrap !important;\n    -webkit-overflow-scrolling: touch !important;\n}\n\n/* تقليص الهوامش الجانبية غير الضرورية لتوفير مساحة للورقة */\n.kix-appview-editor {\n    overflow-x: auto !important;\n}\n#docs-chrome {\n    max-width: 100vw !important;\n}";
                document.head.appendChild(style);
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    private fun setupActionButtons() {
        findViewById<FloatingActionButton>(R.id.fabZoom).setOnClickListener {
            currentScale = when {
                currentScale < 0.65f -> 0.75f
                currentScale < 0.95f -> 1.0f
                else -> 0.45f
            }
            val jsZoom = "document.querySelector('meta[name=\"viewport\"]')?.setAttribute('content', 'width=1024, initial-scale=' + $currentScale + ', maximum-scale=3, user-scalable=yes');"
            webView.evaluateJavascript(jsZoom, null)
        }
        findViewById<FloatingActionButton>(R.id.fabExportPdf).setOnClickListener {
            exportDocumentToPdf()
        }
    }

    private fun exportDocumentToPdf() {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Desktop Docs_Document"
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
}
