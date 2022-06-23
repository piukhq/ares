package com.bink.wallet.scenes.preference

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.Preference
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.CLEAR_CREDS_TITLE
import com.bink.wallet.utils.CLEAR_PREF_KEY
import com.bink.wallet.utils.EMAIL_COMMON_NAME
import com.bink.wallet.utils.REMEMBERABLE_FIELD_NAMES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreferencesViewModel(private var loginRepository: LoginRepository) : BaseViewModel() {

    val preferences = MutableLiveData<ArrayList<Preference>>()
    val preferenceErrorResponse = MutableLiveData<Exception>()
    val savePreferenceError = MutableLiveData<Exception>()
    private val clearCredsPreference = Preference(CLEAR_PREF_KEY, null, null, null, null, null, null, CLEAR_CREDS_TITLE, null)

    fun getPreferences() {
        viewModelScope.launch {
            try {
                val prefsAsList = withContext(Dispatchers.IO) { loginRepository.getPreferences() }
                preferences.value = prefsAsList as ArrayList<Preference>
                if (doesHaveSavedCredentials()) {
                    preferences.value?.add(clearCredsPreference)
                }
            } catch (e: Exception) {
                preferenceErrorResponse.value = e
            }
        }
    }

    fun savePreference(requestBody: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    loginRepository.setPreference(requestBody)
                }
            } catch (e: Exception) {
                savePreferenceError.value = e
            }
        }
    }

    private fun doesHaveSavedCredentials(): Boolean {
        REMEMBERABLE_FIELD_NAMES.forEach { fieldName ->
            FormsUtil.getFormFields(fieldName)?.let {
                //Making sure email is specifically more than 1 because it will always return logged in email
                if (fieldName == EMAIL_COMMON_NAME) {
                    if (it.size > 1) {
                        true
                    }
                } else {
                    return true
                }
            }
        }

        return false
    }

}
