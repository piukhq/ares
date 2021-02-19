package com.bink.wallet.utils.LocalPointScraping

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout
import com.bink.wallet.model.PointScrapeResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PointScrapingUtil {

    private var lastSeenURL: String? = null
    private var webView: WebView? = null

    fun performScrape(context: Context, pointScrapeSite: PointScrapeSite?, parentView: ConstraintLayout?, email: String?, password: String?, callback: (String?, Boolean) -> Unit) {
        /**
         * To open a webview for scraping we also need a view to attach it to, otherwise the webview will automatically
         * open up inside of Google Chrome outside of the app
         */

        if (pointScrapeSite == null || parentView == null || email == null || password == null) {
            Log.d("LocalPointScrape", "Null data")
            return
        }

        Log.d("LocalPointScrape", "Performing Scrape")

        webView = WebView(context).apply {
            visibility = View.GONE
            settings.apply {
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
        }

        parentView.addView(webView)

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                //In the case of Tesco, it re-directs you to the login address before signing you in, there for this check stops the first login script from running again
                if (!lastSeenURL.equals(url)) {
                    Handler().postDelayed({
                        pointScrapeSite.let { site ->
                            Log.d("LocalPointScrape", "Scraping ${site.name}")
                            getJavascript(context, url, site, email, password).let { js ->
                                Log.d("LocalPointScrape", "Javascript being evaluated")
                                webView?.evaluateJavascript(js) { response ->
                                    Log.d("LocalPointScrape", "${site.name} responded with $response")
                                    processResponse(response) { message, isDone ->
                                        if (isDone) {
                                            webView?.destroy()
                                            lastSeenURL = null
                                        }
                                        callback(message, isDone)
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
        when (pointScrapeSite) {
            /**
             * To add a new site, you just need to add the new case, for e.g Morrisons, then underneath add all
             * used URL's to the corresponding Javascript classes
             */
            PointScrapeSite.TESCO -> {
                return when (url) {
                    PointScrapeSite.TESCO.signInURL -> {
                        val javascriptClass = readFileText(context, "lps_tesco_login.txt")
                        val replacedEmail = javascriptClass.replaceFirst("%@", email)
                        val replacedPassword = replacedEmail.replaceFirst("%@", password)
                        replacedPassword
                    }
                    PointScrapeSite.TESCO.scrapeURL -> {
                        readFileText(context, "lps_tesco_scrape.txt")
                    }
                    else -> null
                }
            }
        }
    }

    private fun processResponse(response: String, callback: (String?, Boolean) -> Unit) {
        /**
         * Once we have a response it's being mapped to an object so we can check whether it has
         * any messages to display to the user
         */
        try {
            (Gson().fromJson(response, object : TypeToken<PointScrapeResponse?>() {}.type) as PointScrapeResponse).let { pointScrapeResponse ->
                if (pointScrapeResponse.success) {
                    pointScrapeResponse.points?.let { points ->
                        callback(points, true)
                    }
                } else {
                    pointScrapeResponse.error_message?.let { error ->
                        callback(error, false)
                    }
                }
            }
        } catch (e: Exception) {
            //Either an issue with casting to a PointScrapeResponse or in Parsing the JSON
            callback(e.localizedMessage, false)
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