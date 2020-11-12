package com.bink.wallet.utils

import android.app.Activity
import android.content.pm.PackageManager
import com.google.android.play.core.review.ReviewManagerFactory

class RequestReviewUtil(val activity: Activity) {

    private val reviewManager = ReviewManagerFactory.create(activity)

    fun requestReviewFlow() {
        if (!hasReviewedInThisVersion()) {
            val requestReview = reviewManager.requestReviewFlow()

            requestReview.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewFlow = reviewManager.launchReviewFlow(activity, request.result)

                    reviewFlow.addOnCompleteListener {

                        currentMinorVersion()?.let { currentMinor ->
                            LocalStoreUtils.setAppSharedPref(LocalStoreUtils.KEY_LAST_REVIEW_MINOR, currentMinor)
                        }
                    }
                }
            }
        }
    }

    private fun hasReviewedInThisVersion(): Boolean {
        currentMinorVersion()?.let { currentMinor ->
            LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_LAST_REVIEW_MINOR)?.let { lastReviewedMinor ->
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
        val currentVersionCode = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_META_DATA).versionName

        return try {
            currentVersionCode.split("\\.")[1]
        } catch (e: Exception) {
            //Error getting current minor version
            return null
        }
    }

}