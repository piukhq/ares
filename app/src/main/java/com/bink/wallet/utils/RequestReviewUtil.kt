package com.bink.wallet.utils

import androidx.fragment.app.Fragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.data.SharedPreferenceManager
import com.google.android.play.core.review.ReviewManagerFactory


object RequestReviewUtil {

    private const val twoDaysInMillis = 172800000

    fun triggerViaCards(fragment: Fragment?, reviewRequested: () -> Unit) {
        fragment?.let {
            SharedPreferenceManager.firstOpenDate?.let { firstOpenDate ->
                if ((System.currentTimeMillis() - firstOpenDate.toLong()) > twoDaysInMillis) {
                    if (SharedPreferenceManager.totalOpenCount > 10) {
                        requestReviewFlow(fragment, reviewRequested)
                    }
                }
            }
        }
    }

    fun triggerViaWallet(fragment: Fragment?, reviewRequested: () -> Unit) {
        fragment?.let {
            if (SharedPreferenceManager.hasAddedNewPll) {
                SharedPreferenceManager.hasAddedNewPll = false
                requestReviewFlow(fragment, reviewRequested)
            }
        }
    }

    fun triggerViaCardDetails(fragment: Fragment?, reviewRequested: () -> Unit) {
        fragment?.let {
            if (SharedPreferenceManager.hasNewTransactions) {
                SharedPreferenceManager.hasNewTransactions = false
                requestReviewFlow(fragment, reviewRequested)
            }
        }
    }

    fun recordAppOpen() {
        val firstOpenDate = SharedPreferenceManager.firstOpenDate
        if (firstOpenDate.isNullOrEmpty()) {
            SharedPreferenceManager.firstOpenDate = System.currentTimeMillis().toString()
        }

        SharedPreferenceManager.totalOpenCount = SharedPreferenceManager.totalOpenCount + 1
    }

    private fun requestReviewFlow(fragment: Fragment, reviewRequested: () -> Unit) {
        try {
            val activity = fragment.requireActivity()

            val reviewManager = ReviewManagerFactory.create(activity)

            if (!hasReviewedInThisVersion()) {
                val isReviewEnabled = RemoteConfigUtil().appConfig?.in_app_review_enabled

                if (isReviewEnabled == true) {
                    val requestReview = reviewManager.requestReviewFlow()
                    reviewRequested()

                    requestReview.addOnCompleteListener { request ->
                        if (request.isSuccessful) {
                            val reviewFlow = reviewManager.launchReviewFlow(activity, request.result)

                            reviewFlow.addOnCompleteListener {

                                currentMinorVersion()?.let { currentMinor ->
                                    SharedPreferenceManager.lastReviewedMinor = currentMinor
                                }
                            }
                        }
                    }
                }
            }

        } catch (e: IllegalStateException) {
            //not attached to an activity
        }
    }

    private fun hasReviewedInThisVersion(): Boolean {
        currentMinorVersion()?.let { currentMinor ->
            SharedPreferenceManager.lastReviewedMinor?.let { lastReviewedMinor ->
                if (lastReviewedMinor.isNotEmpty()) {
                    return currentMinor.equals(lastReviewedMinor)
                }
            }
        }

        return false
    }

    private fun currentMinorVersion(): String? {
        /**
         * This gets the current version code, then splits it out in to major, minor and patch.
         * Index 1 'should' be the minor version
         */
        val currentVersionName = BuildConfig.VERSION_NAME

        return try {
            currentVersionName.split(".")[1]
        } catch (e: Exception) {
            //Error getting current minor version
            return null
        }
    }

}