package com.bink.wallet.utils

import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RequestReviewUtil(val activity: FragmentActivity?) {

    private val reviewManager = ReviewManagerFactory.create(activity)

    fun requestReviewFlow() {
        if (!hasReviewedInThisVersion()) {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val isReviewEnabled = remoteConfig.getString(REMOTE_CONFIG_REVIEW_ENABLED)

            if(isReviewEnabled.toLowerCase().equals("true")){
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
        val currentVersionCode = activity?.packageManager?.getPackageInfo(activity.packageName, PackageManager.GET_META_DATA)?.versionName

        return try {
            currentVersionCode?.split(".")?.get(1)
        } catch (e: Exception) {
            //Error getting current minor version
            return null
        }
    }

}