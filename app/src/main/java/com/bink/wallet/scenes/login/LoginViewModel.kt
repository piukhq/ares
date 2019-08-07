package com.bink.wallet.scenes.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class LoginViewModel constructor(loginRepository: LoginRepository) : ViewModel() {
    var loginData: LiveData<LoginBody> = loginRepository.doAuthenticationWork(
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