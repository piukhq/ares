package com.bink.wallet.di

enum class CertificatePins(val domain: String, val hash: String) {
    DEBUG("api.dev.gb.bink.com", "sha256/35Y7j68+91YIgnJV8bQQfGfqwLP1OPnM6PZAjVw79P4="),
    STAGING("api.staging.gb.bink.com", "sha256/z7/qT56fkSkCNT8b4CKoIyCuK7MEFytc63IzAyRgD1g=")
}