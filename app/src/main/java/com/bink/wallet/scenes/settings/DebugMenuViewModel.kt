package com.bink.wallet.scenes.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class DebugMenuViewModel(
    private val loginRepository: LoginRepository,
    var loyaltyWalletRepository: LoyaltyWalletRepository,
    var paymentWalletRepository: PaymentWalletRepository
) : BaseViewModel() {

    val debugItems = ListLiveData<DebugItem>()
    private val _logOutResponse = MutableLiveData<ResponseBody>()
    val logOutResponse: LiveData<ResponseBody>
        get() = _logOutResponse
    private val _logOutErrorResponse = MutableLiveData<Exception>()
    val logOutErrorResponse: LiveData<Exception>
        get() = _logOutErrorResponse
    private val _clearResponse = MutableLiveData<Unit>()
    val clearResponse: LiveData<Unit>
        get() = _clearResponse

    private val _clearErrorResponse = MutableLiveData<Exception>()
    val clearErrorResponse: LiveData<Exception>
        get() = _clearErrorResponse

    fun logOut() {
        viewModelScope.launch {
            try {
                _logOutResponse.value = loginRepository.logOut()
                loyaltyWalletRepository.clearMembershipCards()
                paymentWalletRepository.clearPaymentCards()
            } catch (e: Exception) {
                _logOutErrorResponse.value = e
            }
        }
    }

    fun clearData() {
        loginRepository.clearData(_clearResponse, _clearErrorResponse)
    }
}