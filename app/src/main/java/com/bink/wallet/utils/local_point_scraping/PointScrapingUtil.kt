package com.bink.wallet.utils.local_point_scraping

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.*
import com.bink.wallet.model.LocalPointsAgent
import com.bink.wallet.model.PointScrapingResponse
import com.bink.wallet.utils.LocalPointScrapingError
import com.bink.wallet.utils.SentryErrorType
import com.bink.wallet.utils.SentryUtils
import com.bink.wallet.utils.logDebug
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.charset.StandardCharsets
import java.util.*

@SuppressLint("StaticFieldLeak")
object PointScrapingUtil {

    private var webView: WebView? = null

    fun performNewScrape(
        context: Context,
        isAddCard: Boolean,
        localPointsAgent: LocalPointsAgent?,
        email: String?,
        password: String?,
        logError: (String) -> Unit,
        callback: (PointScrapingResponse) -> Unit
    ) {
        if (localPointsAgent == null || email == null || password == null) return

        /**
         * We start by ensuring we've have a completely clear webview.
         * This ensures that the last account we signed in to isnt cached.
         */

        webView = getWebView(context)
        clearWebViewCookies()

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                /**
                 * onPageFinished is called every time a new URL is successfully loaded in our webview.
                 *
                 * Once we're on a new page we will execute our javascript file that is stored in
                 * remote config.
                 */

                getJavaScriptForMerchant(localPointsAgent, email, password) { javascript ->

                    if (javascript == null) {
                        logError(LocalPointScrapingError.SCRIPT_NOT_FOUND.issue)
                        SentryUtils.logError(
                            SentryErrorType.LOCAL_POINTS_SCRAPE_CLIENT,
                            LocalPointScrapingError.SCRIPT_NOT_FOUND.issue,
                            WebScrapableManager.currentAgent?.merchant,
                            isAddCard
                        )
                    } else {
                        webView?.evaluateJavascript(javascript) { responseFromSite ->

                            processResponse(responseFromSite) { serializedResponse ->

                                logDebug(
                                    "LocalPointScrape",
                                    "serializedResponse: $serializedResponse"
                                )

                                if (serializedResponse == null) {
                                    logError(LocalPointScrapingError.JS_DECODE_FAILED.issue)
                                    SentryUtils.logError(
                                        SentryErrorType.LOCAL_POINTS_SCRAPE_CLIENT,
                                        LocalPointScrapingError.JS_DECODE_FAILED.issue,
                                        WebScrapableManager.currentAgent?.merchant,
                                        isAddCard
                                    )
                                } else {
                                    with(serializedResponse) {

                                        /**
                                         * There's an issue with Tesco where it will attempt to log in
                                         * but the first time it doesn't fill in the password field.
                                         * So we need to manually re-execute the JS
                                         */

                                        if (localPointsAgent.merchant == "tesco" && pointsString.isNullOrEmpty() && didAttemptLogin == true && errorMessage.isNullOrEmpty()) {
                                            webView?.evaluateJavascript(javascript) {}
                                        }

                                        /**
                                         * If there is an error and we've attempted to login, we can assume there is incorrect credentials.
                                         * If there is just an error then we have a generic failure event.
                                         */

                                        if (errorMessage != null) {
                                            if (didAttemptLogin == true) {
                                                logError(LocalPointScrapingError.INCORRECT_CRED.issue)
                                                SentryUtils.logError(
                                                    SentryErrorType.LOCAL_POINTS_SCRAPE_USER,
                                                    "${LocalPointScrapingError.INCORRECT_CRED.issue}. Error Message: $errorMessage",
                                                    WebScrapableManager.currentAgent?.merchant,
                                                    isAddCard
                                                )
                                                callback(serializedResponse)
                                            } else {
                                                logError(LocalPointScrapingError.GENERIC_FAILURE.issue)
                                                SentryUtils.logError(
                                                    SentryErrorType.LOCAL_POINTS_SCRAPE_USER,
                                                    "${LocalPointScrapingError.GENERIC_FAILURE.issue}. Error Message: $errorMessage",
                                                    WebScrapableManager.currentAgent?.merchant,
                                                    isAddCard
                                                )
                                            }
                                        }

                                        /**
                                         * Once we have our points string, we will destroy the webview
                                         * and return the value.
                                         */

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
        }

        webView?.loadUrl(localPointsAgent.points_collection_url)
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

    private fun getJavaScriptForMerchant(
        site: LocalPointsAgent,
        email: String,
        password: String,
        callback: (String?) -> Unit
    ) {
        val storageRef =
            Firebase.storage.reference.child("local-points-collection/${site.merchant.lowercase(Locale.getDefault())}.js")

        storageRef.getBytes(1024 * 1024).addOnSuccessListener {
            val javascriptClass = String(it, StandardCharsets.UTF_8)
            val replacedEmail = javascriptClass.replaceFirst("%@", email)
            val replacedPassword = replacedEmail.replaceFirst("%@", password)
            callback(replacedPassword)
        }.addOnFailureListener {
            callback(null)
        }
    }

    private fun processResponse(response: String, callback: (PointScrapingResponse?) -> Unit) {
        /**
         * Once we have a response it's being mapped to an object so we can check whether it has
         * any messages to display to the user
         */
        try {
            (Gson().fromJson(
                response,
                object : TypeToken<PointScrapingResponse?>() {}.type
            ) as PointScrapingResponse).let { pointScrapeResponse ->
                callback(pointScrapeResponse)
            }
        } catch (e: Exception) {
            callback(null)
            //Either an issue with casting to a PointScrapeResponse or in Parsing the JSON
        }
    }
}