package com.bink.wallet.utils.enums

enum class BuildTypes(val type: String) {
    DEBUG("debug"),
    RELEASE("release"),
    MR("mr"),
    BETA("beta"),
    NIGHTLY("nightly"),
    GAMMA("gamma"),
    EXTERNAL("external")
}