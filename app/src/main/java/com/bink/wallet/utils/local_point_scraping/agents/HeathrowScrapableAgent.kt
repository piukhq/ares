package com.bink.wallet.utils.local_point_scraping.agents

import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants

class HeathrowScrapableAgent : WebScrapable() {

    override val merchant = PointScrapeSite.HEATHROW

    override val membershipPlanId = when (ApiConstants.BASE_URL) {
        ApiConfig.PROD_URL -> 32
        ApiConfig.STAGING_URL -> 32
        ApiConfig.DEV_URL -> 32
        else -> 32
    }

    override val usernameFieldTitle: String
        get() = "Email address"

    override val passwordFieldTitle: String
        get() = "Password"

    override val cardBalancePrefix: String
        get() = ""

    override val cardBalanceSuffix: String
        get() = "points"
}