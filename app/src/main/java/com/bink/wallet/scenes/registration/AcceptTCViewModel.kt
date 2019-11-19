package com.bink.wallet.scenes.registration

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.auth.FacebookAuthResponse
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.scenes.login.LoginRepository
import okhttp3.ResponseBody

class AcceptTCViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    var facebookAuthResult = MutableLiveData<FacebookAuthResponse>()
    var facebookAuthError = MutableLiveData<Throwable>()
    var marketingPreferences = MutableLiveData<ResponseBody>()
    var marketingError = MutableLiveData<Throwable>()
    var shouldAcceptBeEnabledTC = MutableLiveData<Boolean>()
    var shouldAcceptBeEnabledPrivacy = MutableLiveData<Boolean>()

    fun authWithFacebook(facebookAuthRequest: FacebookAuthRequest) {
        loginRepository.authWithFacebook(facebookAuthRequest, facebookAuthResult, facebookAuthError)
    }

    fun handleMarketingPreferences(marketingOption: MarketingOption){
        loginRepository.checkMarketingPref(marketingOption, marketingPreferences, marketingError)
    }
}