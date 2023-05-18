package com.bink.wallet.scenes.polls

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.PollItem
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.launch

class PollsViewModel(private val dataStoreSource: DataStoreSourceImpl) : BaseViewModel() {

    var poll = MutableLiveData<PollItem>()

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