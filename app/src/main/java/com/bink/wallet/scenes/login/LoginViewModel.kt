package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel constructor(private val loginRepository: LoginRepository) : ViewModel() {
    //TODO Change email when login api is provided
    fun auth(liveData: MutableLiveData<LoginBody>) {
        loginRepository.doAuthenticationWork(
            LoginResponse(
                LoginBody(
                    System.currentTimeMillis() / 1000,
                    "Bink20iteration1@testbink.com",
                    0.0,
                    12.345
                )
            ), liveData
        )
    }
}