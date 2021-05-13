package com.bink.wallet.utils.local_point_scraping.agents

import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants

class WaterstoneScrapableAgent : WebScrapable() {

    override val merchant = PointScrapeSite.WATERSTONES

    override val membershipPlanId = when (ApiConstants.BASE_URL) {
        ApiConfig.PROD_URL -> 52
        ApiConfig.STAGING_URL -> 52
        ApiConfig.DEV_URL -> 52
        else -> 52
    }

    override val usernameFieldTitle: String
        get() = "Email Address"

    override val passwordFieldTitle: String
        get() = "Password"

    override val cardBalancePrefix: String
        get() = ""

    override val cardBalanceSuffix: String
        get() = "stamps"
}