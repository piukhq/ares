package com.bink.wallet.scenes.preference

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.Preference
import com.bink.wallet.scenes.login.LoginRepository
import okhttp3.ResponseBody

class PreferencesViewModel(private var loginRepository: LoginRepository) : BaseViewModel() {

    val preferences = MutableLiveData<List<Preference>>()
    val preferenceErrorResponse = MutableLiveData<Throwable>()
    val savePreferenceError = MutableLiveData<Throwable>()
    val savePreference = MutableLiveData<ResponseBody>()

    fun getPreferences() {
        loginRepository.getPreferences(preferences, preferenceErrorResponse)
    }

    fun savePreference(json: String) {
        loginRepository.setPreference(json, savePreference, savePreferenceError)
    }

}
