package com.bink.wallet.scenes.sign_up

import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.utils.EMAIL_REGEX
import com.bink.wallet.utils.PASSWORD_REGEX
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.combineNonNull
import okhttp3.ResponseBody

class SignUpViewModel(
    var loginRepository: LoginRepository,
    val loyaltyWalletRepository: LoyaltyWalletRepository
) : BaseViewModel() {
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    val termsCondition = MutableLiveData<Boolean>()
    val marketingMessages = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData<Boolean>()

    val signUpResponse = MutableLiveData<SignUpResponse>()
    val signUpErrorResponse = MutableLiveData<Exception>()
    val marketingPrefResponse = MutableLiveData<ResponseBody>()
    val marketingPrefErrorResponse = MutableLiveData<Exception>()

    val membershipPlanMutableLiveData: MutableLiveData<List<MembershipPlan>> =
        MutableLiveData()
    val membershipPlanErrorLiveData: MutableLiveData<Exception> = MutableLiveData()
    val membershipPlanDatabaseLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private val passwordValidator = Transformations.map(password) {
        UtilFunctions.isValidField(PASSWORD_REGEX, it)
    }
    private val emailValidator = Transformations.map(email) {
        UtilFunctions.isValidField(EMAIL_REGEX, it)
    }

    private val passwordMatcher = MediatorLiveData<Boolean>()
    var isSignUpEnabled = MediatorLiveData<Boolean>()

    init {
        passwordMatcher.combineNonNull(
            password,
            confirmPassword,
            ::arePasswordsMatching
        )

        isSignUpEnabled.combineNonNull(
            emailValidator,
            passwordValidator,
            passwordMatcher,
            termsCondition,
            isLoading,
            ::isSignUpButtonEnabled
        )
    }

    private fun isSignUpButtonEnabled(
        emailValidator: Boolean,
        passwordValidator: Boolean,
        passwordMatcher: Boolean,
        termsAndConditions: Boolean,
        isLoading: Boolean
    ): Boolean = passwordValidator &&
            emailValidator &&
            passwordMatcher &&
            termsAndConditions &&
            !isLoading

    private fun arePasswordsMatching(
        password: String,
        confirmedPassword: String
    ): Boolean =
        password == confirmedPassword

    fun signUp(signUpRequest: SignUpRequest) {
        loginRepository.signUp(
            signUpRequest,
            signUpResponse,
            signUpErrorResponse
        )
    }

    fun marketingPref(marketingOption: MarketingOption) {
        loginRepository.checkMarketingPref(
            marketingOption,
            marketingPrefResponse,
            marketingPrefErrorResponse
        )
    }

    fun getMembershipPlans() {
        loyaltyWalletRepository.retrieveMembershipPlans(
            membershipPlanMutableLiveData,
            membershipPlanErrorLiveData,
            membershipPlanDatabaseLiveData
        )
    }
}
