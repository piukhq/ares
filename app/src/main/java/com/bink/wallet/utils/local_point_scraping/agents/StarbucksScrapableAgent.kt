package com.bink.wallet.utils.local_point_scraping.agents

import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants

class StarbucksScrapableAgent : WebScrapable() {

    override val merchant = PointScrapeSite.STARBUCKS

    override val membershipPlanId = when (ApiConstants.BASE_URL) {
        ApiConfig.PROD_URL -> 15
        ApiConfig.STAGING_URL -> 15
        ApiConfig.DEV_URL -> 15
        else -> 15
    }

    override val usernameFieldTitle: String
        get() = "Email"

    override val passwordFieldTitle: String
        get() = "Password"

    override val cardBalancePrefix: String
        get() = ""

    override val cardBalanceSuffix: String
        get() = "stars"
}