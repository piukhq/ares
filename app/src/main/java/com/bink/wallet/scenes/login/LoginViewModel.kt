package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LoginData

class LoginViewModel constructor(var loginRepository: LoginRepository) : BaseViewModel() {

    var loginBody = MutableLiveData<LoginBody>()
    var loginData = MutableLiveData<LoginData>()
//    var loginEmail = "Bink20iteration1@testbink.com"
    var loginEmail = "mwoodhams@testbink.com"

    fun authenticate() {
        loginRepository.doAuthenticationWork(
            LoginResponse(
                LoginBody(
                    System.currentTimeMillis() / 1000,
                    loginEmail,
                    0.0,
                    12.345
                )
            ), loginBody
        )
    }

    fun retrieveStoredLoginData() {
        loginRepository.retrieveStoredLoginData(loginData)
        if (!loginData.value!!.email.isNullOrEmpty() &&
            !loginData.value!!.email.equals(loginEmail)) {
            loginEmail = loginData.value!!.email!!
        }
    }
}