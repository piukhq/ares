package com.bink.wallet.utils

import android.os.CountDownTimer

class WebscrapeTimer {

    companion object {
        private const val millisInFuture: Long = 20000
        private const val countDownInterval: Long = 1000
    }

    private var timer: CountDownTimer? = null
    private lateinit var finishCallback: () -> Unit

    fun startTimer(finish: () -> Unit) {
        cancelTimer()
        if (timer == null) {
            finishCallback = finish
            timer = object : CountDownTimer(millisInFuture, countDownInterval) {
                override fun onFinish() {
                    logDebug("LocalPointScrape", "onFinish Called")
                    finish()
                    cancelTimer()
                }

                override fun onTick(millisUntilFinished: Long) {
                }
            }
        }

        timer?.start()

    }

    fun refreshTimer() {
        if (this::finishCallback.isInitialized) {
            startTimer(finishCallback)
        }
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

}