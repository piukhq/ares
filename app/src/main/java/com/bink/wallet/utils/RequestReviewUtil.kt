package com.bink.wallet.utils

import androidx.fragment.app.FragmentActivity
import com.bink.wallet.BuildConfig
import com.bink.wallet.data.SharedPreferenceManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object RequestReviewUtil {
    
    fun requestReviewFlow(activity: FragmentActivity?) {
        val reviewManager = ReviewManagerFactory.create(activity)

        if (!hasReviewedInThisVersion()) {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val isReviewEnabled = remoteConfig.getString(REMOTE_CONFIG_REVIEW_ENABLED)

            if (isReviewEnabled.toLowerCase().equals("true")) {
                val requestReview = reviewManager.requestReviewFlow()

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
    }

    private fun hasReviewedInThisVersion(): Boolean {
        currentMinorVersion()?.let { currentMinor ->
            SharedPreferenceManager.lastReviewedMinor?.let { lastReviewedMinor ->
                if (!lastReviewedMinor.isNullOrEmpty()) {
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
            currentVersionName?.split(".")?.get(1)
        } catch (e: Exception) {
            //Error getting current minor version
            return null
        }
    }

}