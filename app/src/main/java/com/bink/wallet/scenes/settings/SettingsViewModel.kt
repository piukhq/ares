package com.bink.wallet.scenes.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.scenes.login.LoginRepository
import kotlinx.coroutines.launch

class SettingsViewModel constructor(var loginRepository: LoginRepository) :
    BaseViewModel() {

    var loginData = MutableLiveData<LoginData>()
    val itemsList = ListLiveData<SettingsItem>()

    fun retrieveStoredLoginData() = viewModelScope.launch {
        loginRepository.retrieveStoredLoginData(loginData)
    }

    fun storeLoginData(email: String) {
        loginRepository.storeLoginData(email, loginData)
    }
}