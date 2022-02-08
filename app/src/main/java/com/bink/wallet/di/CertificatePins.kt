package com.bink.wallet.di

enum class CertificatePins(val domain: String, val hash: String) {
    // This class is "infinitely expandable" with extra domains & hashes,
    // so for updated keys we just have to add new entries... good, isn't it? 🙂
    // NOTE: The signature is base64, so HAS to end in "=" to be valid (or it's ignored)
//    DEBUG("api.dev.gb.bink.com", "sha256/35Y7j68+91YIgnJV8bQQfGfqwLP1OPnM6PZAjVw79P4="),
//    STAGING("api.staging.gb.bink.com", "sha256/z7/qT56fkSkCNT8b4CKoIyCuK7MEFytc63IzAyRgD1g="),
    PROD("api.bink.com", "sha256/U4So9I4CQcjW67YtUpr8RqD90fWq9ydCikF82W7Vtuc="),
    PROD_2("api.bink.com", "sha256/4TTLsYF+NPCxZrkXK/+zOuUsYYyPKN+w5KMus7qLgHk=")

}