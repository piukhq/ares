package com.bink.wallet.scenes.whats_new

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.WhatsNew
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.launch

class WhatsNewViewModel(private val dataStoreSource: DataStoreSourceImpl) : BaseViewModel() {

    var whatsNew = MutableLiveData<WhatsNew>()

    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    init {
        getSelectedTheme()
    }

    private fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }
}