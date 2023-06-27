package com.bink.wallet.scenes.prev_updates

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.launch

class PrevUpdatesViewModel(private val dataStoreSource: DataStoreSourceImpl) : BaseViewModel() {

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