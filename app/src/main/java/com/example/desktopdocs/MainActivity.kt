package com.example.desktopdocs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CHROME_PACKAGE = "com.android.chrome"
        private const val GOOGLE_DOCS_URL = "https://docs.google.com/document/u/0/"
        private const val GOOGLE_DRIVE_URL = "https://drive.google.com/drive/my-drive"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createFallbackView())

        if (savedInstanceState == null) {
            openInCustomTab(GOOGLE_DOCS_URL)
        }
    }

    private fun openInCustomTab(url: String) {
        val uri = Uri.parse(url)
        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(Color.parseColor("#0F172A"))
            .setNavigationBarColor(Color.parseColor("#0F172A"))
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()

        try {
            customTabsIntent.intent.setPackage(CHROME_PACKAGE)
            customTabsIntent.launchUrl(this, uri)
        } catch (_: ActivityNotFoundException) {
            // Fallback if Google Chrome is not installed: launch default Custom Tabs provider
            try {
                customTabsIntent.intent.setPackage(null)
                customTabsIntent.launchUrl(this, uri)
            } catch (_: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }

    private fun createFallbackView(): LinearLayout {
        val dp = { value: Int ->
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                resources.displayMetrics
            ).toInt()
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(24), dp(24), dp(24), dp(24))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 24f
            setTextColor(Color.parseColor("#F8FAFC"))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Choose a workspace to launch in Chrome Custom Tabs"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(28))
        }

        val openDocsButton = MaterialButton(this).apply {
            text = "Open Google Docs"
            isAllCaps = false
            setBackgroundColor(Color.parseColor("#2563EB"))
            setTextColor(Color.WHITE)
            cornerRadius = dp(10)
            minimumHeight = dp(52)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
            setOnClickListener { openInCustomTab(GOOGLE_DOCS_URL) }
        }

        val openDriveButton = MaterialButton(
            this,
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = "Open Google Drive"
            isAllCaps = false
            setTextColor(Color.parseColor("#E2E8F0"))
            setStrokeColorResource(android.R.color.darker_gray)
            cornerRadius = dp(10)
            minimumHeight = dp(52)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { openInCustomTab(GOOGLE_DRIVE_URL) }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(openDocsButton)
        root.addView(openDriveButton)
        return root
    }
}
