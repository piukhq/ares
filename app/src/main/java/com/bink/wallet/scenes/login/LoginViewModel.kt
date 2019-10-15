package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LoginData
import kotlinx.coroutines.launch

class LoginViewModel constructor(var loginRepository: LoginRepository) : BaseViewModel() {

    var loginBody = MutableLiveData<LoginBody>()
    var loginData = MutableLiveData<LoginData>()
    var loginEmail = loginRepository.loginEmail

    fun authenticate() {
        loginRepository.doAuthenticationWork(
            LoginResponse(
                LoginBody(
                    System.currentTimeMillis() / 1000,
                    loginEmail,
                    0.0,
                    0.0
                )
            ), loginBody
        )
    }

    fun retrieveStoredLoginData() = viewModelScope.launch {
        loginRepository.retrieveStoredLoginData(loginData)
    }
}