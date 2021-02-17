package com.bink.wallet.utils.LocalPointScraping

abstract class WebScrapable {

    abstract val merchant: ScrapableMerchanct
    abstract val membershipPlanId: Int
    abstract val usernameFieldTitle: String
    abstract val passwordFieldTitle: String

}

enum class ScrapableMerchanct {
    TESCO,
    BOOTS,
    MORRISONS,
    SUPERDRUG,
    WATERSTONES,
    HEATHROW,
    PERFUMESHOP
}