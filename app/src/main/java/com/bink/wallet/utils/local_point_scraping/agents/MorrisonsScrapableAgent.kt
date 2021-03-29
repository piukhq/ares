package com.bink.wallet.utils.local_point_scraping.agents

import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants

class MorrisonsScrapableAgent : WebScrapable() {

    override val merchant = PointScrapeSite.MORRISONS

    override val membershipPlanId = when (ApiConstants.BASE_URL) {
        ApiConfig.PROD_URL -> 12
        ApiConfig.STAGING_URL -> 12
        ApiConfig.DEV_URL -> 12
        else -> 12
    }

    override val usernameFieldTitle: String
        get() = "Email"

    override val passwordFieldTitle: String
        get() = "Password"

    override val cardBalancePrefix: String
        get() = ""

    override val cardBalanceSuffix: String
        get() = "points"
}