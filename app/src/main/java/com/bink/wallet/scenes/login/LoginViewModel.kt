package com.bink.wallet.scenes.login

import androidx.lifecycle.LiveData
import com.bink.wallet.BaseViewModel

class LoginViewModel constructor(loginRepository: LoginRepository) : BaseViewModel() {
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