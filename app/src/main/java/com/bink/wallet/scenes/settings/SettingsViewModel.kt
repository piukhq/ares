package com.bink.wallet.scenes.settings

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.scenes.login.LoginRepository
import okhttp3.ResponseBody

class SettingsViewModel constructor(var loginRepository: LoginRepository) :
    BaseViewModel() {

    var loginData = MutableLiveData<LoginData>()
    val itemsList = ListLiveData<SettingsItem>()
    val logOutResponse = MutableLiveData<ResponseBody>()
    val logOutErrorResponse = MutableLiveData<Throwable>()

    fun logOut() {
        loginRepository.logOut(logOutResponse, logOutErrorResponse)
    }
}