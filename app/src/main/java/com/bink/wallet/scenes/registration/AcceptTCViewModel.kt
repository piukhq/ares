package com.bink.wallet.scenes.registration

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.auth.FacebookAuthResponse
import com.bink.wallet.model.auth.User
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import okhttp3.ResponseBody

class AcceptTCViewModel(
    val loginRepository: LoginRepository,
    val loyaltyWalletRepository: LoyaltyWalletRepository,
    val userRepository: UserRepository
) : BaseViewModel() {

    val shouldLoadingBeVisible = ObservableBoolean(false)

    var facebookAuthResult = MutableLiveData<FacebookAuthResponse>()
    var facebookAuthError = MutableLiveData<Exception>()
    var marketingPreferences = MutableLiveData<ResponseBody>()
    var marketingError = MutableLiveData<Exception>()
    val shouldAcceptBeEnabled = MutableLiveData<Boolean>()

    val membershipPlanMutableLiveData: MutableLiveData<List<MembershipPlan>> =
        MutableLiveData()
    val membershipPlanErrorLiveData: MutableLiveData<Exception> = MutableLiveData()
    val membershipPlanDatabaseLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private val _postServiceResponse = MutableLiveData<ResponseBody>()
    val postServiceResponse: LiveData<ResponseBody>
        get() = _postServiceResponse

    private val _postServiceErrorResponse = MutableLiveData<Exception>()
    val postServiceErrorResponse: LiveData<Exception>
        get() = _postServiceErrorResponse

    private val _getUserResponse = MutableLiveData<User>()
    val getUserResponse: LiveData<User>
        get() = _getUserResponse
    private val _userReturned = MutableLiveData<Boolean>()
    val userReturned: LiveData<Boolean>
        get() = _userReturned

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
            membershipPlanDatabaseLiveData
        )
    }

    fun postService(postServiceRequest: PostServiceRequest) {
        loginRepository.postService(
            postServiceRequest,
            _postServiceResponse,
            _postServiceErrorResponse
        )
    }

    fun getCurrentUser() {
        userRepository.getUserDetails(_getUserResponse,_userReturned)
    }
}