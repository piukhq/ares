package com.bink.wallet.scenes.sign_up.continue_with_email.magic_link_result

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R
import com.bink.wallet.model.Consent
import com.bink.wallet.model.MagicLinkToken
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.auth.User
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.utils.JWTUtils
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MagicLinkResultViewModel(
    val loginRepository: LoginRepository, val userRepository: UserRepository, val loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _membershipPlans = MutableLiveData<List<MembershipPlan>>()
    val membershipPlans: LiveData<List<MembershipPlan>>
        get() = _membershipPlans

    fun postMagicLinkToken(context: Context, token: String) {
        _isLoading.value = true
        val magicLinkToken = MagicLinkToken(token)
        viewModelScope.launch {
            try {
                val responseToken = loginRepository.sendMagicLinkToken(magicLinkToken)
                getUser(context, responseToken.access_token)
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun getUser(context: Context, token: String) {
        LocalStoreUtils.setAppSharedPref(
            LocalStoreUtils.KEY_TOKEN,
            context.getString(R.string.token_api_v1, token)
        )

        try {
            JWTUtils.decode(token)?.let { tokenJson ->
                val emailFromJson = JSONObject(tokenJson).getString("user_id")
                _email.value = emailFromJson
            }
        } catch (e: JSONException) {
            _isLoading.value = false
        }

        viewModelScope.launch {
            try {
                val user = userRepository.getUserDetails()
                _user.value = user
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }

    }

    fun postConsent() {
        viewModelScope.launch {
            try {
                email.value?.let {
                    loginRepository.postService(
                        PostServiceRequest(Consent(it, System.currentTimeMillis() / 1000))
                    )
                }
            } catch (e: Exception) {

            }
        }
    }

    fun getMembershipPlans() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _isLoading.value = false
                val plans = loyaltyWalletRepository.retrieveMembershipPlans()
                loyaltyWalletRepository.storeAllMembershipPlans(plans)
                _membershipPlans.value = plans
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

}