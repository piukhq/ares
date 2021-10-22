package com.bink.wallet.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.webkit.WebView
import com.bink.wallet.databinding.WebscrapeCaptchaFragmentBinding

class WebScrapeCaptchaDialog(context: Context, private val captchaWebView: WebView?, private val loginJavascript: String) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    private var timer: CountDownTimer? = null
    private lateinit var binding: WebscrapeCaptchaFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WebscrapeCaptchaFragmentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        captchaWebView?.apply {
            visibility = View.VISIBLE
            binding.webviewLayout.addView(this)
        }

        val captchaJs = "lps_morrisons_captcha.txt".readFileText(context) ?: return

        timer = object : CountDownTimer(60000, 1000) {
            override fun onFinish() {
                dismissDialog()
            }

            override fun onTick(millisUntilFinished: Long) {
                captchaWebView?.evaluateJavascript(captchaJs) { response ->
                    logDebug("LocalPointScrape", "success: $response")

                    if (response.contains("true")) {
                        captchaWebView.evaluateJavascript(loginJavascript) {
                            dismissDialog()
                        }
                    }
                }
            }
        }

        timer?.start()
    }

    private fun dismissDialog() {
        timer?.cancel()
        timer = null

        dismiss()
    }

}