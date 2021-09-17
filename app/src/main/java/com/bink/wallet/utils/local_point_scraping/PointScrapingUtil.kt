package com.bink.wallet.utils.local_point_scraping

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.*
import com.bink.wallet.model.PointScrapingResponse
import com.bink.wallet.utils.*
import com.bink.wallet.utils.local_point_scraping.agents.PointScrapeSite
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@SuppressLint("StaticFieldLeak")
object PointScrapingUtil {

    private var webView: WebView? = null

    fun performNewScrape(
        context: Context,
        pointScrapeSite: PointScrapeSite?,
        email: String?,
        password: String?,
        callback: (PointScrapingResponse) -> Unit
    ) {
        if (pointScrapeSite == null || email == null || password == null) return

        webView = getWebView(context)
        clearWebViewCookies()

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                getJavaScriptForMerchant(context, pointScrapeSite, email, password).let { javascript ->

                    if (javascript == null) {
                        SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_CLIENT, LocalPointScrapingError.SCRIPT_NOT_FOUND.issue)
                        return
                    }

                    webView?.evaluateJavascript(javascript) { responseFromSite ->

                        processResponse(responseFromSite) { serializedResponse ->

                            logDebug("LocalPointScrape", "serializedResponse: $serializedResponse")

                            if (serializedResponse == null) {
                                SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_CLIENT, LocalPointScrapingError.JS_DECODE_FAILED.issue)
                            } else {
                                with(serializedResponse) {
                                    if (pointScrapeSite == PointScrapeSite.TESCO && pointsString.isNullOrEmpty() && didAttemptLogin == true && errorMessage.isNullOrEmpty()) {
                                        //There's an issue with Tesco where it will attempt to log in, but the first time it doesn't fill in the password field.
                                        webView?.evaluateJavascript(javascript) {}
                                    }

                                    if (errorMessage != null) {
                                        if (didAttemptLogin == true) {
                                            SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_USER, "${LocalPointScrapingError.INCORRECT_CRED.issue}. Error Message: $errorMessage")
                                        } else {
                                            SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_USER, "${LocalPointScrapingError.GENERIC_FAILURE.issue}. Error Message: $errorMessage")
                                        }
                                    }

                                    if (pointsString != null) {
                                        webView?.destroy()
                                        webView = null

                                        callback(serializedResponse)
                                    }
                                }
                            }

                        }
                    }
                }

            }
        }

        webView?.loadUrl(pointScrapeSite.signInURL)
    }

    private fun getWebView(context: Context): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE;
                setAppCacheEnabled(false);
            }
            clearCache(true)
            clearFormData()
            clearHistory()
            clearSslPreferences()
        }
    }

    private fun clearWebViewCookies() {
        WebStorage.getInstance().deleteAllData()

        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun getJavaScriptForMerchant(context: Context, site: PointScrapeSite, email: String, password: String): String? {
        val javascriptClass = "lps_${site.remoteName}_navigate.txt".readFileText(context)
        return if (javascriptClass == null) {
            null
        } else {
            val replacedEmail = javascriptClass.replaceFirst("%@", email)
            val replacedPassword = replacedEmail.replaceFirst("%@", password)
            replacedPassword
        }

    }

    private fun processResponse(response: String, callback: (PointScrapingResponse?) -> Unit) {
        /**
         * Once we have a response it's being mapped to an object so we can check whether it has
         * any messages to display to the user
         */
        try {
            (Gson().fromJson(response, object : TypeToken<PointScrapingResponse?>() {}.type) as PointScrapingResponse).let { pointScrapeResponse ->
                callback(pointScrapeResponse)
            }
        } catch (e: Exception) {
            callback(null)
            //Either an issue with casting to a PointScrapeResponse or in Parsing the JSON
        }
    }
}