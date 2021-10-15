package com.bink.wallet.scenes.preference

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.model.request.Preference
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.*
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class PreferencesViewModel(private var loginRepository: LoginRepository) : BaseViewModel() {

    val preferences = MutableLiveData<ArrayList<Preference>>()
    val preferenceErrorResponse = MutableLiveData<Exception>()
    val savePreferenceError = MutableLiveData<Exception>()
    val clearCredsPreference = Preference(CLEAR_PREF_KEY, null, null, null, null, null,null, "Clear Stored Credentials", null)

    fun getPreferences() {
        viewModelScope.launch {
            try {
                val prefsAsList = loginRepository.getPreferences()
                preferences.value = prefsAsList as ArrayList<Preference>
                if(doesHaveSavedCredentials()){
                    preferences.value?.add(clearCredsPreference)
                }
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

    private fun doesHaveSavedCredentials() : Boolean {
        REMEMBERABLE_FIELD_NAMES.forEach { fieldName ->
            FormsUtil.getFormFields(fieldName)?.let {
                //Making sure email is specifically more than 1 because it will always return logged in email
                return if(fieldName == "email"){
                    it.size > 1
                } else {
                    true
                }
            }
        }

        return false
    }

}
