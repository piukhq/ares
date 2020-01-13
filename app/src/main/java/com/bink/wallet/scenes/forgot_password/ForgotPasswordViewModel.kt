package com.bink.wallet.scenes.forgot_password

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.scenes.login.LoginRepository
import okhttp3.ResponseBody

class ForgotPasswordViewModel(val loginRepository: LoginRepository) : BaseViewModel() {
    val email = MutableLiveData<String>()

    val forgotPasswordResponse = MutableLiveData<ResponseBody>()
    val forgotPasswordError = MutableLiveData<Throwable>()
    val isLoading = MutableLiveData<Boolean>()

    fun forgotPassword() {
        email.value?.let {
            loginRepository.forgotPassword(it, forgotPasswordResponse, forgotPasswordError)
        }
    }
}
