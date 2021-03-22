package com.bink.wallet.utils.local_point_scraping.agents

import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants

class SuperdrugScrapableAgent : WebScrapable() {

    override val merchant = PointScrapeSite.SUPERDRUG

    override val membershipPlanId = when (ApiConstants.BASE_URL) {
        ApiConfig.PROD_URL -> 16
        ApiConfig.STAGING_URL -> 16
        ApiConfig.DEV_URL -> 16
        else -> 16
    }

    override val usernameFieldTitle: String
        get() = "Email Address"

    override val passwordFieldTitle: String
        get() = "Password"

    override val cardBalancePrefix: String
        get() = ""

    override val cardBalanceSuffix: String
        get() = "points"
}