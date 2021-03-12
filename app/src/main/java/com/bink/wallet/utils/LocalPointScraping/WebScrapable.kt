package com.bink.wallet.utils.LocalPointScraping

import com.bink.wallet.utils.getDebugSuffix
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

abstract class WebScrapable {

    abstract val merchant: PointScrapeSite
    abstract val membershipPlanId: Int
    abstract val usernameFieldTitle: String
    abstract val passwordFieldTitle: String
    abstract val cardBalancePrefix: String
    abstract val cardBalanceSuffix: String

    fun isEnabled(firebaseRemoteConfig: FirebaseRemoteConfig): Boolean {
        val remoteConfigKey = "LPC_${merchant.remoteName}_enabled"
        return firebaseRemoteConfig.getBoolean(remoteConfigKey.getDebugSuffix())
    }

}

enum class PointScrapeSite(val remoteName: String, val signInURL: String, val scrapeURL: String) {
    TESCO("tesco", "https://secure.tesco.com/account/en-GB/login?from=https://secure.tesco.com/Clubcard/MyAccount/home/Home", "https://secure.tesco.com/Clubcard/MyAccount/home/Home")
}