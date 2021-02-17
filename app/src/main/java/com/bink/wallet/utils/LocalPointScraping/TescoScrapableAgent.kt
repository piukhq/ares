package com.bink.wallet.utils.LocalPointScraping

import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants

class TescoScrapableAgent : WebScrapable() {

    override val merchant = ScrapableMerchanct.TESCO

    override val membershipPlanId = when (ApiConstants.BASE_URL) {
        ApiConfig.PROD_URL -> 203
        ApiConfig.STAGING_URL -> 230
        ApiConfig.DEV_URL -> 207
        else -> 203
    }

    override val usernameFieldTitle: String
        get() = "Email"

    override val passwordFieldTitle: String
        get() = "Password"

}