package com.bink.wallet.scenes.preference

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.model.request.Preference
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.MAGIC_LINK_BUNDLE_ID
import com.bink.wallet.utils.MAGIC_LINK_LOCALE
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class PreferencesViewModel(private var loginRepository: LoginRepository) : BaseViewModel() {

    val preferences = MutableLiveData<List<Preference>>()
    val preferenceErrorResponse = MutableLiveData<Exception>()
    val savePreferenceError = MutableLiveData<Exception>()

    fun getPreferences() {
        viewModelScope.launch {
            try {
                preferences.value = loginRepository.getPreferences()
            } catch (e: Exception){
                preferenceErrorResponse.value = e
            }
        }
    }

    fun savePreference(requestBody: String) {
        viewModelScope.launch {
            try {
                loginRepository.setPreference(requestBody)
            } catch (e: Exception) {
                savePreferenceError.value = e
            }
        }
    }

}
