package com.bink.wallet.scenes.registration

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.auth.FacebookAuthResponse
import com.bink.wallet.scenes.login.LoginRepository

class AcceptTCViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    var facebookAuthResult = MutableLiveData<FacebookAuthResponse>()
    var facebookAuthError = MutableLiveData<Throwable>()

    fun authWithFacebook(facebookAuthRequest: FacebookAuthRequest) {
        loginRepository.authWithFacebook(facebookAuthRequest, facebookAuthResult, facebookAuthError)
    }
}