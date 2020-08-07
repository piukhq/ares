package com.bink.wallet.scenes.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.auth.User
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.login.LoginRepository.Companion.DEFAULT_LOGIN_ID
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.utils.EMAIL_REGEX
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.PASSWORD_REGEX
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.combineNonNull
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class LoginViewModel constructor(
    var loginRepository: LoginRepository,
    val loyaltyWalletRepository: LoyaltyWalletRepository,
    val userRepository: UserRepository
) : BaseViewModel() {

    val loginBody = MutableLiveData<LoginBody>()
    val loginData = MutableLiveData<LoginData>()
    val logInResponse = MutableLiveData<SignUpResponse>()
    private val _logInErrorResponse = MutableLiveData<Exception>()
    val logInErrorResponse: LiveData<Exception>
        get() = _logInErrorResponse
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()
    val membershipPlanMutableLiveData: MutableLiveData<List<MembershipPlan>> =
        MutableLiveData()
    val membershipPlanErrorLiveData: MutableLiveData<Exception> = MutableLiveData()
    val membershipPlanDatabaseLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _userResponse = MutableLiveData<User>()
    val userResponse: LiveData<User>
        get() = _userResponse
    private val _postServiceResponse = MutableLiveData<ResponseBody>()
    val postServiceResponse: LiveData<ResponseBody>
        get() = _postServiceResponse

    private val _postServiceErrorResponse = MutableLiveData<Exception>()
    val postServiceErrorResponse: LiveData<Exception>
        get() = _postServiceErrorResponse

    private val _getUserResponse = MutableLiveData<Boolean>()
    val getUserResponse: LiveData<Boolean>
        get() = _getUserResponse

    private val passwordValidator = Transformations.map(password) {
        UtilFunctions.isValidField(PASSWORD_REGEX, it)
    }
    private val emailValidator = Transformations.map(email) {
        UtilFunctions.isValidField(EMAIL_REGEX, it)
    }
    val isLoginEnabled = MediatorLiveData<Boolean>()

    init {
        isLoginEnabled.combineNonNull(
            emailValidator,
            passwordValidator,
            ::validateFields
        )
    }

    private fun validateFields(
        emailValidator: Boolean,
        passwordValidator: Boolean
    ): Boolean = emailValidator && passwordValidator


    fun logIn(loginRequest: SignUpRequest) {
        loginRepository.logIn(loginRequest, logInResponse, _logInErrorResponse)
    }

    fun retrieveStoredLoginData() = viewModelScope.launch {
        loginData.value = LoginData(
            DEFAULT_LOGIN_ID,
            LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_EMAIL
            ) ?: EMPTY_STRING
        )
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
        userRepository.getUserDetails(_getUserResponse,_userResponse)
    }
}