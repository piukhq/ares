package com.bink.wallet.scenes.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.settings.UserRepository
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

    fun postService(postServiceRequest: PostServiceRequest) {
        loginRepository.postService(
            postServiceRequest,
            _postServiceResponse,
            _postServiceErrorResponse
        )
    }

    fun getCurrentUser() {
        userRepository.getUserDetails(_getUserResponse)
    }
}