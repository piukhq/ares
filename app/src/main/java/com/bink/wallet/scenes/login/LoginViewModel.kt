package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel

class LoginViewModel constructor(var loginRepository: LoginRepository) : BaseViewModel() {

    var loginData: MutableLiveData<LoginBody> = MutableLiveData()

    fun authenticate() {
        loginRepository.doAuthenticationWork(
            LoginResponse(
                LoginBody(
                    System.currentTimeMillis() / 1000,
                    "stagingQA_sri@testbink.com",
                    0.0,
                    12.345
                )
            ), loginData
        )
    }
}