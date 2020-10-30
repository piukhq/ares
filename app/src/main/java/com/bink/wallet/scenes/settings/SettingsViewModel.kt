package com.bink.wallet.scenes.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.utils.LocalStoreUtils
import com.facebook.login.LoginManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class SettingsViewModel constructor(
    var loginRepository: LoginRepository,
    var loyaltyWalletRepository: LoyaltyWalletRepository,
    var paymentWalletRepository: PaymentWalletRepository,
    var userRepository: UserRepository
) :
    BaseViewModel() {

    var loginData = MutableLiveData<LoginData>()
    val itemsList = ListLiveData<SettingsItem>()
    val logOutResponse = MutableLiveData<ResponseBody>()
    val logOutErrorResponse = MutableLiveData<Exception>()

    private val _clearDataResponse = MutableLiveData<Unit>()
    val clearDataResponse: LiveData<Unit>
        get() = _clearDataResponse

    private val _clearErrorResponse = MutableLiveData<Exception>()
    val clearErrorResponse: LiveData<Exception>
        get() = _clearErrorResponse

    private val _userResponse = MutableLiveData<User>()
    val userResponse: LiveData<User>
        get() = _userResponse

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    fun logOut() {
        loginRepository.logOut(logOutResponse, logOutErrorResponse)
        loyaltyWalletRepository.clearMembershipCards()
        paymentWalletRepository.clearPaymentCards()
        LoginManager.getInstance().logOut()
    }

    fun clearData() {
        loginRepository.clearData(_clearDataResponse, _clearErrorResponse)
    }

    fun getSettingsTitle(): Int {
        return R.string.settings
    }

    fun getPlayStoreAppUrl(): Int {
        return R.string.play_store_market_url
    }

    fun getPlayStoreBrowserUrl(): Int {
        return R.string.play_store_browser_url
    }

    fun putUserDetails(user: User) {
        val handler = CoroutineExceptionHandler { _, _ -> //Exception handler to prevent app crash

        }
        scope.launch(handler) {
            try {
                val returnUser = withContext(Dispatchers.IO) { userRepository.putUserDetails(user) }

                returnUser.first_name?.let {
                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_FIRST_NAME,
                        it
                    )
                }

                returnUser.last_name?.let {
                    LocalStoreUtils.setAppSharedPref(
                        LocalStoreUtils.KEY_SECOND_NAME,
                        it
                    )
                }
                _userResponse.value = returnUser
            } catch (e: Exception) {

            }
        }
    }

    fun shouldShowUserDetailsDialog(): Boolean {
        var userFirstName = ""
        var userSecondName = ""

        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_FIRST_NAME)?.let { safeFirstName ->
            userFirstName = safeFirstName
        }

        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_SECOND_NAME)?.let { safeSecondName ->
            userSecondName = safeSecondName
        }

        return userFirstName.isEmpty() || userSecondName.isEmpty()
    }

    fun getUserEmail(): String {
        var userEmail = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let { safeEmail ->
            userEmail = safeEmail
        }

        return userEmail
    }

    fun getUsersFirstName(): String {
        var userFirstName = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_FIRST_NAME)?.let { safeFirstName ->
            userFirstName = safeFirstName
        }

        return userFirstName
    }

    fun getUsersLastName(): String {
        var userLastName = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_SECOND_NAME)?.let { safeLastName ->
            userLastName = safeLastName
        }

        return userLastName
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}