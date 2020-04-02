package com.bink.wallet.scenes.forgot_password

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bink.wallet.BaseViewModel
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.EMAIL_REGEX
import com.bink.wallet.utils.UtilFunctions
import okhttp3.ResponseBody

class ForgotPasswordViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    private val _forgotPasswordResponse = MutableLiveData<ResponseBody>()
    val forgotPasswordResponse: LiveData<ResponseBody>
        get() = _forgotPasswordResponse
    private val _forgotPasswordError = MutableLiveData<Exception>()
    val forgotPasswordError: LiveData<Exception>
        get() = _forgotPasswordError
    val email = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()
    val isEmailValid: LiveData<Boolean> = Transformations.map(email) {
        UtilFunctions.isValidField(EMAIL_REGEX, it)
    }

    fun forgotPassword() {
        email.value?.let {
            isLoading.value = true
            loginRepository.forgotPassword(it, _forgotPasswordResponse, _forgotPasswordError)
        }
    }
}
