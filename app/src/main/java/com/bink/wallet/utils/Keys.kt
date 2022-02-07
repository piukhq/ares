package com.bink.wallet.utils

object Keys {

        init {
            System.loadLibrary("api_keys-lib")
        }
    external fun mixPanelBetaApiKey():String

    external fun binkTestAuthToken():String
}