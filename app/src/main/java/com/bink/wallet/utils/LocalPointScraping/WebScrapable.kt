package com.bink.wallet.utils.LocalPointScraping

abstract class WebScrapable {

    abstract val merchant: PointScrapeSite
    abstract val membershipPlanId: Int
    abstract val usernameFieldTitle: String
    abstract val passwordFieldTitle: String
    abstract val cardBalancePrefix: String

}

enum class PointScrapeSite(val signInURL: String, val scrapeURL: String) {
    TESCO("https://secure.tesco.com/account/en-GB/login?from=https://secure.tesco.com/Clubcard/MyAccount/home/Home", "https://secure.tesco.com/Clubcard/MyAccount/home/Home")
}