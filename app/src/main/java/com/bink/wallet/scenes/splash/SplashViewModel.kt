package com.bink.wallet.scenes.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.settings.UserRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class SplashViewModel(val loginRepository: LoginRepository, val userRepository: UserRepository) : BaseViewModel() {

    private val _postServiceResponse = MutableLiveData<ResponseBody>()
    val postServiceResponse: LiveData<ResponseBody>
        get() = _postServiceResponse

    private val _postServiceErrorResponse = MutableLiveData<Exception>()
    val postServiceErrorResponse: LiveData<Exception>
        get() = _postServiceErrorResponse

    private val _getUserResponse = MutableLiveData<User>()
    val getUserResponse: LiveData<User>
        get() = _getUserResponse

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user =
                    userRepository.getUserDetails()

                _getUserResponse.value = user
            } catch (e: Exception) {
                _postServiceErrorResponse.value = e
            }

        }
    }
}