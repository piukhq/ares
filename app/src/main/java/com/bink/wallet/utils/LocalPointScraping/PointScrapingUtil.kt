package com.bink.wallet.utils.LocalPointScraping

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bink.wallet.model.PointScrapeResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PointScrapingUtil {

    var lastSeenURL: String? = null
    private var webView: WebView? = null

    fun performScrape(context: Context, pointScrapeSite: PointScrapeSite?, email: String?, password: String?, callback: (PointScrapeResponse) -> Unit) {

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

                //In the case of Tesco, it re-directs you to the login address before signing you in, there for this check stops the first login script from running again
                if (!lastSeenURL.equals(url)) {
                    Handler().postDelayed({
                        pointScrapeSite.let { site ->
                            getJavascript(context, url, site, email, password).let { js ->
                                logDebug("LocalPointScrape", "Evaluating JS")
                                webView?.evaluateJavascript(js) { response ->
                                    logDebug("LocalPointScrape", "JS Response $response")
                                    processResponse(response) { pointScrapeResponse ->
                                        logDebug("LocalPointScrape", "is Done ${pointScrapeResponse.isDone()}")
                                        if (pointScrapeResponse.isDone()) {
                                            webView?.destroy()
                                            webView = null
                                            lastSeenURL = null
                                            
                                            callback(pointScrapeResponse)
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
        if(url == null) return null
        when (pointScrapeSite) {
            /**
             * To add a new site, you just need to add the new case, for e.g Morrisons, then underneath add all
             * used URL's to the corresponding Javascript classes
             */
            PointScrapeSite.TESCO -> {
                logDebug("LocalPointScrape","Getting JS for $url")
                return when {
                    url.toLowerCase().contains(PointScrapeSite.TESCO.signInURL.toLowerCase()) -> {
                        val javascriptClass = readFileText(context, "lps_tesco_login.txt")
                        val replacedEmail = javascriptClass.replaceFirst("%@", email)
                        val replacedPassword = replacedEmail.replaceFirst("%@", password)
                        replacedPassword
                    }
                    url.toLowerCase().contains(PointScrapeSite.TESCO.scrapeURL.toLowerCase()) -> {
                        readFileText(context, "lps_tesco_scrape.txt")
                    }
                    else -> null
                }
            }
        }
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

    private fun readFileText(context: Context, fileName: String): String {
        return try {
            context.assets?.open(fileName)?.bufferedReader().use {
                it?.readText() ?: "JS Error"
            }
        } catch (e: Exception) {
            e.localizedMessage ?: ""
        }
    }

    private fun launchDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Tesco LPS")
            .setMessage(message)
            .setPositiveButton("Okay", null)
            .show()
    }

}