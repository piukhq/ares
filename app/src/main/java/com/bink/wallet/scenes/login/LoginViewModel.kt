package com.bink.wallet.scenes.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class LoginViewModel constructor(private val loginRepository: LoginRepository) : ViewModel() {
    var loginData: LiveData<LoginBody> = loginRepository.doAuthenticationWork()
}