package com.bink.wallet.scenes.settings

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.scenes.login.LoginRepository
import okhttp3.ResponseBody

class DebugMenuViewModel(val loginRepository: LoginRepository) : BaseViewModel() {

    val debugItems = ListLiveData<DebugItem>()
    val logOutResponse = MutableLiveData<ResponseBody>()
    val logOutErrorResponse = MutableLiveData<Throwable>()

    fun logOut() {
        loginRepository.logOut(logOutResponse, logOutErrorResponse)
    }
}