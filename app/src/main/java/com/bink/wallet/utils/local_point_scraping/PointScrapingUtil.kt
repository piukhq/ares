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

@SuppressLint("StaticFieldLeak")
object PointScrapingUtil {

    private var webView: WebView? = null

    fun performNewScrape(context: Context, isAddCard: Boolean, localPointsAgent: LocalPointsAgent?, email: String?, password: String?, callback: (PointScrapingResponse) -> Unit) {
        if (localPointsAgent == null || email == null || password == null) return

        webView = getWebView(context)
        clearWebViewCookies()

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                getJavaScriptForMerchant(localPointsAgent, email, password) { javascript ->

                    if (javascript == null) {
                        SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_CLIENT, LocalPointScrapingError.SCRIPT_NOT_FOUND.issue, WebScrapableManager.currentAgent?.merchant, isAddCard)
                    } else {
                        webView?.evaluateJavascript(javascript) { responseFromSite ->

                            processResponse(responseFromSite) { serializedResponse ->

                                logDebug("LocalPointScrape", "serializedResponse: $serializedResponse")

                                if (serializedResponse == null) {
                                    SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_CLIENT, LocalPointScrapingError.JS_DECODE_FAILED.issue, WebScrapableManager.currentAgent?.merchant, isAddCard)
                                } else {
                                    with(serializedResponse) {
                                        if (localPointsAgent.merchant == "tesco" && pointsString.isNullOrEmpty() && didAttemptLogin == true && errorMessage.isNullOrEmpty()) {
                                            //There's an issue with Tesco where it will attempt to log in, but the first time it doesn't fill in the password field.
                                            webView?.evaluateJavascript(javascript) {}
                                        }

                                        if (errorMessage != null) {
                                            if (didAttemptLogin == true) {
                                                SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_USER, "${LocalPointScrapingError.INCORRECT_CRED.issue}. Error Message: $errorMessage", WebScrapableManager.currentAgent?.merchant, isAddCard)
                                            } else {
                                                SentryUtils.logError(SentryErrorType.LOCAL_POINTS_SCRAPE_USER, "${LocalPointScrapingError.GENERIC_FAILURE.issue}. Error Message: $errorMessage", WebScrapableManager.currentAgent?.merchant, isAddCard)
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

    private fun getJavaScriptForMerchant(site: LocalPointsAgent, email: String, password: String, callback: (String?) -> Unit) {
        val storageRef = Firebase.storage.reference.child("local-points-collection/${site.merchant.toLowerCase()}.js")

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
            (Gson().fromJson(response, object : TypeToken<PointScrapingResponse?>() {}.type) as PointScrapingResponse).let { pointScrapeResponse ->
                callback(pointScrapeResponse)
            }
        } catch (e: Exception) {
            callback(null)
            //Either an issue with casting to a PointScrapeResponse or in Parsing the JSON
        }
    }
}