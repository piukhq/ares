package com.bink.wallet.scenes.login

import com.bink.wallet.network.ApiService

class LoginWorker {

    var apiService: ApiService? = null

    fun doAuthenticationWork() {
        apiService?.registerCustomer()
    }
}