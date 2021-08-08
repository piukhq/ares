package com.bink.wallet.utils.local_point_scraping

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bink.wallet.model.PointScrapeResponse
import com.bink.wallet.utils.local_point_scraping.agents.PointScrapeSite
import com.bink.wallet.utils.local_point_scraping.captcha.WebScrapeCaptchaDialog
import com.bink.wallet.utils.logDebug
import com.bink.wallet.utils.readFileText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@SuppressLint("StaticFieldLeak")
object PointScrapingUtil {

    var lastSeenURL: String? = null
    private var webView: WebView? = null

    private var hasSignedIn = false

    fun performScrape(context: Context, pointScrapeSite: PointScrapeSite?, email: String?, password: String?, callback: (PointScrapeResponse) -> Unit) {

        hasSignedIn = false

        if (pointScrapeSite == null || email == null || password == null) {
            return
        }

        webView = WebView(context).apply {
            visibility = View.GONE
            settings.apply {
                javaScriptEnabled = true
            }
            clearCache(true)
            clearFormData()
            clearHistory()
            clearSslPreferences()
        }

        WebStorage.getInstance().deleteAllData()

        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (!lastSeenURL.equals(url) || pointScrapeSite == PointScrapeSite.SUPERDRUG || pointScrapeSite == PointScrapeSite.MORRISONS) {
                    Handler().postDelayed({
                        pointScrapeSite.let { site ->
                            getJavascript(context, url, site, email, password).let { js ->
                                logDebug("LocalPointScrape", "Evaluating JS")
                                js?.let {
                                    webView?.evaluateJavascript(it) { response ->
                                        logDebug("LocalPointScrape", "JS Response $response")
                                        processResponse(response) { pointScrapeResponse ->
                                            logDebug("LocalPointScrape", "is Done ${pointScrapeResponse.isDone()}")
                                            if (pointScrapeResponse.isDone()) {
                                                webView?.destroy()
                                                webView = null
                                                lastSeenURL = null
                                                hasSignedIn = false

                                                callback(pointScrapeResponse)
                                            }

                                            if (pointScrapeResponse.user_action_required) {
                                                val dialog = WebScrapeCaptchaDialog(context, webView, js ?: "")
                                                dialog.show()
                                                dialog.setOnDismissListener {
                                                    webView?.loadUrl(pointScrapeSite.scrapeURL)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, 1000)

                }

                lastSeenURL = url
            }
        }

        webView?.loadUrl(pointScrapeSite.signInURL)
    }

    private fun getJavascript(context: Context, url: String?, pointScrapeSite: PointScrapeSite, email: String, password: String): String? {
        if (url == null) return null

        logDebug("LocalPointScrape", "URL: $url")

        for (agent in WebScrapableManager.scrapableAgents) {
            if (agent.merchant == pointScrapeSite) {
                val merchantName = agent.merchant.remoteName
                return when {
                    url.toLowerCase().contains(agent.merchant.signInURL.toLowerCase()) && !hasSignedIn -> {
                        hasSignedIn = true
                        val javascriptClass = "lps_${merchantName}_login.txt".readFileText(context)
                        val replacedEmail = javascriptClass.replaceFirst("%@", email)
                        val replacedPassword = replacedEmail.replaceFirst("%@", password)
                        replacedPassword
                    }
                    url.toLowerCase().contains(agent.merchant.scrapeURL.toLowerCase()) -> {
                        "lps_${merchantName}_scrape.txt".readFileText(context)
                    }
                    else -> null
                }
            }
        }

        return null

    }

    private fun processResponse(response: String, callback: (PointScrapeResponse) -> Unit) {
        /**
         * Once we have a response it's being mapped to an object so we can check whether it has
         * any messages to display to the user
         */
        try {
            (Gson().fromJson(response, object : TypeToken<PointScrapeResponse?>() {}.type) as PointScrapeResponse).let { pointScrapeResponse ->
                callback(pointScrapeResponse)
            }
        } catch (e: Exception) {
            //Either an issue with casting to a PointScrapeResponse or in Parsing the JSON
        }
    }

}