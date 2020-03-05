package com.bink.wallet.scenes.registration

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.auth.FacebookAuthResponse
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import okhttp3.ResponseBody

class AcceptTCViewModel(
    val loginRepository: LoginRepository,
    val loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {

    var facebookAuthResult = MutableLiveData<FacebookAuthResponse>()
    var facebookAuthError = MutableLiveData<Throwable>()
    var marketingPreferences = MutableLiveData<ResponseBody>()
    var marketingError = MutableLiveData<Throwable>()
    val shouldAcceptBeEnabled = MutableLiveData<Boolean>()

    val membershipPlanMutableLiveData: MutableLiveData<List<MembershipPlan>> =
        MutableLiveData()
    val membershipPlanErrorLiveData: MutableLiveData<Throwable> = MutableLiveData()
    val membershipPlanDatabaseLiveData =
        loyaltyWalletRepository.liveDataDatabaseUpdated

    init {
        shouldAcceptBeEnabled.value = false
    }

    fun authWithFacebook(facebookAuthRequest: FacebookAuthRequest) {
        loginRepository.authWithFacebook(facebookAuthRequest, facebookAuthResult, facebookAuthError)
    }

    fun handleMarketingPreferences(marketingOption: MarketingOption) {
        loginRepository.checkMarketingPref(marketingOption, marketingPreferences, marketingError)
    }

    fun getMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans(
            membershipPlanMutableLiveData,
            membershipPlanErrorLiveData,
            false
        )
    }
}