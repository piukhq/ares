package com.bink.wallet.scenes.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.launch

class DeleteAccountViewModel(var userRepository: UserRepository) : BaseViewModel() {

    val deleteError = mutableStateOf(false)
    val requestComplete = mutableStateOf(false)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        deleteUser()
    }

    private fun deleteUser() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.deleteUser()
                _isLoading.value = false
                requestComplete.value = true
            } catch (e: Exception) {
                logDebug("test123", e.localizedMessage)
                _isLoading.value = false
                deleteError.value = true
                requestComplete.value = true
            }
        }
    }

}