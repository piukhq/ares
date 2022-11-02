package com.bink.wallet.scenes.loyalty_wallet.barcode

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.launch
import com.bink.wallet.model.request.Preference
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.ALWAYS_SHOW_BARCODE_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BarcodeViewModel(
    private val dataStoreSource: DataStoreSourceImpl,
    private var loginRepository: LoginRepository
) : BaseViewModel() {
    var companyName = MutableLiveData<String>()
    var shouldShowLabel = MutableLiveData<Boolean>()
    var forceShowBarcode = mutableStateOf(false)
    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    init {
        getPreferences()
    }

    fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }


    fun setBarcodePreference() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    loginRepository.setPreference(
                        requestBody = JSONObject().put(
                            ALWAYS_SHOW_BARCODE_KEY, 1
                        ).toString()
                    )
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun getPreferences() {
        viewModelScope.launch {
            try {
                val prefsAsList =
                    withContext(Dispatchers.IO) { loginRepository.getPreferences() }
                val prefs = prefsAsList as ArrayList<Preference>
                prefs.firstOrNull { it.slug == ALWAYS_SHOW_BARCODE_KEY }?.let {
                    forceShowBarcode.value = it.value?.toIntOrNull() == 1
                }
            } catch (e: Exception) {

            }
        }
    }
}