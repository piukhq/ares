package com.bink.wallet.utils.enums

enum class ApiVersion(val url: String) {
    DEV("https://api.dev.gb.bink.com"),
    STAGING("https://api.staging.gb.bink.com"),
    DAEDALUS("https://mcwallet.dev.gb.bink.com")
}