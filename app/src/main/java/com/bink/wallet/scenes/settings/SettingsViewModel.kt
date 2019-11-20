package com.bink.wallet.scenes.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.LocalStoreUtils
import kotlinx.coroutines.launch

class SettingsViewModel constructor(var loginRepository: LoginRepository) :
    BaseViewModel() {

    var loginData = MutableLiveData<LoginData>()
    val itemsList = ListLiveData<SettingsItem>()

    fun retrieveStoredLoginData(context: Context) = viewModelScope.launch {
        LocalStoreUtils.getAppSharedPref(
            LocalStoreUtils.KEY_EMAIL,
            context
        )
    }

    fun storeLoginData(email: String, context: Context) {
        LocalStoreUtils.setAppSharedPref(
            LocalStoreUtils.KEY_EMAIL,
            email,
            context
        )
    }
}