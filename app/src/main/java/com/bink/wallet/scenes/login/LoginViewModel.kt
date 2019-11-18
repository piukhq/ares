package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import kotlinx.coroutines.launch

class LoginViewModel constructor(var loginRepository: LoginRepository) : BaseViewModel() {

    var loginBody = MutableLiveData<LoginBody>()
    var loginData = MutableLiveData<LoginData>()
    var loginEmail = loginRepository.loginEmail
    val logInResponse = MutableLiveData<SignUpResponse>()
    val logInErrorResponse = MutableLiveData<Throwable>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

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

    fun logIn(loginRequest: SignUpRequest) {
        loginRepository.logIn(loginRequest, logInResponse, logInErrorResponse)
    }

    fun retrieveStoredLoginData() = viewModelScope.launch {
        loginRepository.retrieveStoredLoginData(loginData)
    }
}