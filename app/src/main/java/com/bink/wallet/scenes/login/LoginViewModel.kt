package com.bink.wallet.scenes.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel

class LoginViewModel constructor(private val loginRepository: LoginRepository) : BaseViewModel() {
    var loginData: LiveData<LoginBody> = MutableLiveData()

    fun authenticate() {
        loginData = loginRepository.doAuthenticationWork(
            LoginResponse(
                LoginBody(
                    System.currentTimeMillis() / 1000,
                    "Bink20iteration1@testbink.com",
                    0.0,
                    12.345
                )
            )
        )
    }
}