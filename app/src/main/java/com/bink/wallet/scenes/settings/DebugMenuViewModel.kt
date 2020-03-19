package com.bink.wallet.scenes.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.ListLiveData
import com.bink.wallet.scenes.login.LoginRepository
import okhttp3.ResponseBody

class DebugMenuViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    val debugItems = ListLiveData<DebugItem>()
    private val _logOutResponse = MutableLiveData<ResponseBody>()
    val logOutResponse: LiveData<ResponseBody>
        get() = _logOutResponse
    private val _logOutErrorResponse = MutableLiveData<Exception>()
    val logOutErrorResponse: LiveData<Exception>
        get() = _logOutErrorResponse

    fun logOut() {
        loginRepository.logOut(_logOutResponse, _logOutErrorResponse)
    }
}