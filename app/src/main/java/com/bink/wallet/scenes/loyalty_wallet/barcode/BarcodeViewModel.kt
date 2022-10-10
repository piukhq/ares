package com.bink.wallet.scenes.loyalty_wallet.barcode

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.launch

class BarcodeViewModel(private val dataStoreSource: DataStoreSourceImpl) : BaseViewModel() {
    var companyName = MutableLiveData<String>()
    var shouldShowLabel = MutableLiveData<Boolean>()
    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    fun getSelectedTheme(){
        viewModelScope.launch {
            dataStoreSource.getMode().collect{
                _theme.value = it
            }
        }
    }
}