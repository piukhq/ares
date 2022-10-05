package com.bink.wallet.scenes.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.*
import okhttp3.ResponseBody

class SettingsViewModel constructor(
    var loginRepository: LoginRepository,
    var loyaltyWalletRepository: LoyaltyWalletRepository,
    var paymentWalletRepository: PaymentWalletRepository,
    var userRepository: UserRepository,
    val dataStoreSource: DataStoreSourceImpl
) :
    BaseViewModel() {

    var loginData = MutableLiveData<LoginData>()
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

    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
    get() = _theme

     val showThemeDialog = mutableStateOf(false)


    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    fun logOut() {
        viewModelScope.launch {
            try {
                logOutResponse.value = loginRepository.logOut()
                loyaltyWalletRepository.clearMembershipCards()
                paymentWalletRepository.clearPaymentCards()
            } catch (e: Exception) {
                logOutErrorResponse.value = e
            }
        }
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
                val returnedUser =
                    withContext(Dispatchers.IO) { userRepository.putUserDetails(user) }

                _userResponse.value = returnedUser
            } catch (e: Exception) {

            }
        }
    }

    fun selectedTheme(theme:String){
        viewModelScope.launch {
            dataStoreSource.storeMode(theme)
        }
    }

    fun getSelectedTheme(){
        viewModelScope.launch {
            dataStoreSource.getMode().collect{
                _theme.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}