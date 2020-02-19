package com.bink.wallet.di.qualifier.network

import org.koin.core.qualifier.Qualifier

class NetworkQualifiers {
    object BinkRetrofit : Qualifier
    object SpreedlyRetrofit : Qualifier
    object BinkOkHttp : Qualifier
    object SpreedlyOkHttp : Qualifier
    object BinkApiInterface : Qualifier
    object SpreedlyApiInterface : Qualifier
}