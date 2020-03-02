package com.bink.wallet.utils.enums

enum class ApiVersion(val url: String) {
    DEV("api.dev.gb.bink.com"),
    STAGING("api.staging.gb.bink.com"),
    DAEDALUS("mcwallet.dev.gb.bink.com")
}