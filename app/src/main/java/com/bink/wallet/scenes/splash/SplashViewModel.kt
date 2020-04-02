package com.bink.wallet.scenes.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.scenes.login.LoginRepository
import okhttp3.ResponseBody

class SplashViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    private val _postServiceResponse = MutableLiveData<ResponseBody>()
    val postServiceResponse: LiveData<ResponseBody>
        get() = _postServiceResponse

    private val _postServiceErrorResponse = MutableLiveData<Exception>()
    val postServiceErrorResponse: LiveData<Exception>
        get() = _postServiceErrorResponse

    fun postService(postServiceRequest: PostServiceRequest) {
        loginRepository.postService(
            postServiceRequest,
            _postServiceResponse,
            _postServiceErrorResponse
        )
    }
}