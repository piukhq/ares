package com.bink.wallet.scenes.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.facebook.login.LoginManager
import okhttp3.ResponseBody

class SettingsViewModel constructor(
    var loginRepository: LoginRepository,
    var loyaltyWalletRepository: LoyaltyWalletRepository,
    var paymentWalletRepository: PaymentWalletRepository
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

    fun logOut() {
        loginRepository.logOut(logOutResponse, logOutErrorResponse)
        loyaltyWalletRepository.clearMembershipCards()
        paymentWalletRepository.clearPaymentCards()
        LoginManager.getInstance().logOut()
    }

    fun clearData() {
        loginRepository.clearData(_clearDataResponse, _clearErrorResponse)
    }
}