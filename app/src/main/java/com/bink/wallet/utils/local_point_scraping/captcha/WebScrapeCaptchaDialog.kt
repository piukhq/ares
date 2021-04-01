package com.bink.wallet.utils.local_point_scraping.captcha

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.webkit.WebView
import com.bink.wallet.R
import com.bink.wallet.model.PointScrapeResponse
import com.bink.wallet.utils.local_point_scraping.PointScrapingUtil
import com.bink.wallet.utils.logDebug
import com.bink.wallet.utils.readFileText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.webscrape_captcha_fragment.*

class WebScrapeCaptchaDialog(context: Context, private val captchaWebView: WebView?, private val loginJavascript: String) : Dialog(context) {

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webscrape_captcha_fragment)

        captchaWebView?.apply {
            visibility = View.VISIBLE
            webview_layout.addView(this)
        }

        val captchaJs = "lps_morrisons_captcha.txt".readFileText(context)

        timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                captchaWebView?.evaluateJavascript(captchaJs) { response ->
                    logDebug("LocalPointScrape", "success: $response")

                    if(response.contains("true")){
                        captchaWebView?.evaluateJavascript(loginJavascript) { logInResponse ->
                            timer?.cancel()
                            timer = null

                            dismiss()
                            //Now we've logged in we need to go to the point page
                        }
                    }
                }
            }
        }

        timer?.start()
    }

}