package com.bink.wallet.scenes.login

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.scenes.login.LoginRepository.Companion.DEFAULT_LOGIN_ID
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.launch

class LoginViewModel constructor(var loginRepository: LoginRepository) : BaseViewModel() {

    var loginBody = MutableLiveData<LoginBody>()
    var loginData = MutableLiveData<LoginData>()
    val logInResponse = MutableLiveData<SignUpResponse>()
    val logInErrorResponse = MutableLiveData<Throwable>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    fun authenticate() {
        loginRepository.doAuthenticationWork(
            LoginResponse(
                LoginBody(
                    System.currentTimeMillis() / 1000,
                    loginData.value?.email ?: EMPTY_STRING,
                    0.0,
                    0.0
                )
            ), loginBody
        )
    }

    fun logIn(loginRequest: SignUpRequest) {
        loginRepository.logIn(loginRequest, logInResponse, logInErrorResponse)
    }

    fun retrieveStoredLoginData(context: Context) = viewModelScope.launch {
        loginData.value = LoginData(
            DEFAULT_LOGIN_ID,
            LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_EMAIL, context
            ) ?: EMPTY_STRING
        )
    }
}